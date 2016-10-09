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
  User.findOne({ uid : req.body.uid }, function (err, user) { //use mongoose to find user in database
      if (err) { res.json(err); }
      if (!user) { //if usr does not exist, create new usr
        console.log('doesnt exist');
        var user1 = new User(
          {
            uid : req.body.uid,
            email : req.body.email,
            group : req.body.group
          }
        );
        user1.save(function (err, user){ //save
          if(err){ return next(err); }
          return res.json({msg:'created new account',token: user.generateJWT()}) //creates a JWT
        });
      }
      else{
        console.log('found usr');
        return res.json({msg:'logged in account', token: user.generateJWT()}) //return the to passpor
      }
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
