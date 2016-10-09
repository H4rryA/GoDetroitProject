var express = require('express');
var router = express.Router();

var mongoose = require('mongoose');
var Schedule = mongoose.model('Schedule');
var Stop = mongoose.model('Stop');
var User = mongoose.model('User');

var confsecret = process.env.SECRET
var jwt = require('express-jwt');
var auth = jwt({secret: confsecret});

router.post('/passengerSchedule', auth, function(req, res, next) {
  var newSchedule = new Schedule();
  var astop = new Stop({
    type: 'arrival',
    name: req.body.arrival_stop.name,
    gps: [req.body.arrival_stop.location.lat, req.body.arrival_stop.location.lng],
    time: new Date(req.body.arrival_time.value * 1000),
  })
  var bstop = new Stop({
    type: 'arrival',
    name: req.body.departure_stop.name,
    gps: [req.body.departure_stop.location.lat, req.body.departure_stop.location.lng],
    time: new Date(req.body.departure_time.value * 1000),
  })

  findbustop = function(instop, cb){
    console.log(instop.name);
    Stop.findOne({"name" : instop.name, "time": instop.time},
    function(err, stop) {
      if(!stop){
        var sstop = new Stop()
        sstop = instop
        sstop.save(function(error, rstop) {
          newSchedule.stops.push(rstop)
          console.log("created new");
          cb;
        })
      }
      else {
        newSchedule.stops.push(stop);
        console.log("add to old");
        stop.upcount(cb);
      }
    })
  }
  finish = function() {
    newSchedule.route = req.body.headsign;

    newSchedule.user = req.user._id;
    newSchedule.date = new Date();

    newSchedule.save(function (err){ //save
      if(err){ return next(err); }
      console.log('here4')
      return res.json(newSchedule) //creates a JWT
    });
  }

  findbustop(astop, findbustop(bstop, finish()));
})

//input date + 10 min
//input date - 10 min

router.get('/passengerSchedule', function(req, res, next) {
    t1 = new Date(req.query.time);
    t2 = new Date(req.query.time)
    t2 = t2.setHours(t1.getHours()+1);
    console.log(t1)
    console.log(t2)
    Stop.find({"time" : {$gte: t1, $lt: t2}}).exec(function(err, stp) {
      if(err){return next(err);}
      res.json(stp);
    })
})

module.exports = router;
