var map;

function loadApi() {
  gapi.client.load('fusiontables', 'v1', initialize);
}

function initialize() {
  var isMobile = (navigator.userAgent.toLowerCase().indexOf('android') > -1) ||
    (navigator.userAgent.match(/(iPod|iPhone|iPad|BlackBerry|Windows Phone|iemobile)/));
  if (isMobile) {
    var viewport = document.querySelector("meta[name=viewport]");
    viewport.setAttribute('content', 'initial-scale=1.0, user-scalable=no');
  }
  var mapDiv = document.getElementById('map');

  map = new google.maps.Map(mapDiv, {
    center: new google.maps.LatLng(42.365073378104945, -83.07137716674805),
    zoom: 12,
  });

  google.maps.event.addListener(map, 'click', function(event) {
    var parameters = 'lat='+event.latLng.lat()+'&long='+event.latLng.lng()+'&rad=500';
    httpGetAsync('/crimes', parameters, function(response){
      addMarker(event.latLng, map, response);
    });
  });

  var query = 'select col0, col1 from 1zHCgMcx-VnSvSATgylPUvUidXih50QBHxP0lAGZP limit 1000';
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
  var locations = [];
  for (var i = 0; i < rows.length; ++i) {
    var row = rows[i];
    if (row[0]) {
      var lat = row[0];
      var lng = row[1];
      if (lat && lng && !isNaN(lat) && !isNaN(lng)) {
        var latLng = new google.maps.LatLng(lat, lng);
        locations.push(latLng);
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
     radius: 30,
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
