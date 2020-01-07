const path = require('path')
const express = require('express')
const proxy = require('http-proxy-middleware')
const opn = require('opn')
const app = express()
const argv = require('minimist')(process.argv.slice(2))

const angularCliConfig = require('./.angular-cli.json')
const outDir = angularCliConfig.apps[0].outDir
const indexHtmlFile = angularCliConfig.apps[0].index
const indexPath = outDir + '/' + indexHtmlFile

const port = argv.port || argv.p || 4201
const mockPath = argv.mockPath || '/mocks/api/'
const mockPort = argv.mockPort || 3000
const shouldOpen = argv.o || argv.open

const proxyOptions = {
  target: `http://localhost:${mockPort}`,
  pathRewrite: {
    [`^${mockPath}`]: '/'
  }
}

app.use(mockPath, proxy(proxyOptions))

app.use(express.static(path.join(__dirname, outDir)))

app.get('*', (req, res) => res.sendFile((path.join(__dirname, indexPath))))

app.listen(port, () => {
  console.log(`Static server listening on port ${port}`)

  if (shouldOpen) opn(`http://localhost:${port}`)
})
