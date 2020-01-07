module.exports = function(router) {
  return [
    require('./auth')(router),
    require('./fcr')(router)
  ]
}
