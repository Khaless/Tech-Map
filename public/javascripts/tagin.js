$(document).ready(
		function() {

			$("#tag-input").tokenInput("/tags.json?toggle-new=true", {
				theme : "facebook",
				onAdd : function(item) {
					if (item.id == -1) {
						
						/* Create a new tag server side. */
						$.ajax({
							url : "/tags.json",
							data : { name: item.real_name },
							dataType : "json",
							type : "POST",
							success: function(data) { 
								console.log(data);
								
								/* Remove and Re-add this new token */
								$("#tag-input").tokenInput("remove", {real_name: item.real_name});
								$("#tag-input").tokenInput("add", {id: data, name: item.real_name});
								
							}
						});
					}
				},
				onResult : function(results) {
					$.each(results, function(index, value) {
						if (value.id == -1) {
							value.real_name = value.name;
							value.name = "<b>New Tag</b> " + value.name;
						}
					});
					return results;
				}
			});

			$.geolocation.find(function(loc) {

				var location = {
					'lat' : loc.latitude,
					'lng' : loc.longitude,
					'str' : "Unknown [ " + loc.latitude + "," + loc.longitude
							+ "]"
				};

				/*
				 * Attempt to use Google Geocoding services to find Country,
				 * Administrative Area and Locality.
				 */
				$.ajax({
					url : 'http://maps.google.com/maps/geo?q=' + loc.latitude
							+ ',' + loc.longitude
							+ '&output=json&v=2&sensor=false&key=AIzaSyD65KItLCLZDrGMs6mJz3Li3bUR0xgufSo',
					dataType : 'jsonp',
					cache : false,
					success : function(data) {
						if (data.Placemark.length > 0) {
							location.str = data.Placemark[0].address;
						}
						$('#tag-location-span').text(location.str);
						$('#tag-location').attr("value",
								location.lat + "," + location.lng);
					}
				});
			});

		});