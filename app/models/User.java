package models;

import play.db.ebean.Model;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import play.data.validation.Constraints;
import com.avaje.ebean.Page;

/**
 * Computer entity managed by Ebean
 */
@Entity 
@Table(name="users")
public class User extends Model {

    @Id
    public Long id;
    
    @Constraints.Required
    public String enterprise_id;
        
    /**
     * Generic query helper for entity User with id Long
     */
    public static Finder<Long,User> find = new Finder<Long,User>(Long.class, User.class); 
        
}

