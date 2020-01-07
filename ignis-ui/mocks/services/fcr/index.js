module.exports = function(router) {
  return [
    require('./tables')(router),
    require('./staging')(router)
  ]
}
