import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import models.Country;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.Play;
import play.db.DB;

import com.avaje.ebean.Ebean;

public class Global extends GlobalSettings {

	public void onStart(Application app) {

		Logger.info("Application has started");

		/*
		 * Turn on SQL logging in the development environment.
		 */
		if (Play.isDev()) {
			Ebean.getServer(null).getAdminLogging().setDebugGeneratedSql(true);
		}

		/*
		 * Since we cannot insert initial data using evolutions & we have to
		 * work around some STORED PROCEDURE definition issues (see evolution
		 * 1.sql).
		 * 
		 * We define an InitialData insert routine that sets up the remainder of
		 * our database on initialization.
		 */
		InitialData.checkAndDoInitialization(app);

	}

	public void onStop(Application app) {
		Logger.info("Application shutdown...");
	}

	static class InitialData {

		public static void checkAndDoInitialization(Application app) {

			Logger.info("Checking Database...");

			/*
			 * No countries means our database is not yet initialised
			 */
			if (Ebean.find(Country.class).findRowCount() == 0) {

				Logger.info("Not Initialised. Now Initializing Database...");

				/*
				 * We initialise stored procedures and data here.
				 * 
				 * This is because Play Evolutions currently has a bug that
				 * prevents us from declaring our stored procedures in them.
				 * 
				 * See:
				 * https://play.lighthouseapp.com/projects/82401/tickets/212
				 * -evolutions-break-on-semicolons
				 */

				Connection conn = DB.getConnection();
				try {
					Statement stmt = conn.createStatement();

					/*
					 * Stored procedure workaround since we cannnot define them in our evolutions
					 */

					stmt.execute("CREATE OR REPLACE FUNCTION update_point_from_lat_lng()"
							+ " RETURNS TRIGGER AS $$"
							+ " BEGIN"
							+ "   NEW.point = ST_GeogFromText('SRID=4326;Point(' || CAST(NEW.longitude AS DECIMAL(15,6) ) || ' ' || CAST(NEW.latitude AS DECIMAL(15,6) ) || ')');"
							+ "   RETURN NEW;"
							+ " END;"
							+ "$$ language 'plpgsql';");

					stmt.execute("DROP TRIGGER IF EXISTS update_geotags_point ON geotags;");
					stmt.execute("CREATE TRIGGER update_geotags_point BEFORE INSERT OR UPDATE"
							+ " ON geotags FOR EACH ROW EXECUTE PROCEDURE"
							+ " update_point_from_lat_lng();");
					
					stmt.execute("CREATE OR REPLACE FUNCTION aggregate_tags_for_zoom(float, integer[], integer[]) RETURNS TABLE(tag_id integer, tag_name VARCHAR(32), weight bigint, point geometry) AS $$"
							+ "   DECLARE"
							+ "     zoom ALIAS FOR $1;"
							+ "     country_ids ALIAS FOR $2;"
							+ "     tag_ids ALIAS FOR $3;"
							+ "   BEGIN"
							+ "	  RETURN QUERY SELECT id, name, COUNT(id), geom FROM ("
							+ "	    SELECT geom, unnest(ids) as id, unnest(names) as name FROM ("
							+ "	      SELECT"
							+ "		array_agg(t.id) as ids,"
							+ "		array_agg(t.name) as names,"
							+ "		ST_Centroid(ST_Collect( g.point::geometry )) as geom"
							+ "	      FROM geotags g"
							+ "	      JOIN geotags_tags gt ON (g.id = gt.geotag_id)"
							+ "	      JOIN tags t ON (t.id = gt.tag_id)"
							+ "	      WHERE"
							+ "		(array_length(country_ids, 1) IS NULL OR g.country_id = ANY(country_ids)) AND"
							+ "		(array_length(tag_ids, 1) IS NULL OR gt.tag_id = ANY(tag_ids))  GROUP BY"
							+ "      ST_SnapToGrid( g.point::geometry, $1, $1/2)"
							+ "    ) as sub"
							+ "  ) as sub2 GROUP BY id, name, geom;"
							+ "   END"
							+ " $$ LANGUAGE plpgsql;");
					  
					/*
					 * Insert required data
					 */
					String path = "conf/initial-data.sql";
					Map<String, String> env = System.getenv();
					if (env.containsKey("OPENSHIFT_REPO_DIR")) {
						path = env.get("OPENSHIFT_REPO_DIR") + "/" + path;
					}
					BufferedReader in = new BufferedReader(new FileReader(path));
					conn.setAutoCommit(false); /* Turn off auto commit so the below are done as one transaction */
					String line;
					while((line = in.readLine()) != null) {
						Logger.info(line);
						stmt.executeUpdate(line);
					}
					in.close();
					conn.commit();
					
					Logger.info("Database is initialized.");
				
				} catch (SQLException e) {
					e.printStackTrace();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}

			else {
				Logger.info("Database is initialized.");
			}
		}
	}

}
