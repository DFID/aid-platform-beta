(function(global, undefined){
    function paintCountryPolygons(countriesData,polygonsData, countryCode) {

        var countryData=countriesData[countryCode];

        var multiVertices = new Array();
        var minLatitude=undefined;
        var maxLatitude=undefined;
        var minLongitude=undefined;
        var maxLongitude=undefined;
        
        
        for (var countryPolygonesDefArrayIndex in polygonsData[countryCode] ){
            var countryPolygoneDefString = polygonsData[countryCode][countryPolygonesDefArrayIndex];
            var verticesDefArray = countryPolygoneDefString.split(" ");
            var vertices = new Array();
            for (var vertexDefStringIndex in verticesDefArray){
                var vertexDefString = verticesDefArray[vertexDefStringIndex].split(",");
                var longitude=Number(vertexDefString[0]);
                var latitude=Number(vertexDefString[1]);
                var latLng=new L.LatLng(latitude,longitude);
                vertices[vertices.length]=latLng;
                
                /* see if we can calulculate the bounds of the country */
                if (minLatitude == undefined || minLatitude > latitude){
                    minLatitude=latitude;
                }
                if (maxLatitude == undefined || maxLatitude < latitude){
                    maxLatitude=latitude;
                }
                if (minLongitude == undefined || minLongitude > longitude){
                    minLongitude=longitude;
                }
                if (maxLongitude == undefined || maxLongitude < longitude){
                    maxLongitude=longitude;
                }
            }
            multiVertices[multiVertices.length]=vertices;
        }
        var multiPolygon = L.multiPolygon(multiVertices,{
            stroke: true, /* draws the border when true */
            color: '#ffffff', /* border color */
            weight: 3, /* stroke width in pixels */
            fill:true,
            fillColor: "#FFFFFF",
            fillOpacity: 0.3
        });
    
        /* center the map on the rectangle where the country fits in */
        var middleLat=minLatitude + ((maxLatitude - minLatitude) /2);
        var middleLng=minLongitude + ((maxLongitude - minLongitude) /2);
        map.fitBounds([new L.LatLng(minLatitude, minLongitude),new L.LatLng(maxLatitude, maxLongitude)]);
        multiPolygon.addTo(map); /* finally addes the polygon to the map */
        

        if ("undefined" != typeof countryData){
            var outerOffset=5;
            var circleUnit=((countryData.projects*5) + outerOffset); /* ((projects * 10) + outerOffset) * 2 */
            var fontSize=(countryData.projects*5);
        
            if (circleUnit > 50){
                circleUnit=50;
                fontSize=20;
            }
            if (circleUnit <= 20){
                circleUnit=20;
                fontSize=15;
            }
        
            var circleX = circleUnit;
            var circleY = circleUnit;
            var circleRadiusInner = (circleUnit) - outerOffset;
            var circleRadiusOuter = ((circleUnit));
            var projectsString = new String(countryData.projects); /* so we can calculate how many pixels the string is */
            var dx = -1 * fontSize * projectsString.length / 4 ;
            var dy = (fontSize )/4;
        
            var greenCircle = L.icon({
                //iconUrl: 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" version="1.1" width="' + circleUnit*2 + 'px" height="' + circleUnit*2 + 'px"><circle cx="' + circleX + '" cy="' + circleY + '" r="' + circleRadiusOuter + '" fill="#2C6367" fill-opacity="0.4"/><circle cx="' + circleX + '" cy="' + circleY + '" r="' + circleRadiusInner + '" stroke="#2C6367" stroke-width="1" fill="white" /><text x="' + circleX + '" y="' + circleY + '" fill="black" stroke="black" stroke-width="1" font-size="' + fontSize + '" dx="' + dx + '" dy="' + dy + '" >' + countryData.projects + '</text></svg>',
            	iconUrl: 'data:image/svg+xml,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20version%3D%221.1%22%20width%3D%22' + circleUnit*2 + 'px%22%20height%3D%22' + circleUnit*2 + 'px%22%3E%3Ccircle%20cx%3D%22' + circleX + '%22%20cy%3D%22' + circleY + '%22%20r%3D%22' + circleRadiusOuter + '%22%20fill%3D%22%232C6367%22%20fill-opacity%3D%220.4%22%2F%3E%3Ccircle%20cx%3D%22' + circleX + '%22%20cy%3D%22' + circleY + '%22%20r%3D%22' + circleRadiusInner + '%22%20stroke%3D%22%232C6367%22%20stroke-width%3D%221%22%20fill%3D%22white%22%20%2F%3E%3Ctext%20x%3D%22' + circleX + '%22%20y%3D%22' + circleY + '%22%20fill%3D%22black%22%20stroke%3D%22black%22%20stroke-width%3D%221%22%20font-size%3D%22' + fontSize + '%22%20dx%3D%22' + dx + '%22%20dy%3D%22' + dy + '%22%20%3E' + countryData.projects + '%3C%2Ftext%3E%3C%2Fsvg%3E',
                iconSize:     [circleUnit*2, circleUnit*2], // size of the icon
                iconAnchor:   [circleUnit, circleUnit] // point of the icon which will correspond to marker's location
            });
            L.marker(new L.LatLng(middleLat,middleLng), {
                icon: greenCircle
            }).addTo(map);
        }
    }


    // creates a new map and centers it somewhere in the indian ocean
    var map = L.map('map',{
        zoomControl:false,
        scrollWheelZoom:false,
        doubleClickZoom:false,
        touchZoom:false,
        boxZoom:false,
        dragging:false
    });

    // creates a tile layer
    L.tileLayer("http://aipalpha.dfid.gov.uk/v2/dfid-towns/{z}/{x}/{y}.png", {
        minZoom: 1,
        maxZoom: 6,
        attribution: ''
    }).addTo(map);





                            // uses the global variable set on the country view
                            paintCountryPolygons(countriesData,polygonsData, countryCode);

})(this)