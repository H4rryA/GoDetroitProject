var passport = require('passport');
var LocalStrategy = require('passport-local').Strategy;
var mongoose = require('mongoose');
var User = mongoose.model('User');

passport.use(new LocalStrategy(
  function(uid, password, done) {
    User.findOne({ uid: uid }, function (err, user) { //use mongoose to find user in database
      if (err) { return done(err); }
      if (!user) {
        return done(null, false, { message: 'Incorrect uid.' });
      }
      if (!user.validPassword(password)) {
        return done(null, false, { message: 'Incorrect password.' });
      }
      return done(null, user); //return the to passport
    });
  }
));
