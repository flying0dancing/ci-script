const delayResponseMiddleware = require('../../../middlewares/delay-response.middleware')
const tableUtils = require('./table.utils')
const utils = require('../../../utils.js')
const multer  = require('multer');
const upload = multer({ dest: './uploads' })

module.exports = function (router) {

  // Get details on a dataset schema
  router.get('/tables/:schema', [delayResponseMiddleware], function (req, res) {
    try {
      return res.json(tableUtils.getTables(req.params.schema.toUpperCase()))
    }
    catch(e) {
      switch(e) {
        case 402:
          res.status(402).json(utils.json402)
          break;

        case 404:
          res.status(404).json(utils.json404)
          break;
      }
    }
  })

   // Create new dataset schema
   router.post('/tables/', [delayResponseMiddleware], function (req, res) {
    if (req.body){
      return res.status(200)
    } else {
      return res.status(402)
    }
  })

  router.post('/tables/file', upload.single('file'), function (req, res) {
    res.status(200).json({
      success: true
    });
  });

  return router
}
