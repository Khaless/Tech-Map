package controllers;

import java.util.Arrays;
import java.util.Date;
import java.util.Set;

import models.Country;
import models.GeoTag;
import models.Tag;
import models.User;
import play.api.libs.json.JsValue;
import play.api.libs.json.Json;
import play.data.Form;
import play.data.validation.Constraints.Pattern;
import play.data.validation.Constraints.Required;
import play.db.ebean.Transactional;
import play.mvc.Controller;
import play.mvc.Result;

import play.*;
import play.mvc.*;
import views.html.*;


public class Geotag extends Controller {

	public static class TagIn {

		/*
		 * Enterprise ID as submitted by user
		 */
		@Required
		public String enterprise_id;

		/*
		 * Latitude,Longitude string
		 */
		@Required
		@Pattern(value = "[0-9.-]+,[0-9.-]+", message = "A valid location (Lat/Long) is required")
		public String location;

		/*
		 * CSV field of tag_id
		 */
		@Required
		@Pattern(value = "[0-9,]+", message = "A valid set of tags must be supplied")
		public String tags;
		
		public String newtags;

		public Set<Tag> getTagObjects() {
			return Tag.find.where().idIn(Arrays.asList(this.tags.split(",")))
					.findSet();
		}
		
		public double getLatitude() {
			return Double.parseDouble(location.split(",")[0]);
		}
		
		public double getLongitude() {
			return Double.parseDouble(location.split(",")[1]);
		}

	}

	final static Form<TagIn> tagInForm = form(TagIn.class);

	public static Result tagIn() {
		return ok(tagin.render(tagInForm));

	}

	@Transactional
	public static Result doTagIn() {

		Form<TagIn> submittedForm = tagInForm.bindFromRequest();
		if (submittedForm.hasErrors()) {
			return ok(tagin.render(submittedForm));
		} else {

			User user = User.find.where()
					.eq("enterprise_id", submittedForm.get().enterprise_id)
					.findUnique();
			if (user == null) {
				user = new User();
				user.enterprise_id = submittedForm.get().enterprise_id;
				user.save();
			}	

			GeoTag gt = new GeoTag();
			gt.tags = submittedForm.get().getTagObjects();
			gt.user = user;
			gt.latitude = submittedForm.get().getLatitude();
			gt.longitude = submittedForm.get().getLongitude();
			gt.date_time = new Date();
			
			/*
			 * An optimisation -- resolve the country @ tag time.
			 */
			Country country = Country.findCountryByLatLng(gt.latitude, gt.longitude).findUnique();
			if(country != null) {
				gt.country = country;
			}

			/*
			 * Ebean.save() does not work well with ManyToMany associations, so
			 * in addition to .save(), we also call
			 * .saveManyToManyAssociations()
			 */
			gt.save();
			gt.saveManyToManyAssociations("tags");

			flash("success", "You have been Tagged In!");
			
			return ok(tagin.render(submittedForm));
		}
	}
}
