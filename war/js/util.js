function chat(name, msg) {
	if(!name) {
		name = "Lupus in tabula";
	}
	$('#chat-log').append("<li><b>" + name + "</b>: " + msg + "</li>"); 	
}