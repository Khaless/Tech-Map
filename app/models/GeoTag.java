package models;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import play.data.format.Formats;
import play.data.validation.Constraints;
import play.db.ebean.Model;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;

/**
 * Computer entity managed by Ebean
 */
@Entity
@Table(name = "geotags")
public class GeoTag extends Model {

	@Id
	public Long id;

	@Constraints.Required
	@Formats.DateTime(pattern = "yyyy-MM-dd")
	public Date date_time;

	@ManyToOne
	public User user;

	@Constraints.Required
	public double latitude;

	@Constraints.Required
	public double longitude;

	@ManyToOne
	public Country country;

	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "geotags_tags", joinColumns = @JoinColumn(name = "geotag_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
	public Set<Tag> tags;

	/**
	 * Generic query helper for entity User with id Long
	 */
	public static Finder<Long, GeoTag> find = new Finder<Long, GeoTag>(
			Long.class, GeoTag.class);

	/*
	 * <b>Deprecated Function</b> left here as a reminder of how to do a manual
	 * update on an EBean.
	 * 
	 * This is now done by a trigger on insert/update.
	 */
	public static void updatePoint(double lat, double lng, long id) {
		Ebean.createSqlUpdate(
				"UPDATE geotags SET point = ST_GeogFromText('SRID=4326;POINT(' || :lng || ' ' || :lat || ')') WHERE id = :id")
				.setParameter("lat", lat).setParameter("lng", lng)
				.setParameter("id", id).execute();
	}

	public static Query<GeoTag> findByBoundingBox(double swLat, double swLng,
			double neLat, double neLng) {

		/*
		 * @formatter:off
		 * 
		 * Then arguments above give us two corners of a bounding box (SW and NE)
		 * We must convert this to a Geography Polygon of 4 corners.
		 * 
		 * +--------NE
		 * |        |
		 * |        |
		 * |        | 
		 * SW-------+
		 * 
		 * convert to
		 * 
		 * NW-------NE
		 * |        |
		 * |        |
		 * |        | 
		 * SW-------SE
		 * 
		 * Note: the POLYGON format of PostGIS is 
		 * POLYGON((longitude latitude, longitude_2 latitude_2, ...))
		 * 
		 */
		
		StringBuilder sb = new StringBuilder("SELECT id, user_id, date_time, latitude, longitude FROM geotags WHERE ST_Intersects(point, ST_GeogFromText('");
	    
		/* build POLYGON(...) expression */
		sb.append("POLYGON((");
		sb.append(Double.toString(swLng)).append(" ").append(Double.toString(swLat)).append(", ");
		sb.append(Double.toString(neLng)).append(" ").append(Double.toString(swLat)).append(", ");
		sb.append(Double.toString(neLng)).append(" ").append(Double.toString(neLat)).append(", ");
		sb.append(Double.toString(swLng)).append(" ").append(Double.toString(neLat)).append(", ");
		sb.append(Double.toString(swLng)).append(" ").append(Double.toString(swLat)).append("))");
		
		sb.append("'))");
		
		 /* @formatter:on */

		RawSql rawSql = RawSqlBuilder.parse(sb.toString())
				.columnMapping("id", "id").columnMapping("user_id", "user.id")
				.columnMapping("date_time", "date_time")
				.columnMapping("latitude", "latitude")
				.columnMapping("longitude", "longitude").create();

		return find.setRawSql(rawSql);

	}

}
