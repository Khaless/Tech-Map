package models;

import javax.persistence.Entity;
import javax.persistence.Table;

import play.db.ebean.Model;

import com.avaje.ebean.Query;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;

/**
 * Computer entity managed by Ebean
 */
@Entity
@Table(name = "aggregate_tags")
public class AggregateTag extends Model {

	public Long tag_id;
	public String name;
	public int weight;
	public double latitude;
	public double longitude;

	public static Finder<Long, AggregateTag> find = new Finder<Long, AggregateTag>(
			Long.class, AggregateTag.class);

	public static Query<AggregateTag> findAggregated(double zoomLevel, Integer[] country_ids, Integer[] tag_ids) {
		
		if(zoomLevel <= 0.0d || zoomLevel > 1000.0d) {
			throw new RuntimeException("Unsupported Zoom level");
		}

		String sql = "SELECT tag_id, tag_name, weight," + " ST_X(point),"
				+ " ST_Y(point)" + " FROM aggregate_tags_for_zoom( " 
				+ Double.toString(zoomLevel) + ", "
				+ "ARRAY[" + AggregateTag.join(country_ids, ",") + "]::integer[], "
				+ "ARRAY[" + AggregateTag.join(tag_ids, ",") + "]::integer[]"
				+ ") at";

		RawSql rawSql = RawSqlBuilder.parse(sql)
				.columnMapping("tag_id", "tag_id")
				.columnMapping("tag_name", "name")
				.columnMapping("weight", "weight")
				.columnMapping("ST_Y(point)", "latitude")
				.columnMapping("ST_X(point)", "longitude").create();
		
		return find.setRawSql(rawSql);

	}
	
	static private String join(Integer[] input, String delimiter) {
		StringBuilder sb = new StringBuilder();
		for (Integer value : input) {
			sb.append(Integer.toString(value));
			sb.append(delimiter);
		}
		int length = sb.length();
		if (length > 0) {
			sb.setLength(length - delimiter.length());
		}
		return sb.toString();
	}

}
