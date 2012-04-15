
require 'rexml/document'
require 'zipruby'

countries = Hash.new
id = 1

def kml_coords_to_postgis_str(kml_string)
  kml_string.split(" ").map do |triple|
    lng,lat,trash = triple.split(",")
    sprintf "%s %s", lng, lat
  end.join(",")
end

def kml_coords_to_postgis_polygon(kml_string)
  "POLYGON((" + kml_coords_to_postgis_str(kml_string) + "))"
end

def kml_coords_to_postgis_point(kml_string)
  "POINT(" + kml_coords_to_postgis_str(kml_string) + ")"
end

Zip::Archive.open('289717-country.kmz') do |archive|
  archive.fopen(archive.get_name(0)) do |file|
    doc = REXML::Document.new(file.read)
    
    # Country Polygons (Borders)
    doc.elements.each('kml/Document/Folder/S_country') do |country|
      name = country.elements["name"].text
      countries[name] = { :id => id, :name => name, :polygons => [], :centroid => nil }
      id += 1
      key = "Polygon"
      key = "MultiGeometry/Polygon" if country.elements["MultiGeometry"]
      country.each_element(key) do |polygon|
        countries[name][:polygons] <<  kml_coords_to_postgis_polygon(polygon.elements["outerBoundaryIs/LinearRing/coordinates"].text)
      end
    end

    # Country Labels (Centroids)
    doc.elements.each('kml/Document/Folder/Placemark') do |placemark|
      name = placemark.elements["name"].text
      countries[name][:centroid] = kml_coords_to_postgis_point(placemark.elements["Point/coordinates"].text)
    end

  end
end

# Print it out in SQL format

countries.each do |k,v|
  printf "INSERT INTO countries (id, name, centroid) VALUES (%d, '%s', ST_GeogFromText('%s'));\n", v[:id], v[:name], v[:centroid]
  v[:polygons].each do |p|
    printf "INSERT INTO country_polygons (country_id, border) VALUES (%d, ST_GeogFromText('%s'));\n", v[:id], p
  end
end



