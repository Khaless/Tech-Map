package controllers;

import play.*;
import play.mvc.*;
import views.html.*;

public class Map extends Controller {
  
  public static Result index() {
    return ok(map.render());
  }
 
}