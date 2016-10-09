// get an instance of mongoose and mongoose.Schema
var mongoose = require('mongoose');
var Schema = mongoose.Schema;
var crypto = require('crypto'); //crypto module
var jwt = require('jsonwebtoken'); //jsonwebtoken module
var secret = process.env.SECRET;


// set up a mongoose model and pass it using module.exports
var scheduleSchema = new mongoose.Schema({
  user : {type: Schema.Types.ObjectId, ref: 'User'},
  transitData : [],
  date: Date
});


mongoose.model('Schedule', scheduleSchema);
