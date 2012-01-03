<%@page import="com.google.appengine.api.channel.*"%>
<%@page import="com.raibaz.lupus.facebook.*"%>
<%@page import="com.raibaz.lupus.game.*"%>
<%@ page import="java.util.Date"%>

<%		
	Player currentPlayer = new Player();
	if(request.getParameter("player_name") != null) {
		currentPlayer.setName(request.getParameter("player_name"));
	}
	if(request.getParameter("player_avatar") != null) {
		currentPlayer.setPictureUrl(request.getParameter("player_avatar"));
	}
	String chanToken = "";
	if(request.getParameter("player_id") != null) {	
		ChannelService chanServ = ChannelServiceFactory.getChannelService();
		chanToken = chanServ.createChannel("games");
		currentPlayer.setFbId(request.getParameter("player_id"));
	}
%>

<html>
	<head>
		<link href="/style.css?ver=<%=new Date()%>" rel="stylesheet" type="text/css"/> 
		<script type="text/javascript" src="https://www.google.com/jsapi?key=ABQIAAAArc-aBcMtas27GxefyJyUHhRL-CQUxo4cyKjOW-vmsVYovkcPkxQE2hJN1nGerTi9FsBBBwotb0LXSQ"></script>
		<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.6.2/jquery.min.js"></script>
		<script type="text/javascript" src="/_ah/channel/jsapi"></script>		
		<script type="text/javascript">
			if(<%=request.getParameter("need_authentication")%> == true) {				
				top.location.href = "https://www.facebook.com/dialog/oauth?client_id=183863271686299&redirect_uri=https://apps.facebook.com/lupusintabula/";
			}			
		</script>
	</head>
	<body>
		<div id="welcome"><img class="avatar" src="<%=currentPlayer.getPictureUrl()%>"/><h3>Benvenuto a Lupus in tabula, <%=currentPlayer.getName()%></h3></div>		
		<div id="games"><span>Le seguenti partite sono in attesa di giocatori:<ul id="gamelist"></ul></div>
		<div id="create"><input type="button" id="create_game" value="Crea una partita"/>
		<script type="text/javascript">
			$(document).ready(function() {				
				updateGameList();
				setInterval("updateGameList()", 2000);						
			});	
			
			function updateGameList() {
				$.post('/list_games', {"player_id":"<%=currentPlayer.getFbId()%>"}, function(data) {
					resp = JSON.parse(data);	
					$('#gamelist').html("");	
					if(resp.length == 0) {
						$('#gamelist').html('Non ci sono partite in attesa di giocatori. Perch� non ne crei una tu?');
					}			
					for(i in resp) {
						game = resp[i];
						li_class = "";
						var invite = "";
						if(game.inviter) {
							li_class = "invited";
							invite = '<span class="invite">Invitato da ' + game.inviter.name + '</span>';
						}	
						li_class = "invited";
						invite = '<span class="invite">Invitato da sabadnbasd</span>';												
						$('#gamelist').append('<li id="' + game.id + '" class="' + li_class + '">' + game.name + ' creata da ' + game.owner.name + ' - <a href="#" class="join_link">Unisciti</a>' + invite + '</li>');						
					}
					$('#gamelist li a').click(function() {						
						game_id = $(this).parent().attr("id");
						$.post('/join_game', {"game": game_id, "player_id": '<%=currentPlayer.getFbId()%>'}, function(data) {
							resp = JSON.parse(data);
							window.location.replace("/waiting_game.jsp?game_id=" + resp.id + "&channel_token=" + resp.channelToken + "&player_id="+"<%=currentPlayer.getFbId()%>");
						});
					});
					
				});
			}		
						
			$('#create_game').click(function() {			   
				$('#create_game').attr('disabled', 'true');
				$.post('/create_game', {player_id:'<%=currentPlayer.getFbId()%>'}, function(data) {
					resp = JSON.parse(data);				
					url = "/waiting_game.jsp?is_owner=true&game_id=" + resp.id + "&channel_token=" + resp.channelToken + "&player_id="+"<%=currentPlayer.getFbId()%>";
					console.info(url);
					window.location.replace(url);										
				});
			});
			
			
		</script>
	</body>
</html>