<!doctype html>
<html>
	<head>
		<script type="text/javascript" src="https://www.google.com/jsapi?key=ABQIAAAArc-aBcMtas27GxefyJyUHhRL-CQUxo4cyKjOW-vmsVYovkcPkxQE2hJN1nGerTi9FsBBBwotb0LXSQ"></script>
		<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.6.2/jquery.min.js"></script>
		<script type="text/javascript" src="/_ah/channel/jsapi"></script>
		<script src="http://connect.facebook.net/en_US/all.js"></script>
		<link href="/css/style.css" rel="stylesheet" type="text/css"/> 
		<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
	</head>
	<body>
		In attesa di giocatori...non chiudere questa pagina!<br/>
		Ricordati che per avere un'esperienza di gioco soddisfacente è necessario avere almeno otto giocatori connessi, per cui <a href="#" class="invite-friends">invita</a> subito dei tuoi amici!
		
		<div id="players">
			<ul id="players-list">				
			</ul>
		</div>
		
		
		<% 
			if(request.getParameter("is_owner") != null && request.getParameter("is_owner").equals("true")) {
		%>
			<a href="#" id="start_game">Comincia la partita</a><br/>
			<a href="#" id="archive_game">Elimina la partita</a>
		<%
			}
		%>
		
		<div id="invite">
			<a href="#" id="invite-friends" class="invite-friends">Invita dei tuoi amici a questa partita</a>
		</div>
		
		<div id="messages">&nbsp;</div>
		
		<div id="fb-root">&nbsp;</div>
		
		
		<script type="text/javascript">
			$(document).ready(function() {
				
				$.post('/get_game?' + new Date().getTime(), {"game_id":"<%=request.getParameter("game_id")%>"}, function(data) {
					resp = data;
					for(i in resp.players) {						
						$('#players-list').append('<li id="' + resp.players[i].fbId + '"><img src="' + resp.players[i].pictureUrl + '"/>' + resp.players[i].name + ' </li>');
					}
					
					fadeItem = function() {
 					   $('#players-list li:hidden:first').delay(500).fadeIn(fadeItem);
					}
					fadeItem();
					
				}, "json");				
				
				channel = new goog.appengine.Channel('<%=request.getParameter("channel_token")%>');				
				socket = channel.open();								
				socket.onmessage = function(message) {											
					data = JSON.parse(message.data);					
					if(data.type == "JOIN") {
						$('#players-list').append('<li id="' + data.player.fbId + '"><img src="' + data.player.pictureUrl + '"/>' + data.player.name + ' </li>');
						$('#'+data.player.fbId).fadeIn();
					} else if(data.type == "GAMESTATE") {
						if(data.msg.indexOf("start__") == 0) {																			
							url = "/play.jsp?player_id=" + data.target.fbId + "&game_id=" + data.gameId + '&channel_token=' + data.msg.substring("start__".length);													
							window.location.replace(url);						
						} else if(data.msg == "ARCHIVED") {
							$('#messages').html("La partita è stata cancellata, verrete rediretti alla pagina iniziale tra 5 secondi...");
							setTimeout('window.location.replace("https://apps.facebook.com/lupusintabula");', 5000);
						}
					}
				};	
				
				$('#start_game').click(function() {
					$.post('/start_game?' + new Date().getTime(), {"game_id": "<%=request.getParameter("game_id")%>"});
				});
							
				$('#archive_game').click(function() {
					$.post('/archive-game?' + new Date().getTime(), {"game_id": "<%=request.getParameter("game_id")%>","player_id":"<%=request.getParameter("player_id")%>"}, function(data) {
						window.location.replace("https://apps.facebook.com/lupusintabula");
					});
				});				
				
				$('.invite-friends').click(function() {
					FB.init({appId:'183863271686299', cookie:true,status:true, xfbml:true,frictionlessRequests:true});
     				FB.ui({ method: 'apprequests',message: 'Seleziona quali amici vuoi invitare. Attenzione: è preferibile invitare amici che sono già online.',data: '<%=request.getParameter("game_id")%>'});
				});
			});
		</script>
	</body>
</html>