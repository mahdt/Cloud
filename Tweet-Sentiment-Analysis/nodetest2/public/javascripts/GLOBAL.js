var TwitterData = [];
// DOM Ready =============================================================
$(document).ready(function() {

    // Populate the user table on initial page load
    populateTable();

});
// Functions =============================================================

// Fill table with data
function populateTable() {

    // Empty content string
    var tableContent = '';

    var id = 571109665678848000;
    var path = 'http://localhost:3000/Tweets/';

    path = path + id;



    $.getJSON( path, function( data ) {

    	 tableContent += '<tr>';
            tableContent += '<td><a href="#" class="linkshowuser" rel="' + this.TweetID + '">' + this.TweetID + '</a></td>';
            tableContent += '<td>' + this.TweetText + '</td>';
            tableContent += '<td><a href="#" class="linkdeleteuser" rel="' + this.TweetID + '">delete</a></td>';
            tableContent += '</tr>';
        });

        // Inject the whole content string into our existing HTML table
        $('#userList table tbody').html(tableContent);
    });
};

