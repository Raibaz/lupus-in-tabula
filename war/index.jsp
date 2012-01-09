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
<!doctype html>
<html>
	<head>
		<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
		<link href="/css/style.css?ver=<%=new Date()%>" rel="stylesheet" type="text/css"/>
		<link href="/css/jquery.qtip.min.css" rel="stylesheet" type="text/css"/>
		<script type="text/javascript">
			if(<%=request.getParameter("need_authentication")%> == true) {				
				top.location.href = "https://www.facebook.com/dialog/oauth?client_id=183863271686299&redirect_uri=https://apps.facebook.com/lupusintabula/";
			}			
		</script>  
		<script type="text/javascript" src="https://www.google.com/jsapi?key=ABQIAAAArc-aBcMtas27GxefyJyUHhRL-CQUxo4cyKjOW-vmsVYovkcPkxQE2hJN1nGerTi9FsBBBwotb0LXSQ"></script>
		<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.6.2/jquery.min.js"></script>
		<script type="text/javascript" src="/js/jquery.qtip.min.js"></script>
		<script type="text/javascript" src="/_ah/channel/jsapi"></script>
		<script type="text/javascript" src="/js/index.js?ver=<%=new Date()%>"></script>				
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
				$.post('/list_games?'+new Date().getTime(), {"player_id":"<%=currentPlayer.getFbId()%>"}, function(data) {
					resp = data;	
					$('#gamelist').html("");	
					if(resp.length == 0) {
						$('#gamelist').html('Non ci sono partite in attesa di giocatori. Perchè non ne crei una tu?');
					}			
					for(i in resp) {																						
						$('#gamelist').append(buildGameDesc(resp[i]));											
					}					
					$('#gamelist li a').click(function() {						
						game_id = $(this).parent().attr("id");
						$.post('/join_game?'+new Date().getTime(), {"game": game_id, "player_id": '<%=currentPlayer.getFbId()%>'}, function(data) {
							resp = data;
							window.location.replace("/waiting_game.jsp?game_id=" + resp.id + "&channel_token=" + resp.channelToken + "&player_id="+"<%=currentPlayer.getFbId()%>");
						}, "json");
					}).removeData('qtip').qtip({
						overwrite: false,
						position: {
							my: "left center",
							at: "right center",
							target: $('#gamelist li a')
						},
						show: {							
							ready: true,
							solo: true,
							effect: function() {
								$(this).fadeIn("slow");
							}												
						}, hide: {
							effect: function() {
								$(this).fadeOut("slow");
							}
						},
						style : {
							classes: "ui-tooltip-jtools ui-tooltip-dark invite-tooltip"
						}				
					});							
				}, "json");
			}		
												
			$('#create_game').click(function() {			   
				$('#create_game').attr('disabled', 'true');
				$.post('/create_game?'+new Date().getTime(), {player_id:'<%=currentPlayer.getFbId()%>'}, function(data) {
					resp = data;									
					url = "/waiting_game.jsp?is_owner=true&game_id=" + resp.id + "&channel_token=" + resp.channelToken + "&player_id="+"<%=currentPlayer.getFbId()%>";					
					window.location.replace(url);										
				}, "json");
			});
			
			
		</script>
	</body>
</html>