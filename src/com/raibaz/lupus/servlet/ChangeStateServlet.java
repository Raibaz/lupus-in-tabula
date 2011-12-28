package com.raibaz.lupus.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.raibaz.lupus.LupusMessage;
import com.raibaz.lupus.LupusMessage.MessageType;
import com.raibaz.lupus.dao.LupusDAO;
import com.raibaz.lupus.game.Game;
import com.raibaz.lupus.game.GameEngine;
import com.raibaz.lupus.game.GameState;
import com.raibaz.lupus.game.Player;

public class ChangeStateServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String gameId = req.getParameter("game_id");
		String stateStr = req.getParameter("state");
		String playerId = req.getParameter("player_id");
		LupusDAO dao = new LupusDAO();
		
		Game g = dao.ofy().find(Game.class, gameId);
		
		GameState state = GameState.valueOf(stateStr);
		GameEngine engine = new GameEngine(g);
		
		if(state == GameState.VOTING_1) {
		
			g.setState(state);
											
			engine.resetVotes();
			LupusMessage msg = new LupusMessage(MessageType.GAMESTATE, g.getOwner());
			msg.setMsg(stateStr);
			msg.setTarget(engine.determineNextVoterInRound1(g.getLastDead()));
			msg.broadcastToPlayingPlayers(g);
			dao.ofy().put(g);
		} else if(state == GameState.DEFENSE) {
			Player currentNominated = dao.ofy().find(Player.class, playerId);
			Player nextNominated = null;
			List<Player> players = g.getPlayers(true);			
			int currentNominatedIndex = 0;
			for(int i = 0; i < players.size(); i++) {
				Player p = players.get(i);
				if(p.getFbId().equals(currentNominated.getFbId())) {
					currentNominatedIndex = i;
					break;
				}
			}	
			for(int i = currentNominatedIndex+1; i < players.size(); i++) {
				Player p = players.get(i);
				if(p.isNominated()) {
					nextNominated = p;
					break;
				}
			}
			if(nextNominated == null) {
				g.setState(GameState.VOTING_2);
				LupusMessage msg = new LupusMessage(MessageType.GAMESTATE, g.getOwner());
				msg.setMsg(GameState.VOTING_2.toString());
				msg.setTarget(engine.determineNextVoterInRound2(g.getLastDead()));
				msg.broadcastToPlayingPlayers(g);
				dao.ofy().put(g);
			} else {
				LupusMessage stateMsg = new LupusMessage(MessageType.GAMESTATE, null);
				stateMsg.setMsg(GameState.DEFENSE.toString());				
				stateMsg.setTarget(nextNominated);
				stateMsg.broadcastToPlayingPlayers(g);
			}
		}
	}

}
