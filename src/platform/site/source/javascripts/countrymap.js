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
            var southwestBound = new google.maps.LatLng(southwest.lat, northeast.lng);
            var mapCenterPoint = new google.maps.LatLng(mapCenter.lat, mapCenter.lng);

            var bounds = new google.maps.LatLngBounds(southwestBound, northEastBound);
            bounds.extend(mapCenterPoint);

            var mapOptions = { mapTypeId: google.maps.MapTypeId.ROADMAP };
            var map =  new google.maps.Map(document.getElementById("countryMap"), mapOptions);

            map.fitBounds(bounds);
            map.panToBounds(bounds);
        }).fail(function() { console.log( "error" ); });
    } else {
        $('#countryMap').hide();
        $('#countryMapDisclaimer').hide();
    }
})(this)