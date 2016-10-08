var express = require('express');
var router = express.Router();

var passport = require('passport');
var confsecret = process.env.SECRET//require('../appconfig.js').secret; //secret, will change to use env.var
var jwt = require('express-jwt');
var auth = jwt({secret: confsecret, /*userProperty: 'payload'*/});

var mongoose = require('mongoose');
var User = mongoose.model('User');

var validateUserGroup = require('./validation').validateUserGroup;

router.post('/register', function(req, res, next){ //handles a request of post to /register
  if(!req.body.uid || !req.body.email){ //check all fields are filled
    return res.status(400).json({message: 'Please fill out all fields'});
  }
  var user = new User();
  user.uid = req.body.uid;
  user.email = req.body.email;
  user.group = req.body.group;

  user.save(function (err){ //save
    if(err){
      console.log('err' + err);
      return res.send({token: 'duplication'});
    }
    console.log('all good');
    return res.json({token: user.generateJWT()}) //creates a JWT
  });
});

router.post('/login', function(req, res, next){
  if(!req.body.uid /*|| !req.body.password*/){ //checking fields
    return res.status(400).json({message: 'Please fill out all fields'});
  }

  User.findOne({ uid: req.body.uid }, function (err, user) { //use mongoose to find user in database
    if (err) { return done(err); }
    if (!user) {
      res.json({ message: 'Incorrect username.' });
    }
    return res.json({token: user.generateJWT()}) //return the to passport
  });
});
router.get('/allusers', auth, function(req, res, next) { //get all user
      console.log(req.user);
      validateUserGroup(req, res, "admin", function() { //validate user as admin
        User.find({}, function (err, user){
        res.json(user);
        })
      })
})

router.delete('/delete', auth, function(req, res, next) { //wipes DB
    validateUserGroup(req, res, "admin", function() { //validate user as admin
      User.remove({}, function(err) {
        console.log('collection removed');
        res.json();
      })
    })
})

module.exports = router;
