// get an instance of mongoose and mongoose.Schema
var mongoose = require('mongoose');
var Schema = mongoose.Schema;
var crypto = require('crypto'); //crypto module
var jwt = require('jsonwebtoken'); //jsonwebtoken module
var secret = process.env.SECRET;


// set up a mongoose model and pass it using module.exports
var UserSchema = new mongoose.Schema({
  uid: String,
  email: String,
  group: String,
  schedules: [{type: Schema.Types.ObjectId, ref:'Schedule'}]
});

UserSchema.methods.generateJWT = function() { //creating jwt after valid pw

  // set expiration to 60 days
  var today = new Date();
  var exp = new Date(today);
  exp.setDate(today.getDate() + 1);

  return jwt.sign({
    _id: this._id, //payload
    uid: this.uid,
    email:this.email,
    group:this.group,
    exp: parseInt(exp.getTime() / 1000), //expiration time
  }, secret);
};

mongoose.model('User', UserSchema);
