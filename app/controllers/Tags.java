package controllers;

import static play.libs.Json.toJson;

import java.util.ArrayList;
import java.util.List;

import models.AggregateTag;
import models.Tag;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;

import play.db.ebean.Transactional;
import play.mvc.Controller;
import play.mvc.Result;

import com.avaje.ebean.ExpressionList;

public class Tags extends Controller {

	public static Result tagsByName(String query) {
		List<Tag> tags = Tag.find.where().ilike("name", "%" + query + "%")
				.findList();
		
		/*
		 * Hack to get easy, in-line additions of new tags.
		 */
		if (form().bindFromRequest().get("toggle-new") != null) {
			Tag nt = new Tag();
			nt.name = query;
			nt.id = -1L;
			tags.add(nt);
		}
		
		return ok(toJson(tags));
	}

	@Transactional
	public static Result addTag() {
		
		Tag newTag = new Tag();
		//newTag.id = Tag.find.nextId();
		newTag.name = form().bindFromRequest().get("name");
		newTag.save();
		return ok(toJson(newTag.id));
	}

	public static Result aggregatedTags() {
		
		JsonNode json = request().body().asJson();
		if (json == null) {
			return badRequest("Expecting Json data");
		}

		ArrayList<Integer> tag_ids = new ArrayList<Integer>();	
		for(JsonNode node : (ArrayNode) request().body().asJson().findValues("tag_ids").get(0)) {
			tag_ids.add(node.getIntValue());
		}
		
		ArrayList<Integer> country_ids = new ArrayList<Integer>();	
		for(JsonNode node : (ArrayNode) request().body().asJson().findValues("country_ids").get(0)) {
			country_ids.add(node.getIntValue());
		}
		
		double zoomLevel = request().body().asJson().path("zoom").asDouble(1.0d);
			
		ExpressionList<AggregateTag> el = AggregateTag.findAggregated(zoomLevel).where();
		
		if (tag_ids.size() > 0) {
			el = el.in("tag_id", tag_ids);
		}
		
		if (country_ids.size() > 0) {
			el = el.in("country_id", country_ids);
		}

		List<AggregateTag> tags = el.findList();
		return ok(toJson(tags));
	}

}