const stagingData = require('./staging.data')
const jobsData = require('./jobs.data')

exports.getDataset = getDataset
exports.getDatasets = getDatasets
exports.getJob = getJob

function getDataset(dataset) {
  if(!dataset) throw 402

  return stagingData[0]
}

function getDatasets(jobId) {
  if(!jobId) throw 402

  return stagingData
}

function getJob(jobId) {
  if(!jobId) throw 402

  return jobsData[0]
}
