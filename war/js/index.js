function buildGameDesc(game) {
	var li_class = "";
	var invite = "";
	if(game.inviter) {
		li_class = "invited";
		invite = 'Invitato da ' + game.inviter.name;
	}
	return '<li id="' + game.id + '" class="' + li_class + '">' + 
				game.name + ' creata da ' + game.owner.name + ' - <a href="#" class="join_link" title="'+invite+'">Unisciti</a>' +
				'<div class="game_description">' + game.players.length + ' giocatori attualmente in attesa' + '</div>'
			'</li>';
}