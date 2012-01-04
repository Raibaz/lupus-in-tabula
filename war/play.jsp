<%@ page import="java.util.Date"%>

<html>
	<head>
		<script type="text/javascript" src="https://www.google.com/jsapi?key=ABQIAAAArc-aBcMtas27GxefyJyUHhRL-CQUxo4cyKjOW-vmsVYovkcPkxQE2hJN1nGerTi9FsBBBwotb0LXSQ"></script>
		<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.6.2/jquery.min.js"></script>
		<script type="text/javascript" src="/js/jquery.blockUI.js"></script>
		<script type="text/javascript" src="/js/jquery.qtip.min.js"></script>
		<script type="text/javascript" src="/_ah/channel/jsapi"></script>		
		<script type="text/javascript" src="/js/util.js?ver=<%=new Date()%>"></script>
		<script type="text/javascript" src="/js/play.js?ver=<%=new Date()%>"></script>
		<link href="/css/style.css?ver=<%=new Date()%>" rel="stylesheet" type="text/css"/> 
		<link href="/css/jquery.qtip.min.css" rel="stylesheet" type="text/css"/>  
	</head>
	<body>
		<div id="play-main">
			<div id="chat">
				<ul id="chat-log"></ul>
			</div>
			<div id="players">
				<ul id="players-list"></ul>
			</div>
			<div id="timer">&nbsp;</div>
			<div id="commands">
				<a id="stop-debate" href="#">Ferma il dibattito</a><br/>
				<a id="archive-game" href="#">Termina la partita</a>
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
			game_id = "<%=request.getParameter("game_id")%>";
			myFbId = "<%=request.getParameter("player_id")%>";
			$(document).ready(function() {
				$.post('/get_game', {"game_id":"<%=request.getParameter("game_id")%>"}, function(data) {					
					resp = JSON.parse(data);					
					for(i in resp.players) {
						$('#players-list').append('<li class="player-item" id="' + resp.players[i].fbId + '"><img src="' + resp.players[i].pictureUrl + '"/>' + resp.players[i].name + ' <span class="votes"/></li>');
						players[resp.players[i].fbId] = new Player();
						if(resp.players[i].fbId == myFbId) {
							players[resp.players[i].fbId].self = true;
						}
					}
					
					$('.player-item').click(function() {						
						if(current_state !== 'DEBATE') {
							$('#players').block().removeData('qtip');							
							$.post('/vote', {"target_id": this.id, "game_id": game_id, "voter_id":myFbId});
						}
					});
					
					fadeItem = function() {
 					   $('#players-list li:hidden:first').delay(200).fadeIn(fadeItem);
					}
					fadeItem();
					
					if(myFbId === resp.owner.fbId) {
						is_owner = true;
						$('#stop-debate').show().click(function() {
							$.post('/change-state', {"game_id":game_id,"state":"VOTING_1"});
							$('#stop-debate').hide();
							return false;
						});
						$('#archive-game').show().click(function() {
							$.post('/archive-game', {"game_id":game_id,"player_id":myFbId});
						});
					}
													
					$('#chat-text').click(function() {
						$('#chat-text').val("");
					});
					
					$('#chat-form').submit(function() {
						if(!players[myFbId].alive) {
							chat('', 'Non puoi parlare, sei morto.');
							return false;
						}						
						if(defending_player && defending_player.fbId != myFbId) {
							chat('', "Non puoi parlare durante l'arringa di " + defending_player.name);
							return false;
						}
						msg = $('#chat-text').val();
						$.post('/chat', {"game_id": game_id,"msg": msg, "player_id": myFbId});
						$('#chat-text').val("");
						return false;
					});
														
					channel = new goog.appengine.Channel('<%=request.getParameter("channel_token")%>');
					socket = channel.open();				
					socket.onmessage = function(message) {						
						data = JSON.parse(message.data);
						if(data.type == "CHAT") {							
							chat(data.player.name, data.msg);							 
						} else if(data.type == "GAMESTATE" && data.msg && data.msg.indexOf("start__") == 0) {																			
							url = "/play.jsp?player_id=" + data.player.fbId + "&game_id=" + data.gameId + '&channel_token=' + data.msg.substring("start__".length);							
							window.location.replace(url);						
						} else if(data.type == "GAMESTATE") {
							current_state = data.msg;
							if(data.msg === "VOTING_1") {								
								chat('', "Inizia il primo round di nomination, il primo a votare è " + data.target.name);
								if(myFbId == data.target.fbId) {
									$('#players').unblock().qtip({
										content: {
											text: "Per votare per un giocatore o per indicarlo, è sufficiente cliccare sulla sua immagine"
										}, style: {
											classes: "ui-tooltip-jtools ui-tooltip-dark hint-tooltip",
											height: "20%"
										}, position:  {
											my: "top right",
											at: "top left",
											target: $('#players-list')
										}, show: {
											ready: true,
											event: false,
											delay: 3000
										}, hide: {
											inactive: 3000
										}
									});
									blockUnavailablePlayers();							
								} else {
									$('#players').block();
								}
							} else if(data.msg == "VOTING_2") {		
								defending_player = undefined;																																
								chat('', "Inizia il secondo round di nomination, il primo a votare è " + data.target.name);
								if(myFbId == data.target.fbId) {
									$('#players').unblock();
									blockUnavailablePlayers();									
								} else {
									$('#players').block();
								}
							} else if(data.msg == "NIGHT") {								
								chat('', data.target.name + " è morto.");
								players[data.target.fbId].alive = false;
								$('#' + data.target.fbId).addClass('dead-player');
								resetVotes();
								resetNominees();
								chat('', "è notte");	
								chat('', "I lupi aprono gli occhi.");	
								$('#players').block();						
							} else if(data.msg == "DEBATE") {
								current_state = data.msg;
								chat('', "E' giorno, e " + data.target.name + " è morto.");
								$('#' + data.target.fbId).addClass('dead-player');
								players[data.target.fbId].alive = false;
								$('#players').unblock();
								if(is_owner) {
									$('#stop-debate').show();
								}
							} else if(data.msg == "ENDED") {
								$('#players').block();
								chat('', "Il gioco è finito.");
							} else if(data.msg == "SEER") {
								chat('', "I lupi chiudono gli occhi.");
								chat('', "Il veggente apre gli occhi e indica un giocatore");
								resetVotes();
								$('#players').block();
							} else if(data.msg == "MEDIUM") {
								chat('', "Il veggente chiude gli occhi.");
								chat('', "Il medium apre gli occhi e ottiene informazioni sul giocatore morto di giorno");
								$('#players').block();
							} else if(data.msg == "DEFENSE") {
								$('#players').unblock();
								if(data.nominated) {
									var splitted = data.msg.split("|");								
									resetVotes();
									chat('', "E' finito il primo giro di nomination");								
									for(i in data.nominated) {							
										players[data.nominated[i].fbId].nominated = true;											
										chat('', data.nominated[i].name + " è stato nominato");
									}				
								}
								chat('', data.target.name + " ha ora la possibilità di difendersi");
								startDefenseTimer(data.target, <%=request.getParameter("player_id")%>);								
							} else if(data.msg == "ARCHIVED") {
								socket.close();
								setTimeout('window.location.replace("/game_finished.jsp");', 5000);
								chat('', "Il moderatore ha deciso di concludere la partita, verrete reindirizzati alla pagina successiva tra 5 secondi...");
							}
						} else if(data.type == "VOTE") {
							chat(data.player.name, "ha votato per <b>" + data.target.name + "</b>");	
							players[data.target.fbId].votes++;
							$('#' + data.target.fbId + " .votes").html(players[data.target.fbId].votes).show();
							if(data.next) {						 						
								chat('', "Ora è il turno di " + data.next.name);
								if(myFbId == data.next.fbId) {
									$('#players').unblock();
									blockUnavailablePlayers();
								} else {
									$('#players').block();
								}
							}
						} else if(data.type == "NIGHTVOTE") {
							if(data.next.fbId == myFbId) {
								chat('', "E' il tuo turno di indicare un giocatore");
								$('#players').unblock();
								blockUnavailablePlayers();
							} else {
								chat('', "E' il turno di " + data.next.name + " di indicare un giocatore");
							}
						} else if(data.type == "NIGHTINFO") {
							if(data.msg == "true") {
								chat('', data.target.name  + " è un lupo");
							} else {
								chat('', data.target.name  + " è un contadino");
							}
						}
					};						
				});			
				$.post('/get-role', {"game_id":game_id,"player_id":myFbId}, function(data) {
					resp = JSON.parse(data);					
					chat('', "Il tuo ruolo è " + resp.msg);
					myRole = resp.msg;
					for(i in resp.nominated) {
						if(resp.msg == "WOLF") {
							players[resp.nominated[i].fbId].wolf = true;
						}
						chat('', resp.nominated[i].name + " ha il tuo stesso ruolo");
					}
				});	
			});
		</script>
	</body>
</html>