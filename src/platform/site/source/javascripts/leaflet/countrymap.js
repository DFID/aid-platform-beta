(function(global, undefined){
    // Configuration items which could be moved to CMS
    var mapDataUrlTemplate = 'http://{s}.tile2.opencyclemap.org/transport/{z}/{x}/{y}.png';
    var initialZoomLevel = 5;
    var mapTileSourceSubdomains = 'abc';

    var map = L.map('countryMap').setView(countryBounds[$("#countryCode").val()], initialZoomLevel);
    L.tileLayer(mapDataUrlTemplate, {
        attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors',
        subdomains : mapTileSourceSubdomains
    }).addTo(map);
})(this)