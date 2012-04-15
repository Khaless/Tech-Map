var markersArray = [];

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
			return j.name;
		}).join(", ");

		var marker = new google.maps.InfoWindow({
			map : map,
			position : new google.maps.LatLng(e[0].latitude, e[0].longitude),
			content : str
		});
		markersArray.push(marker);

	});

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

	map = new google.maps.Map(document.getElementById('map'), {
		zoom : 4,
		mapTypeId : google.maps.MapTypeId.ROADMAP
	});

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