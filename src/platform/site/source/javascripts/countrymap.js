(function(global, undefined){

    var countryName = $("#countryName").val();
    var countryCode = $("#countryCode").val();
    var projectType = $("#projectType").val();

    if (projectType == "global") {

        alert("Asdads")
        var map = new L.Map('countryMap', {
            center: new L.LatLng(7.79,21.28), 
            zoom: 1
        });

        map.addLayer(new L.Google('ROADMAP'));

        global.map = map;

    } else if (countryName && countryCode) {  

        var map = new L.Map('countryMap', {
            center: new L.LatLng(countryBounds[countryCode][0], countryBounds[countryCode][1]), 
            zoom: 6
        });
        map.addLayer(new L.Google('ROADMAP'));

        // ugly exposure of a map to the global scope until
        // this block get tidied
        global.map = map;


    } else if (countryCode) {
        var bounds = regionBounds[countryCode];

        var boundary = new L.LatLngBounds(
            new L.LatLng(bounds.southwest.lat, bounds.southwest.lng),
            new L.LatLng(bounds.northeast.lat, bounds.northeast.lng)
        );
        var map = new L.Map('countryMap', {
            maxBounds: boundary
        });

        map.addLayer(new L.Google('ROADMAP'))

        map.fitBounds(boundary);
        map.panInsideBounds(boundary);

        global.map = map;
    } else {
        $('#countryMap').hide();
        $('#countryMapDisclaimer').hide();
    }

})(this)
