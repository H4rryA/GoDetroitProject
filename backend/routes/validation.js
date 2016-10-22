var validation = module.exports = {};

validation.validateUserGroup = function(req, res, auth_group, callback) { //validaton function
    if(req.user.group == auth_group)
      callback();
    else
      res.json("no auth");
}
