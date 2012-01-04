var game_id;
var is_owner = false;
var players = [];
var myFbId;
var myRole;
var defending_player;
var interval;

function Player() {
	this.alive = true;
	this.nominated = false;
	this.self = false;
	this.votes = 0;
	this.wolf = false;
}

function blockUnavailablePlayers() {
	for(i in players) {
		if(current_state == "VOTING_2") {			
			if(!players[i].nominated) {
				$('#' + i).block();				
			}
		} else {
			if(!players[i].alive || players[i].self) {
				$('#' + i).block();
			}
		}
		
		if(current_state == "NIGHT" && myRole == "WOLF") {
			if(players[i].wolf) {
				$('#' + i).block();
			}
		} 
	}
}

function resetVotes() {
	for(i in players) {
		players[i].votes = 0;
		$('#' + i + " .votes").hide();
	}	
}

function resetNominees() {
	for(i in players) {
		players[i].nominated = false;
	}
}

function startDefenseTimer(defending, local_player_id) {
	defending_player = defending;
	$('#timer').show();
	remaining_time = 30;
	interval = setInterval(function() {
		$('#timer').html("0:" + remaining_time--);
		if(remaining_time < 10) {
			$('#timer').html("0:0" + remaining_time);
		}
		if(remaining_time <= 0) {
			$('#timer').hide();
			clearInterval(interval);
			if(defending_player.fbId == local_player_id) {
				$.post('/change-state', {"game_id":game_id,"state":"DEFENSE","player_id":local_player_id});
			}
		}
	}, 1000);
	
}