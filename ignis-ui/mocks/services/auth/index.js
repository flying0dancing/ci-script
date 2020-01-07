module.exports = function(router) {
  return [
    require('./auth.route')(router)
  ]
}
