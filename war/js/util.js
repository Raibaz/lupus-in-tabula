/*************************
 * BLOCKUI CONFIGURATION
 *************************/
$.blockUI.defaults.message = '';
$.blockUI.defaults.css = {};



function chat(name, msg) {
	if(!name) {
		name = "Lupus in tabula";
	}
	$('#chat-log').append("<li><b>" + name + "</b>: " + msg + "</li>").scrollTop($('#chat-log').height()); 		
}