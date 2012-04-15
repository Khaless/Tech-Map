package models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import play.data.validation.Constraints;
import play.db.ebean.Model;

import com.avaje.ebean.Page;
import com.avaje.ebean.Query;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;

/**
 * Computer entity managed by Ebean
 */
@Entity 
@Table(name="countries")
public class Country extends Model {

    @Id
    public Long id;
    
    @Constraints.Required
    public String name;
        
    /**
     * Generic query helper for entity Taggable with id Long
     */
    public static Finder<Long,Country> find = new Finder<Long,Country>(Long.class, Country.class);
    
    public static Query<Country> findCountryByLatLng(double lat, double lng) {
    	
    	StringBuilder sb = new StringBuilder("SELECT c.id, c.name FROM countries c");
    	sb.append(" JOIN country_polygons cp ON (cp.country_id = c.id)");
    	sb.append(" WHERE ST_Intersects(ST_GeogFromText('SRID=4326;POINT(");
    	sb.append(Double.toString(lng)).append(" ").append(Double.toString(lat)).append(")'), cp.border)");
    	
		RawSql rawSql = RawSqlBuilder.parse(sb.toString())
				.columnMapping("c.id", "id")
				.columnMapping("c.name", "name").create();

		return find.setRawSql(rawSql);
    }
    
}

