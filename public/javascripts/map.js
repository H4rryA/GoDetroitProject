var map;
var heatmap;
var click_markers = [];
var rider_markers = [];
var hidemarker = [false,false];

var starttime = new Date(2016,9,9,0,0)

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
    httpGetAsync('/crimes?', parameters, function(response){
      console.log(response);
      addMarker(event.latLng, 10*(response/15), click_markers);
    });
  });

  console.log(starttime);
  setInterval(increaseTime,2000);

  var query = 'select col0, col1 from 1zHCgMcx-VnSvSATgylPUvUidXih50QBHxP0lAGZP limit 1000';
  var request = gapi.client.fusiontables.query.sqlGet({ sql: query });
  request.execute(function(response) {
    onDataFetched(response);
  });
}

function setMapOnAllClick(map) {
  for (var i = 0; i < click_markers.length; i++) {
    click_markers[i].setMap(map);
  }
  click_markers = []
}

function wipeRiderMarkers() {
  console.log('wiped');
  for (var i = 0; i < rider_markers.length; i++) {
    rider_markers[i].setMap(null);
  }
  rider_markers = []
}

function clearMarkers() {
    if(hidemarker[0] == false){
      setMapOnAllClick(null);
      hidemarker[0] = true;
    }
    else {
      setMapOnAllClick(map);
      hidemarker[0] = false;
    }
}

function hideHM() {
  if(hidemarker[1] == false){
    heatmap.setMap(null);
    hidemarker[1] = true;
  }
  else {
    heatmap.setMap(map);
    hidemarker[1] = false;
  }
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
  heatmap = new google.maps.visualization.HeatmapLayer({
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

function addMarker(location, num, markertype) {
  // Add the marker at the clicked location, and add the next-available label
  // from the array of alphabetical characters.
  var marker = new google.maps.Marker({
    position: location,
    label: num,
    map: map
  });
  markertype.push(marker);
}
//http calls for marker points
function httpGetAsync(theUrl, parameters, callback){
    var xmlHttp = new XMLHttpRequest();
    xmlHttp.onreadystatechange = function() {
        if (xmlHttp.readyState == 4 && xmlHttp.status == 200)
            // console.log(xmlHttp.responseText);
            callback(xmlHttp.responseText);
    }
    xmlHttp.open("GET", theUrl+parameters, true); // true for asynchronous
    xmlHttp.send(null);
}

function httpGetRiders() {
  var parameters = 'time='+starttime.toISOString();
  httpGetAsync('/passengerSchedule?', parameters, populateDOM)
}

function populateDOM(stops) {
  stops = JSON.parse(stops);
  for(var j = 0 ; j < stops.length ; j++){
    p = new google.maps.LatLng(stops[j].gps[0], stops[j].gps[1])
    addMarker(p, ""+stops[j].count+"", rider_markers)
  }
}

function increaseTime(){
    starttime = addHours(starttime, 1);
    if(starttime > new Date(2016,9,10,0,0)){
      starttime = new Date(2016,9,9,1,0)
    }
    setTimeout(2000);
    console.log(starttime.toISOString());
    console.log(rider_markers);
    wipeRiderMarkers();
    httpGetRiders();
}

function addHours(date,h){
    date.setHours(date.getHours()+h);
    return date;
}

google.maps.event.addDomListener(window, 'load', loadApi);
