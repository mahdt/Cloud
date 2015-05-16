var express = require('express');
var router = express.Router();

var fs = require('fs');


/* GET home page. */
router.get('/', function(req, res, next) {
  fs.readFile('/home/rahul/Documents/Cloud/Twitter/nodetest2/public/javascripts/map.html', function (err, html) {
    if (err) {
        throw err; 
    }       
        res.writeHeader(200, {"Content-Type": "text/html"});  
        res.write(html);  
        res.end();
});
  //res.render('index', { title: 'Express' });
});

module.exports = router;