var map;

function loadApi() {
  gapi.client.load('fusiontables', 'v1', initialize);
}

function initialize() {
  var mapDiv = document.getElementById('map');
  map = new google.maps.Map(mapDiv, {
    center: new google.maps.LatLng(42.350900619879376, -83.05313354492188),
    zoom: 11,
    mapTypeId: google.maps.MapTypeId.ROADMAP
  });

  google.maps.event.addListener(map, 'click', function(event) {
    var parameters = 'lat='+event.latLng.lat()+'&long='+event.latLng.lng()+'&rad=1000';
    httpGetAsync('/crimes', parameters, function(response){
      addMarker(event.latLng, map, response);
    });
  });

  var query = 'select col0, col1 from 1m_Ncb4c9pb8j4W0S3-Qudf2cgVFODVac9bZUqsaI limit 1000';
  var request = gapi.client.fusiontables.query.sqlGet({ sql: query });
  request.execute(function(response) {
    onDataFetched(response);
  });
}

function onDataFetched(response) {
  if (response.error) {
    alert('Unable to fetch data. ' + response.error.message +
        ' (' + response.error.code + ')');
  } else {
    drawHeatmap(extractLocations(response.rows));
  }
}

function extractLocations(rows) {
  // Patterns for latitude/longitude in a single field, separated by a space
  // or comma, with optional north/south/east/west orientation
  var numberPattern = '([+-]?\\d+(?:\\.\\d*)?)';
  var latPattern = numberPattern + '\\s*([NS])?';
  var lngPattern = numberPattern + '\\s*([EW])?';
  var latLngPattern = latPattern + '(?:\\s+|\\s*,\\s*)' + lngPattern;
  var northRegexp = /N/i;
  var eastRegexp = /E/i;
  var parseRegexp = new RegExp(latLngPattern, 'i');
  var locations = [];
  for (var i = 0; i < rows.length; ++i) {
    var row = rows[i];
    if (row[0]) {
      var parts = row[0].match(parseRegexp);
      if (parts) {
        var latString = parts[1];
        var latOrientation = parts[2];
        var lngString = parts[3];
        var lngOrientation = parts[4];
        var lat = parseFloat(latString);
        var lng = parseFloat(lngString);
        if (latOrientation) {
          var latAdjust = northRegexp.test(latOrientation) ? 1 : -1;
          lat = latAdjust * Math.abs(lat);
        }
        if (lngOrientation) {
          var lngAdjust = eastRegexp.test(lngOrientation) ? 1 : -1;
          lng = lngAdjust * Math.abs(lng);
        }
        var latLng = new google.maps.LatLng(lat, lng);
        var weight = row[1];
        locations.push({ location: latLng, weight: parseFloat(weight) });
      }
    }
  }
  return locations;
}

function drawHeatmap(locations) {
  var heatmap = new google.maps.visualization.HeatmapLayer({
     dissipating: true,
     gradient: [
       'rgba(102,255,0,0)',
       'rgba(147,255,0,1)',
       'rgba(193,255,0,1)',
       'rgba(238,255,0,1)',
       'rgba(244,227,0,1)',
       'rgba(244,227,0,1)',
       'rgba(249,198,0,1)',
       'rgba(255,170,0,1)',
       'rgba(255,113,0,1)',
       'rgba(255,57,0,1)',
       'rgba(255,0,0,1)'
     ],
     opacity: 0.6,
     radius: 39,
     data: locations
  });
  heatmap.setMap(map);
}

function addMarker(location, map, crime_index) {
  // Add the marker at the clicked location, and add the next-available label
  // from the array of alphabetical characters.
  var marker = new google.maps.Marker({
    position: location,
    label: crime_index,
    map: map
  });
}

google.maps.event.addDomListener(window, 'load', initialize);
//http calls for marker points
function httpGetAsync(theUrl, parameters, callback)
{
    var xmlHttp = new XMLHttpRequest();
    xmlHttp.onreadystatechange = function() {
        if (xmlHttp.readyState == 4 && xmlHttp.status == 200)
            console.log(xmlHttp.responseText);
            callback(xmlHttp.responseText);
    }
    xmlHttp.open("GET", theUrl+'?'+parameters, true); // true for asynchronous
    xmlHttp.send(null);
}

google.maps.event.addDomListener(window, 'load', loadApi);
