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
		
		var marker = new TxtOverlay( new google.maps.LatLng(e[0].latitude, e[0].longitude ) , str, "maps-tag-overlay", map )
		markersArray.push(marker);

	});

}

function calculateZoomLevel() {
	var zoomLevel = map.getZoom();
	switch(zoomLevel) {
		case 0:
		case 1:
			return 360;
		case 2:
			return 150;
		case 3:
		case 4:
			return 50;
		case 5:
			return 30;
		case 6: 
			return 10;
		case 7:
			return 5;
		default: 
			return 1; 
	}
}

function updateMapIfZoomLevelChanged() {
	var newZoomLevel = calculateZoomLevel();
	if (newZoomLevel != zoomLevel) {
		zoomLevel = newZoomLevel;
	}
	updateTagMap();
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