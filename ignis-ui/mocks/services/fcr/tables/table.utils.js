const data = require('./tables.data')

exports.getTables = getTables

function getTables(schema) {
  if(!schema) throw 402

  return data
}
