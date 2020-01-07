const delayResponseMiddleware = require('../../middlewares/delay-response.middleware')
const utils = require('../../utils')
const config = require('../../config')

module.exports = function(router) {

  router.post('/users/auth', delayResponseMiddleware, function (req, res) {

    if (req.body.username === "test" && req.body.password === "password") {
      res.json({
        code: 200,
        status: 'success',
        message: ''
      })
    } else {
      res.status(400).json({
        code: 400,
        status: 'error',
        errorMessage: 'Your username or password is incorrect'
      })
    }

  })

  return router
}
