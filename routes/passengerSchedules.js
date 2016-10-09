var express = require('express');
var router = express.Router();

var mongoose = require('mongoose');
var Schedule = mongoose.model('Schedule');
var User = mongoose.model('User');

var confsecret = process.env.SECRET
var jwt = require('express-jwt');
var auth = jwt({secret: confsecret});

router.post('/passengerSchedule', auth, function(req, res, next) {

  var newSchedule = new Schedule();
  newSchedule.user = req.user._id;
  newSchedule.transitData = req.body;
  newSchedule.date = new Date();

  newSchedule.save(function (err){ //save
    if(err){ return next(err); }
    return res.json(newSchedule) //creates a JWT
  });
});

router.get('/passengerSchedule', auth, function(req, res, next) {
    User.findById(req.user._id).populate('schedules').exec(function(err, usr) {
      if(err){return next(err);}
      res.json(usr);
    })
})

module.exports = router;
