package controllers;

import java.util.Arrays;
import java.util.Date;
import java.util.Set;

import models.GeoTag;
import models.Tag;
import models.Country;
import models.User;
import play.data.Form;
import play.data.validation.Constraints.Pattern;
import play.data.validation.Constraints.Required;
import play.db.ebean.Transactional;
import play.mvc.Controller;
import play.mvc.Result;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlQuery;

import play.*;
import play.mvc.*;
import views.html.*;

public class Application extends Controller {

	public static Result index() {
		return ok(index.render());
	}
	
	public static Result about() {
		return ok(about.render());
	}

}