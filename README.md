Technology Map
==============

The Technology Map application is designed to allow users to check in the technologies they are working with at any location on the globe. 

Anyone who is interested can then explore the tag map interactively, filtering by zoom level, countries or technologies to see which technologies are in use and where they are being used. For example, if somebody was interested in seeing where we used "github", they could filter by selecting only this technology from the map view and zooming out to see the entire world. It's likely that since git hub is used around the world it would be represented by a tag in the middle of the world. The user could then zoom into a country, and at the finer zoom level the tag would then appear over the cities in that country where "github" is used. 

The application is also aware of country borders, so you could specifically search for a set of technologies in a set of countries. e.g. "SAP", "DB2" & "AIX" in "Australia", "United Kingdom" & "France".

### Technologies
This application is built on top of the Play 2.0 framework and makes use of the PostGIS PostgreSQL extension for spatial data along with jQuery and some jQuery plugins for client side behaviour and a Bootstrap based layout.

### Libraries & Frameworks
http://playframework.org/                    => Apache License
http://loopj.com/jquery-tokeninput/          => MIT License 
http://code.google.com/p/jquery-geolocation/ => MIT License
http://twitter.github.com/bootstrap/         => Apache License

### Credits
The following articles and blog posts assisted in building this application.

PostGIS Geometry Information:
http://workshops.opengeo.org/postgis-intro/geography.html

Country Border Information: 
http://www.gelib.com/world-borders.htm

Spacial Clustering with PostGIS:
http://gis.stackexchange.com/questions/11567/spatial-clustering-with-postgis

PostGis & OpenShift:
https://www.redhat.com/openshift/community/blogs/time-for-a-spatial-power-up-openshift-postgis

Deploying & Managing Postgres on Openshift: 
https://www.redhat.com/openshift/community/blogs/deploying-and-managing-postgresql-on-openshift

Play 2.0 and Openshift Quick Start Project & Guide:
https://github.com/opensas/openshift-play2-java-quickstart

Custom text on Google Maps API v3:
http://stackoverflow.com/questions/3953922/is-it-possible-to-write-custom-text-on-google-maps-api-v3

Plotting country borders onto google maps:
http://facstaff.unca.edu/mcmcclur/GoogleMaps/EncodePolyline/ http://blog.newsplore.com/2009/03/01/political-boundaries-overlay-google-maps-2

How to Deploy
=============

Create a new OpenShift App
```sh
    rhc-create-app -a app-name -t diy-0.1 -l email@example.org
    cd app-name
```

Add my Github (or your cloned repo) as Upstream & Pull from it:
    git remote add upstream -m master https://Khaless@github.com/Khaless/Tech-Map.git
    git pull -s recursive -X theirs upstream master

Build application
    play stage

Remove target/ directory from git ignore and add all changes and target/ dir
    vim .gitgnore (and remove target/)
    git add .gitignore
    git add target
    git commit -m "Commit for OpenShift"

Add postgres to the application
    rhc-ctl-app -a app-name -e add-postgresql-8.4

SSH into your instance:
    ssh id@app-name-domain.rhcloud.com

Run the following commands to setup PostGis:
    psql -d db-name -c "create language plpgsql;"
    psql -d db-name -f /usr/share/pgsql/contrib/postgis-64.sql
    psql -d db-name -f /usr/share/pgsql/contrib/spatial_ref_sys.sql

Push to OpenShift & Watch it deploy (this is wrapped by the openshift_deploy script).
Note: make sure you have play framework configured in your PATH
    bash openshift_deploy
