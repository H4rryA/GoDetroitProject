// In the following example, markers appear when the user clicks on the map.
// Each marker is labeled with a single alphabetical character.
var labels = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
var labelIndex = 0;

function initialize() {
  var me = { lat: 42.34, lng: -83.06 };
  var map = new google.maps.Map(document.getElementById('map'), {
    zoom: 12,
    center: me
  });

  // This event listener calls addMarker() when the map is clicked.
  google.maps.event.addListener(map, 'click', function(event) {
    var parameters = 'lat='+event.latLng.lat()+'&long='+event.latLng.lng()+'&rad=1000';
    httpGetAsync('/crimes', parameters, function(response){
      addMarker(event.latLng, map, response);
    });
  });
  // Add transit Layer


  var layer = new google.maps.FusionTablesLayer({
    query: {
      select: 'col0, col1',
      from: '1m_Ncb4c9pb8j4W0S3-Qudf2cgVFODVac9bZUqsaI'
    },
    heatmap: {
      enabled: true,
      radius:49,
      opacity:.1
    }
  });

  layer.setMap(map);
}

// Adds a marker to the map.
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
