var express = require('express');
var router = express.Router();

var mongoose = require('mongoose');
var Schedule = mongoose.model('Schedule');
var User = mongoose.model('User');

var confsecret = process.env.SECRET
var jwt = require('express-jwt');
var auth = jwt({secret: confsecret});

router.post('/passengerSchedule', auth, function(req, res, next) {
  console.log('here1');
  var newSchedule = new Schedule();
  var astop = {
    type: 'arrival',
    name: req.body.transitData.arrival_stop.name,
    gps: [req.body.transitData.arrival_stop.location.lat, req.body.arrival_stop.location.lng],
    time: new Date(req.body.transitData.arrival_time.value * 1000)
  }

  newSchedule.stops.push(astop);
  var dstop = {
    type: 'departure',
    name: req.body.transitData.departure_stop.name,
    gps: [req.body.transitData.departure_stop.location.lat, req.body.departure_stop.location.lng],
    time: new Date(req.body.transitData.departure_time.value * 1000)
  }
  newSchedule.stops.push(dstop);

  newSchedule.route = req.body.transitData.headsign;

  newSchedule.user = req.user._id;
  newSchedule.date = new Date();

  newSchedule.save(function (err){ //save
    if(err){ return next(err); }
    console.log('here2')
    return res.json(newSchedule) //creates a JWT
  });
});

router.get('/passengerSchedule', auth, function(req, res, next) {
    Schedule.find().exec(function(err, usr) {
      if(err){return next(err);}
      res.json(usr);
    })
})

module.exports = router;
