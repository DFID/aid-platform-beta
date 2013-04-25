(function(global, undefined){

    function buildMarkerPopupHtml(location) {
        return [
            "<div class='location-popup'>",
                "<div class='row'>", 
                    "<div class='four columns location-label'> Name </div>", 
                    "<div class='eight columns'>", location.name, "</div>",
                "</div>",
                 "<div class='row'>", 
                    "<div class='four columns location-label'> Precision </div>", 
                    "<div class='eight columns'>", location.precision, "</div>",
                "</div>",
                "<div class='row'>", 
                    "<div class='four columns location-label'> Type </div>", 
                    "<div class='eight columns'>", location["type"], "</div>",
                "</div>",
            "</div>"
        ].join("")
    }

    var countryName = $("#countryName").val();
    var countryCode = $("#countryCode").val();
    var projectType = $("#projectType").val();


    var map;

    if (projectType == "global") {

        map = new L.Map('countryMap', {
            center: new L.LatLng(7.79,21.28), 
            zoom: 1
        });

        map.addLayer(new L.Google('ROADMAP'));


    } else if (countryName && countryCode) {  

        map = new L.Map('countryMap', {
            center: new L.LatLng(countryBounds[countryCode][0], countryBounds[countryCode][1]), 
            zoom: 6
        });
        map.addLayer(new L.Google('ROADMAP'));


    } else if (countryCode) {
        var bounds = regionBounds[countryCode];

        var boundary = new L.LatLngBounds(
            new L.LatLng(bounds.southwest.lat, bounds.southwest.lng),
            new L.LatLng(bounds.northeast.lat, bounds.northeast.lng)
        );

        map = new L.Map('countryMap', {
            maxBounds: boundary
        });

        map.addLayer(new L.Google('ROADMAP'))

        map.fitBounds(boundary);
        map.panInsideBounds(boundary);

    } else {
        $('#countryMap').hide();
        $('#countryMapDisclaimer').hide();
    }

    // create the geopoints if any are defined
    if(map && global.locations) {

        var markers = new L.MarkerClusterGroup({ 
            spiderfyOnMaxZoom: false, 
            showCoverageOnHover: false,
            iconCreateFunction: function(cluster) {
                var count = cluster.getChildCount();
                var additional = ""
                if(count > 99) {
                    count = "+";
                    additional = "large-value";
                }

                return new L.DivIcon({ html: '<div class="marker cluster ' + additional + '">' + count+ '</div>' });
            } 
        });

        for(var i = 0; i < locations.length; i++){
            var location = locations[i]
            var latlng   = new L.LatLng(location.latitude, location.longitude)
            var marker   = new L.Marker(latlng, { title: location.name });

            marker.bindPopup(buildMarkerPopupHtml(location))
            markers.addLayer(marker);
        }

        map.addLayer(markers);
    }

})(this)
