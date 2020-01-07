const delayResponseMiddleware = require('../../../middlewares/delay-response.middleware')
const stagingUtils = require('./staging.utils')
const utils = require('../../../utils.js')
const jobsData = require('./jobs.data')

module.exports = function (router) {

  // Get dataset information by ID
  router.get('/staging/datasets/:dataset', [delayResponseMiddleware], function (req, res) {
    try {
      return res.json(stagingUtils.getDataset(req.params.dataset.toUpperCase()))
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

    // Get job information by jobId
    router.get('/staging/jobs/:jobId', [delayResponseMiddleware], function (req, res) {
      try {
        return res.json(stagingUtils.getJob(req.params.jobId.toUpperCase()))
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

    // Get all jobs information
    router.get('/staging/jobs/', [delayResponseMiddleware], function (req, res) {
      return res.json(jobsData)
    })

  // Get dataset information by JobID
  router.get('/staging/jobs/:jobId/datasets', [delayResponseMiddleware], function (req, res) {
    try {
      return res.json(stagingUtils.getDatasets(req.params.jobId.toUpperCase()))
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


  // Get dataset information validationError
  router.get('/staging/datasets/:dataset/validationError', [delayResponseMiddleware], function (req, res) {
    return res.json({
      "url": "http://10.20.0.96:8080/v1/staging/datasets/60/validationError",
      "error": "File does not exist: /user/yyh/datasets/18/E_MV_DSFR02_DSP3\n\tat org.apache.hadoop.hdfs.server.namenode.INodeFile.valueOf(INodeFile.java:71)\n\tat org.apache.hadoop.hdfs.server.namenode.INodeFile.valueOf(INodeFile.java:61)\n\tat org.apache.hadoop.hdfs.server.namenode.FSNamesystem.getBlockLocationsInt(FSNamesystem.java:1828)\n\tat org.apache.hadoop.hdfs.server.namenode.FSNamesystem.getBlockLocations(FSNamesystem.java:1799)\n\tat org.apache.hadoop.hdfs.server.namenode.FSNamesystem.getBlockLocations(FSNamesystem.java:1712)\n\tat org.apache.hadoop.hdfs.server.namenode.NameNodeRpcServer.getBlockLocations(NameNodeRpcServer.java:588)\n\tat org.apache.hadoop.hdfs.protocolPB.ClientNamenodeProtocolServerSideTranslatorPB.getBlockLocations(ClientNamenodeProtocolServerSideTranslatorPB.java:365)\n\tat org.apache.hadoop.hdfs.protocol.proto.ClientNamenodeProtocolProtos$ClientNamenodeProtocol$2.callBlockingMethod(ClientNamenodeProtocolProtos.java)\n\tat org.apache.hadoop.ipc.ProtobufRpcEngine$Server$ProtoBufRpcInvoker.call(ProtobufRpcEngine.java:616)\n\tat org.apache.hadoop.ipc.RPC$Server.call(RPC.java:982)\n\tat org.apache.hadoop.ipc.Server$Handler$1.run(Server.java:2049)\n\tat org.apache.hadoop.ipc.Server$Handler$1.run(Server.java:2045)\n\tat java.security.AccessController.doPrivileged(Native Method)\n\tat javax.security.auth.Subject.doAs(Subject.java:422)\n\tat org.apache.hadoop.security.UserGroupInformation.doAs(UserGroupInformation.java:1698)\n\tat org.apache.hadoop.ipc.Server$Handler.run(Server.java:2043)\n"
      })
  })

  return router
}
