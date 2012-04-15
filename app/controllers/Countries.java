package controllers;

import models.Country;
import java.util.List;
import play.*;
import play.mvc.*;
import views.html.*;
import static play.libs.Json.toJson;

public class Countries extends Controller {
  
  public static Result tags(String query) {
	  List<Country> tags = Country.find.where().ilike("name", "%" + query + "%").findList();
	  return ok(toJson(tags));
  }

}