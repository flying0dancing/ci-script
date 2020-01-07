module.exports = function(router) {
  return [
    require('./staging.route')(router)
  ]
}
