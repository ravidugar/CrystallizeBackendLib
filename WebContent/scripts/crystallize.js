var runningList = new Array();

document.addEventListener("keydown", KeyCheck);

/**
 * Checks if key pressed was backspace,
 * Deletes the last word in the running list 
 * if backspace was pressed.
 */
function KeyCheck(event)
{
   var KeyID = event.keyCode;
   switch(KeyID)
   {
      case 8:
      var text = document.getElementById("query").value;
      if (text[text.length-1] != " ") {
        var allWords = text.split(" ");
        var wordToRemove = allWords[allWords.length-1];
        text = text.substring(0,text.lastIndexOf(wordToRemove));
        document.getElementById("query").value = text;
        runningList.pop();
      }
      break; 
      default:
      break;
   }
}

/**
 * Sends the running list to the backend
 */
function mysubmit() {
    var responseArray = new Array();
    // Creates the appropriate JSON objects
    for (var i = 0; i < runningList.length; i++) {
        responseArray[i] = {FormID:0, Tags:null, WordID: runningList[i]};
    }

    // Create a HTTP request to query database
	var insertData = {};
	insertData.table = "Phrases";
	insertData.ID = "4"; //TODO: Change this
	insertData.document = {}
	insertData.document.PhraseSequence = responseArray;
	
	console.log(insertData);
	
	// AJAX POST request to get query results
    $.ajax({
        type: "POST",
        url: "Insert",
        data: JSON.stringify(insertData),
        dataType: 'json',
        contentType: 'application/json;charset=UTF-8',
        crossDomain: true,
        success: function( res ) {
        	if(!res.ok) alert("Failed to submit translation")
        	}
    	});
}

$(function(){
function split( val ) {
    return val.split( / \s*/ );
}

// Gets the last word in the textbox
function extractLast( term ) {
    return split( term ).pop();
}

$( "#query" )
    // don't navigate away from the field on tab when selecting an item
    .bind( "keydown", function( event ) {
        if ( event.keyCode === $.ui.keyCode.TAB &&
            $( this ).data( "autocomplete" ).menu.active ) {
            event.preventDefault();
        }
    })

    .autocomplete({
        minLength: 1,
        source: function( request, response ) {
        	
        	// Get the last word in the textbox
        	var query = extractLast( request.term );
        	
        	// Create a HTTP request to query database
        	var requestData = {};
        	requestData.table = "Dictionary";
        	var queryJSON = {};
        	queryJSON.attribute = "EnglishSummary";
        	queryJSON.op = "CONTAINS";
        	queryJSON.values = [query];

        	requestData.query = [queryJSON];
        	console.log(requestData);
        	
        	// AJAX POST request to get query results
            $.ajax({
                type: "POST",
                url: "Query",
                data: JSON.stringify(requestData),
                dataType: 'json',
                contentType: 'application/json;charset=UTF-8',
                crossDomain: true,
                success: function( res ) {
                      var wordArray = new Array();
                      var wordObjectArray = res["results"];
                      // Only display 10 results at a time
                      // Display english_translation:japanese and store wordID as value
                      for (i=0; i < 11; i++) {
                          wordObject = wordObjectArray[i];
                          if (wordObject == undefined) {
                        	  break;
                          }
                          wordArray[i] = {label: wordObject["EnglishSummary"] + ":" + wordObject["Kana"][0], value: wordObject["WordID"]};
                      }
                      // Return array of results for autocomplete to display
                      response(wordArray);
                }
            	});
        },
        focus: function() {
            // prevent value inserted on focus
            return false;
        },
        select: function( event, ui ) {
            var terms = split( this.value );
            
            // remove the current input
            terms.pop();
            
            // add the wordID of selected item to the running list
            runningList.push(ui.item.value);
            
            // Get the japanese word from the label
            // To be displayed in the textbox on selection
            var engAndJap = ui.item.label;
            terms.push(engAndJap.split(":")[1]);
            
            // add placeholder to get the space at the end
            terms.push( "" );
            this.value = terms.join( " " );
            return false;
        },
        open: function( event, ui ) {
            var input = $( event.target ),
                widget = input.autocomplete( "widget" ),
                style = $.extend( input.css( [
                    "font",
                    "border-left",
                    "padding-left"
                ] ), {
                    position: "absolute",
                    visibility: "hidden",
                    "word-wrap": "break-word",
                    "padding-right": 0,
                    "border-right": 0,
                    "white-space": "pre"
                } ),
                div = $( "<div/>" ),
                pos = {
                    my: "left top",
                    collision: "none"
                },
                offset = -7; // magic number to align the first letter
                             // in the text field with the first letter
                             // of suggestions
                             // depends on how you style the autocomplete box

            widget.css( "width", "" );

            div
                .text( input.val().replace( /\S*$/, "" ) )
                .css( style )
                .insertAfter( input );
            offset = Math.min(
                Math.max( offset + div.width(), 0 ),
                input.width() - widget.width()
            );
            div.remove();

            pos.at = "left+" + offset + " bottom";
            input.autocomplete( "option", "position", pos );

            widget.position( $.extend( { of: input }, pos ) );
        }
    });

});