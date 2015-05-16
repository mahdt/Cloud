var express = require('express');
var router = express.Router();

router.get('/', function(req, res) {
	sql.query('SELECT TweetID,Text as TweetText,Latitude,Longitude FROM Tweets',function(err,rows,fields){
	if(err) throw_err(err,res);
	res.json(rows);
});

});

function throw_err(err, res) {
    res.json({ 'error': {
        message: err.message,
        error: err
    }});
    throw err;
}

var projection = function(req,res){
  pagination(req, res, req.query.fields, 'Tweets');
};




router.get('/:id', function(req, res) {
	sql.query('SELECT TweetID,Text as TweetText,Latitude,Longitude FROM Tweets WHERE TweetID = ' + req.params.id,function(err,rows,fields){
	if(err) throw_err(err,res);
	res.json({'Tweets': rows});
});

});


module.exports = router;