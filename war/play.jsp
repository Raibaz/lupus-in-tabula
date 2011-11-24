<html>
	<head>
		<script type="text/javascript" src="https://www.google.com/jsapi?key=ABQIAAAArc-aBcMtas27GxefyJyUHhRL-CQUxo4cyKjOW-vmsVYovkcPkxQE2hJN1nGerTi9FsBBBwotb0LXSQ"></script>
		<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.6.2/jquery.min.js"></script>
		<script type="text/javascript" src="js/jquery.blockUI.js"></script>
		<script type="text/javascript" src="/_ah/channel/jsapi"></script>		
		<script type="text/javascript" src="js/util.js"></script>
		<link href="/style.css" rel="stylesheet" type="text/css"/> 
	</head>
	<body>
		<div id="play-main">
			<div id="chat">
				<ul id="chat-log"></ul>
			</div>
			<div id="players">
				<ul id="players-list"></ul>
			</div>
			<div id="commands">
				<a id="stop-debate" href="#">Ferma il dibattito</a>
			</div>
		</div>
		<div id="play-lower">
			<form id="chat-form">
				<input id="chat-text" type="text" value="Chat here..."/>
				<input id="chat-submit" type="submit" value="Invia"/>
			</form>
		</div>
		
		<script type="text/javascript">
							
			var current_state = "DEBATE";
			$(document).ready(function() {
				$.post('/get_game', {"game_id":"<%=request.getParameter("game_id")%>"}, function(data) {
					resp = JSON.parse(data);
					for(i in resp.players) {
						$('#players-list').append('<li class="player-item" id="' + resp.players[i].fbId + '"><img src="' + resp.players[i].pictureUrl + '"/>' + resp.players[i].name + '</li>');
					}
					
					$('.player-item').click(function() {
						if(current_state === 'VOTING_1') {							
							$.post('/vote', {"target_id": this.id, "game_id": "<%=request.getParameter("game_id")%>", "voter_id":"<%=request.getParameter("player_id")%>"});
						}
					});
					
					fadeItem = function() {
 					   $('#players-list li:hidden:first').delay(500).fadeIn(fadeItem);
					}
					fadeItem();
					
					if("<%=request.getParameter("player_id")%>" === resp.owner.fbId) {
						$('#stop-debate').show();
					}
					
					$('#stop-debate').click(function() {
						$.post('/change-state', {"game_id":"<%=request.getParameter("game_id")%>","state":"VOTING_1"});
					});
					
					$('#chat-text').click(function() {
						$('#chat-text').val("");
					});
					
					$('#chat-form').submit(function() {
						msg = $('#chat-text').val();
						$.post('/chat', {"game_id": "<%=request.getParameter("game_id")%>","msg": msg, "player_id": "<%=request.getParameter("player_id")%>"});
						$('#chat-text').val("");
						return false;
					});
														
					channel = new goog.appengine.Channel('<%=request.getParameter("channel_token")%>');
					socket = channel.open();				
					socket.onmessage = function(message) {
						console.info(message.data);
						data = JSON.parse(message.data);
						if(data.type == "CHAT") {
							chat(data.player.name, data.msg);							 
						} else if(data.type == "GAMESTATE" && data.msg && data.msg.indexOf("start__") == 0) {																			
							url = "/play.jsp?player_id=" + data.player.fbId + "&game_id=" + data.gameId + '&channel_token=' + data.msg.substring("start__".length);
							console.info(url);
							location.replace(url);						
						} else if(data.type == "GAMESTATE") {
							if(data.msg === "VOTING_1") {
								current_state = data.msg;
								chat('', "Inizia il primo round di nomination, il primo a votare � " + data.target.name);
								if(<%=request.getParameter("player_id")%> == data.target.fbId) {
									$('#players').unblock();
								} else {
									$('#players').block();
								}
							} else if(data.msg == "VOTING_2") {
								$('#players').unblock();
								var splitted = data.msg.split("|");								
								$('.player-item').block();
								chat('', "E' finito il primo giro di nomination");
								for(i in data.nominated) {									
									$('#' + data.nominated[i].fbId).unblock();
									chat('', data.nominated[i].name + " � stato nominato");
								}																							
								chat('', "Inizia il secondo round di nomination, il primo a votare � " + data.target.name);
								if(<%=request.getParameter("player_id")%> == data.target.fbId) {
									$('#players').unblock();
								} else {
									$('#players').block();
								}
							} else if(data.msg == "NIGHT") {								
								chat('', data.target.name + " � morto.");
								chat('', "� notte");	
								chat('', "I lupi aprono gli occhi.");	
								$('#players').block();						
							} else if(data.msg == "DEBATE") {
								chat('', "E' giorno, e " + data.target.name + " � morto.");
								$('#players').unblock();
							}
						} else if(data.type == "VOTE") {
							chat(data.player.name, "ha votato per <b>" + data.target.name + "</b>");	
							if(data.next) {						 						
								chat('', "Ora � il turno di " + data.next.name);
								if(<%=request.getParameter("player_id")%> == data.next.fbId) {
									$('#players').unblock();
								} else {
									$('#players').block();
								}
							}
						} else if(data.type == "NIGHTVOTE") {
							if(data.next.fbId == <%=request.getParameter("player_id")%>) {
								chat('', "E' il tuo turno di indicare un giocatore");
								$('#players').unblock();
							} else {
								chat('', "E' il turno di " + data.next.name + " di indicare un giocatore");
							}
						}
					};						
				});				
			});
		</script>
	</body>
</html>