// https://data.detroitmi.gov/resource/i9ph-uyrp.json?$where=within_circle(location, 42.39, -83.13, 1000)&$select=count(caseid)
var callsocrata = module.exports = {};
var request = require('request');

callsocrata.callsocrata = function(lat, long, rad, date_range,callback) {
  $today = new Date();
  $starting = new Date($today);
  $starting.setDate($today.getDate() - date_range);
  console.log(lat + ", "+long);

  var options = {
    'url': 'https://data.detroitmi.gov/resource/i9ph-uyrp.json', //
    'qs': {
      '$where' : "within_circle(location, "+lat+", "+long+", "+rad+") " +
      "AND offensedescription in('THEFT', 'ROBBERY', 'INTIMIDATION', 'HOMICIDE', 'SHOOTING', 'ASSAULT', 'BURGLARY')" +
      "AND incidentdate between '"+$starting.toISOString().slice(0, 19)+"' and '"+$today.toISOString().slice(0, 19)+"'",

      '$select' : 'count(caseid)',//setting parameters, counting cases only
      '$$app_token' : process.env.SOCRATA_KEY,
    }
  }

  request.get(options, function (error, response, body) {
    if (!error && response.statusCode == 200) {
      var j = JSON.parse(body);
      console.log(j);
      j = j[0]['count_caseid']
      callback(j);
    }
  })
}
