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
@Table(name="tags")
public class Tag extends Model {

    @Id
    public Long id;
    
    @Constraints.Required
    public String name;
        
    /**
     * Generic query helper for entity Taggable with id Long
     */
    public static Finder<Long,Tag> find = new Finder<Long,Tag>(Long.class, Tag.class); 
    
}

