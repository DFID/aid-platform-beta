(function(global, undefined){

    var countryName = $("#countryName").val();
    var countryCode = $("#countryCode").val();

    if (countryName && countryCode) {  

        var point = new google.maps.LatLng(
            countryBounds[countryCode][0], 
            countryBounds[countryCode][1]
        );

        var mapOptions = { mapTypeId: google.maps.MapTypeId.ROADMAP, zoom: 6  };
        var map =  new google.maps.Map(document.getElementById("countryMap"), mapOptions);

        setTimeout(function(){
            google.maps.event.trigger(map, "resize");
            map.setZoom(map.getZoom());
        }, 200)

        map.setCenter(point);

    } else if (countryCode) {
        // this is a regional project
        var bounds = regionBounds[countryCode];
        var northEastBound = new google.maps.LatLng(bounds.northeast.lat, bounds.northeast.lng);
        var southwestBound = new google.maps.LatLng(bounds.southwest.lat, bounds.southwest.lng);
        var bounds = new google.maps.LatLngBounds(southwestBound, northEastBound);
        var mapOptions = { mapTypeId: google.maps.MapTypeId.ROADMAP,  };
        var map =  new google.maps.Map(document.getElementById("countryMap"), mapOptions);

        setTimeout(function(){
            google.maps.event.trigger(map, "resize");
            map.setZoom(map.getZoom());
        }, 200)

        map.fitBounds(bounds);
        map.panToBounds(bounds);
    } else {
        $('#countryMap').hide();
        $('#countryMapDisclaimer').hide();
    }

})(this)
