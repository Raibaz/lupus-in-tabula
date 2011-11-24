<html>
	<head>
		<script type="text/javascript" src="https://www.google.com/jsapi?key=ABQIAAAArc-aBcMtas27GxefyJyUHhRL-CQUxo4cyKjOW-vmsVYovkcPkxQE2hJN1nGerTi9FsBBBwotb0LXSQ"></script>
		<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.6.2/jquery.min.js"></script>
		<script type="text/javascript" src="/_ah/channel/jsapi"></script>
		<link href="/style.css" rel="stylesheet" type="text/css"/> 
	</head>
	<body>
		O hai, you are waiting for players to join game <%= request.getParameter("game_id")%>
		
		<div id="players">
			<ul id="players-list">				
			</ul>
		</div>
		
		<% 
			if(request.getParameter("is_owner") != null && request.getParameter("is_owner").equals("true")) {
		%>
			<a href="#" id="start_game">Comincia la partita!</a>
		<%
			}
		%>
		
		
		<script type="text/javascript">
			$(document).ready(function() {
				
				$.post('/get_game', {"game_id":"<%=request.getParameter("game_id")%>"}, function(data) {
					resp = JSON.parse(data);
					for(i in resp.players) {						
						$('#players-list').append('<li id="' + resp.players[i].fbId + '"><img src="' + resp.players[i].pictureUrl + '"/>' + resp.players[i].name + ' </li>');
					}
					
					fadeItem = function() {
 					   $('#players-list li:hidden:first').delay(500).fadeIn(fadeItem);
					}
					fadeItem();
					
				});				
				
				channel = new goog.appengine.Channel('<%=request.getParameter("channel_token")%>');
				socket = channel.open();				
				socket.onmessage = function(message) {
					console.info(message.data);
					data = JSON.parse(message.data);
					if(data.type == "JOIN") {
						$('#players-list').append('<li id="' + data.player.fbId + '"><img src="' + data.player.pictureUrl + '"/>' + data.player.name + ' </li>');
						$('#'+data.player.fbId).fadeIn();
					} else if(data.type == "GAMESTATE" && data.msg.indexOf("start__") == 0) {																			
						url = "/play.jsp?player_id=" + data.target.fbId + "&game_id=" + data.gameId + '&channel_token=' + data.msg.substring("start__".length);
						console.info(url);
						channel.close();
						location.replace(url);						
					}
				};	
				
				$('#start_game').click(function() {
					$.post('/start_game', {"game_id": "<%=request.getParameter("game_id")%>"});
				});			
			});
		</script>
	</body>
</html>