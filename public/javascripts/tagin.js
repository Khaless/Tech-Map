
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
			
			
			var geocoder = new google.maps.Geocoder();
			
			var notSupportedFn = function() {
				
				if(notSupportedTimer) {
					clearTimeout(notSupportedTimer);
				}
				
				$('#tag-location-span').text('Sorry, we could not determine your location. Please type it in below...');
				$('#tag-location-alt').show();
				$('#tag-location-alt-btn').click(function() {
				    geocoder.geocode( { 'address': $('#tag-location-alt-input').attr('value') }, function(results, status) {
				        if (status == google.maps.GeocoderStatus.OK) {
				        	var str = results[0].formatted_address;
				        	var lat = results[0].geometry.location.lat(); 
				        	var lng = results[0].geometry.location.lng();
				        	$('#tag-location').attr("value", lat + "," + lng);
				        	$('#tag-location-span').html("<b>Address Found:</b> " + str + " [" + lat + "," + lng + "]");
				          } else {
				        	$('#tag-location-span').text('Could not find address. Please try again.');
				          }
				    });
				    return false;
				});
			}
			
			var notSupportedTimer = setTimeout(notSupportedFn, 5000);
			
			$.geolocation.find(function(loc) {
				
				clearTimeout(notSupportedTimer);
				
				var location = {
					'lat' : loc.latitude,
					'lng' : loc.longitude,
					'str' : "GeoLocation Error [ " + loc.latitude + "," + loc.longitude
							+ "]"
				};
				
				$('#tag-location').attr("value", location.lat + "," + location.lng);

				/*
				 * Attempt to use Google Geocoding services to find Country,
				 * Administrative Area and Locality.
				 */
			    var latlng = new google.maps.LatLng(loc.latitude, loc.longitude);
			    geocoder.geocode( { 'latLng': latlng}, function(results, status) {
			        if (status == google.maps.GeocoderStatus.OK) {
			        	location.str = results[0].formatted_address;
			        	$('#tag-location-span').text(location.str);
			          } else {
			        	$('#tag-location-span').text(location.str);
			          }
			    });
			}, notSupportedFn);
		});