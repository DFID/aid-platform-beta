(function(global, undefined){
    var countryName = $("#countryName").val();
    var countryCode = $("#countryCode").val();
    if (countryName && countryCode) {        
        var url = "https://maps.googleapis.com/maps/api/geocode/json?address=" + countryName + "&components=country:" + countryCode + "&sensor=false";
        var geocode = '';
        $.getJSON(url).done(function(response) { 
            geocode = response.results[0];

            var northeast = geocode.geometry.viewport.northeast;
            var southwest = geocode.geometry.viewport.southwest;
            var mapCenter = geocode.geometry.location;

            var northEastBound = new google.maps.LatLng(northeast.lat, northeast.lng);
            var southwestBound = new google.maps.LatLng(southwest.lat, southwest.lng);
            var mapCenterPoint = new google.maps.LatLng(mapCenter.lat, mapCenter.lng);

            createMap(northEastBound, southwestBound, mapCenterPoint);
        }).fail(function() { console.log( "error" ); });
    } else if (countryCode) {
        // this is a regional project
        var bounds = regionBounds[countryCode];

        var northEastBound = new google.maps.LatLng(bounds.northeast.lat, bounds.northeast.lng);
        var southwestBound = new google.maps.LatLng(bounds.southwest.lat, bounds.southwest.lng);

        createMap(northEastBound, southwestBound, '');
    } else {
        $('#countryMap').hide();
        $('#countryMapDisclaimer').hide();
    }

})(this)

function createMap(northEastBound, southwestBound, mapCenterPoint) {
    var bounds = new google.maps.LatLngBounds(southwestBound, northEastBound);

    if (mapCenterPoint) {
       bounds.extend(mapCenterPoint);
    }

    var mapOptions = { mapTypeId: google.maps.MapTypeId.ROADMAP };
    var map =  new google.maps.Map(document.getElementById("countryMap"), mapOptions);

    setTimeout('google.maps.event.trigger(map, "resize");map.setZoom(map.getZoom());', 200);

    map.fitBounds(bounds);
    map.panToBounds(bounds);
}
