var markersArray = [];
var zoomLevel = -1;
var map;

function updateTagMap_callback(data) {

	var bylatlng = {};
	var max_weight = 1;

	/*
	 * Remove all old info windows & redraw.
	 */
	if (markersArray) {
		for (i in markersArray) {
			markersArray[i].setMap(null);
		}
		markersArray.length = 0;
	}

	$.each(data, function(i, e) {

		if (e.weight > max_weight) {
			max_weight = e.weight;
		}

		if (!bylatlng[e.latitude + "_" + e.longitude]) {
			bylatlng[e.latitude + "_" + e.longitude] = [];
		}

		bylatlng[e.latitude + "_" + e.longitude].push(e);
	});

	$.each(bylatlng, function(i, e) {

		var str = $.map(e, function(j) {
			var fontSize = (150.0*(1.0+(1.5*j.weight-max_weight/2)/max_weight))+"%";
			return "<span style='font-size:" + fontSize + "'>" + j.name + "</span>";
		}).join(" ");	

		var sw = new google.maps.LatLng(e[0].latitude - calculateZoomLevel()/4, e[0].longitude - calculateZoomLevel()/2); //sw
		var ne = new google.maps.LatLng(e[0].latitude + calculateZoomLevel()/4, e[0].longitude + calculateZoomLevel()/2); //ne
		var nw = new google.maps.LatLng(e[0].latitude + calculateZoomLevel()/4, e[0].longitude - calculateZoomLevel()/2); //nw
		var center = new google.maps.LatLng(e[0].latitude, e[0].longitude ); // center
		
		/*var rect = new google.maps.Rectangle();
		rect.setOptions({ map: map, bounds: new google.maps.LatLngBounds(sw, ne)});
		markersArray.push(rect);
		markersArray.push(new google.maps.Marker({position: ne, map: map, title: "ne"}));
		markersArray.push(new google.maps.Marker({position: sw, map: map, title: "sw"}));
		
		markersArray.push(new google.maps.Marker({position: center, map: map, title: "ll"}));
		*/

		var marker = new TxtOverlay( nw , str, "maps-tag-overlay", map )
		markersArray.push(marker);

	});

}

function calculateZoomLevel() {
	
	var d = 360 / Math.pow(2, map.getZoom());
	console.log(d);
	return d;
	
	// Calculate d, the viewport Horizontal distance in Meters (Approximtion)
	//var R = 6371; //km
        var b = map.getBounds();
        var ne = b.getNorthEast();
	var sw = b.getSouthWest();
	//var x = (ne.lng() - sw.lng()) * Math.cos((sw.lat() + ne.lat())/2);
	//var y = (ne.lat() - sw.lat());
	//var d = Math.sqrt(x*x + y*y) * R;

	var d = -1;
	
	if(ne.lng() < 0 && sw.lng() > 0) { // left of meridian
		d = 180.0 - sw.lng() + 180 + ne.lng();
	} else if (ne.lng() > 0 && sw.lng() > 0) { // right of meridian
		d = ne.lng() - sw.lng();
 	}
	else if (ne.lng() < 0 && sw.lng() < 0) { // we have wrapped around the earth!
		d = 360;
	}

	d = d/4;

        console.log(d);
	return d;
}

function updateMapIfZoomLevelChanged() {
	setTimeout(function() {
	var newZoomLevel = calculateZoomLevel();
	if (newZoomLevel != zoomLevel) {
		zoomLevel = newZoomLevel;
	}
	updateTagMap();
	},1000);
}

function updateTagMap() {

	var tag_ids = $.map($("#tag-input").tokenInput("get"), function(e) {
		return e.id;
	});
	var country_ids = $.map($("#country-input").tokenInput("get"), function(e) {
		return e.id;
	});

	$.ajax({
		url : "/aggregated_tags.json",
		data : JSON.stringify({
			zoom: zoomLevel,
			tag_ids : tag_ids,
			country_ids : country_ids
		}),
		dataType : "json",
		type : "POST",
		headers : {
			'Content-Type' : 'application/json'
		},
		success : updateTagMap_callback
	});
}

$(document).ready(function() {

	$("#tag-input").tokenInput("/tags.json", {
		theme : "facebook",
		onAdd : function(item) {
			updateTagMap();
		},
		onDelete : function(item) {
			updateTagMap();
		}
	});

	$("#country-input").tokenInput("/countries.json", {
		theme : "facebook",
		onAdd : function(item) {
			updateTagMap();
		},
		onDelete : function(item) {
			updateTagMap();
		}
	});

	zoomLevel = 4;
	map = new google.maps.Map(document.getElementById('map'), {
		zoom : 4,
		mapTypeId : google.maps.MapTypeId.ROADMAP
	});

	google.maps.event.addListener(map, 'zoom_changed', updateMapIfZoomLevelChanged);

	/*
	 * Attempt to centre the map on their location.
	 */
	$.geolocation.find(function(loc) {
		/* Geolocation found via HTML5 API */
		map.setCenter(new google.maps.LatLng(loc.latitude, loc.longitude));
	}, function() {
		/* Geolocation HTML5 API Not Supported */
		map.setCenter(new google.maps.LatLng(60, 105));
	});

	updateTagMap();
});
