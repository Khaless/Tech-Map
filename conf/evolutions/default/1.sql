
# Initial Schema

# --- !Ups

CREATE SEQUENCE users_seq;
CREATE TABLE users (
  id INTEGER PRIMARY KEY DEFAULT NEXTVAL('users_seq'),
  enterprise_id VARCHAR(64)
) WITH ( OIDS=FALSE );

CREATE SEQUENCE tags_seq;
CREATE TABLE tags (
  id INTEGER PRIMARY KEY DEFAULT NEXTVAL('tags_seq'),
  name VARCHAR(32) NOT NULL UNIQUE
) WITH ( OIDS=FALSE );

CREATE SEQUENCE countries_seq;
CREATE TABLE countries (
  id INTEGER PRIMARY KEY DEFAULT NEXTVAL('countries_seq'),
  name VARCHAR(255) NOT NULL,
  centroid GEOGRAPHY(POINT,4326)
) WITH ( OIDS=FALSE );

CREATE INDEX countries_name_idx ON countries USING BTREE (name);

CREATE SEQUENCE country_polygons_seq;
CREATE TABLE country_polygons (
  country_id INT NOT NULL, 
  border_id INTEGER DEFAULT NEXTVAL('country_polygons_seq'),
  border GEOGRAPHY(POLYGON,4326),
  PRIMARY KEY(country_id, border_id),
  FOREIGN KEY (country_id) REFERENCES countries(id)
) WITH ( OIDS=FALSE );

CREATE SEQUENCE geotags_seq;
CREATE TABLE geotags (
  id INTEGER PRIMARY KEY DEFAULT NEXTVAL('geotags_seq'),
  user_id INT NOT NULL,
  date_time TIMESTAMP WITH TIME ZONE,
  latitude DOUBLE PRECISION,
  longitude DOUBLE PRECISION,
  country_id INT,
  point GEOGRAPHY(POINT,4326),
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (country_id) REFERENCES countries(id)
) WITH ( OIDS=FALSE );

CREATE TABLE geotags_tags (
  geotag_id INT NOT NULL,
  tag_id INT NOT NULL, 
  PRIMARY KEY(geotag_id, tag_id),
  FOREIGN KEY (geotag_id) REFERENCES geotags(id),
  FOREIGN KEY (tag_id) REFERENCES tags(id)  
) WITH ( OIDS=FALSE );

CREATE INDEX geotags_point_gix ON geotags USING GIST (point);
                     
# --- !Downs

SET CONSTRAINTS ALL DEFERRED;

DROP TABLE IF EXISTS geotags_tags CASCADE;
DROP TABLE IF EXISTS geotags CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS tags CASCADE;
DROP TABLE IF EXISTS countries CASCADE;
DROP TABLE IF EXISTS country_polygons CASCADE;

DROP SEQUENCE IF EXISTS users_seq;
DROP SEQUENCE IF EXISTS tags_seq;
DROP SEQUENCE IF EXISTS geotags_seq;
DROP SEQUENCE IF EXISTS countries_seq;
DROP SEQUENCE IF EXISTS country_polygons_seq;


