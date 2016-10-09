// get an instance of mongoose and mongoose.Schema
var mongoose = require('mongoose');
var Schema = mongoose.Schema;

// set up a mongoose model and pass it using module.exports
var stopSchema = new mongoose.Schema({
  type: String,
  name: String,
  gps: [],
  time: Date,
  count : {type: Number, default:0}
  });

stopSchema.methods.upcount = function(cb) {
	this.count += 1;
	this.save(cb);
};

mongoose.model('Stop', stopSchema);
