const express = require('express')
const path = require('path')
const app = express()
const bodyParser = require('body-parser')
const router = express.Router()
const morgan = require('morgan')

const port = 3000

const apiRoutes = require('./services')(router)

// configure app to use bodyParser()
// this will let us get the data from a POST
app.use(bodyParser.urlencoded({ extended: true }))
app.use(bodyParser.json())

// use morgan to log requests to the console
app.use(morgan('dev'))

// import routes
app.use('/mocks/api', apiRoutes)

// use `public` directory to serve static files
app.use('/public', express.static(path.join(__dirname, 'public')))

app.listen(port, function () {
  console.log(`Mock server listening on port ${port}`)
})
