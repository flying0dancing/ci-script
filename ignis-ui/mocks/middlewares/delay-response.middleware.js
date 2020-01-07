module.exports = function delayResponseMiddleware(req, res, next) {
  const delayParam = req.query.delayResponse ? +req.query.delayResponse : null

  // delay = time (milliseconds)
  let delay = 200

  if(delayParam && delayParam % 1 == 0)
    delay = delayParam

  setTimeout(() => {
    next()
  }, delay)
}
