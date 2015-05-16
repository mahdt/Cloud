var express = require('express');
var path = require('path');
var favicon = require('serve-favicon');
var logger = require('morgan');
var cookieParser = require('cookie-parser');
var bodyParser = require('body-parser');
var routes = require('./routes/index');
var Tweets = require('./routes/Tweets');
var users = require('./routes/users');
var twitter = require('twitter');

var app = express();


var server = require('http').Server(app);
var io = require('socket.io')(server);

//Setup twitter stream api
var twit = new twitter({
  consumer_key: '0051bD7xTxDrRlcunih5HmziH',
  consumer_secret: 'XGBitJq6Wh8dhkxdYeyRnF6hi1cyszNMuTJQmsfgiznq7WAmVU',
  access_token_key: '549807950-lNSdnbkHEUWYZGA467ffXPX9olQ57ch0jngYnXYo',
  access_token_secret: 'R0M4GE7qH9AxJp1BVHzRS16fhY6oQrmOzArR31iATWfc4'
}),
stream = null;

server.listen(8081);

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'jade');

// uncomment after placing your favicon in /public
//app.use(favicon(__dirname + '/public/favicon.ico'));
app.use(logger('dev'));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));

app.use('/', routes);
app.use('/users', users);
app.use('/Tweets',Tweets);



// catch 404 and forward to error handler
app.use(function(req, res, next) {
    var err = new Error('Not Found');
    err.status = 404;
    next(err);
});

// error handlers

// development error handler
// will print stacktrace
if (app.get('env') === 'development') {
    app.use(function(err, req, res, next) {
        res.status(err.status || 500);
        res.render('error', {
            message: err.message,
            error: err
        });
    });
}

// production error handler
// no stacktraces leaked to user
app.use(function(err, req, res, next) {
    res.status(err.status || 500);
    res.render('error', {
        message: err.message,
        error: {}
    });
});

var mysql      = require('mysql');
var connection = mysql.createConnection({
  connectionLimit : 100,
  host     : '127.0.0.1',
  user     : 'root',
  password : 'Garima@123',
  database:  'project1'

});




connection.connect(function(err) {
  if (err) {
    console.error('error connecting: ' + err.stack);
    return;
  }

  console.log('connected as id ' + connection.threadId);
});

//and pass it into the node-mysql-wrap constructor
var createMySQLWrap = require('mysql-wrap');
sql = createMySQLWrap(connection);


io.on('connection',function(socket){  
    console.log("New Connection created");
    socket.on('InitTweets',function(){
        //SQL Query for particular HashTag, get data
        //socket.emit('InitData', data);
        sql.query('SELECT TweetID,Text as TweetText,Latitude,Longitude FROM Tweets',function(err,rows,fields){
  socket.emit('InitData', rows);
  });
  });


    socket.on('getLatestTweets', function(){
      //alert("sdsd");
      //Twiiter Stream, Fiters to get tweets for that hashtag
      //socket.on('gotLatestTwet', data);
          twit.stream('statuses/filter', {'locations':'-180,-90,180,90'}, function(s) {
          stream = s;
          stream.on('data', function(data) {
            //console.log('sdsds');
              // Does the JSON result have coordinates
              if (data.coordinates){
                if (data.coordinates !== null){
                  //If so then build up some nice json and send out to web sockets
                  var outputPoint = {"lat": data.coordinates.coordinates[0],"lng": data.coordinates.coordinates[1]};
                  //console.log(outputPoint);
                  //Send out to web sockets channel.
                  socket.emit('twitter-stream', outputPoint);
                }
              }
          });
          stream.on('error', function(error) {
            console.log('sdsdrrrr');
            console.log(error);
          });
      });
    });
    socket.emit("connected");
});


module.exports = app;
