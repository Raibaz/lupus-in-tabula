package com.raibaz.lupus.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import com.raibaz.lupus.game.PlayerRole;

public class VoteServlet extends HttpServlet {
	
	private static final Logger log = Logger.getLogger(VoteServlet.class.getName());

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		String gameId = req.getParameter("game_id");
		String voterId = req.getParameter("voter_id");
		String targetId = req.getParameter("target_id");
		
		LupusDAO dao = new LupusDAO();
		
		Game g = dao.ofy().get(Game.class, gameId);		
		Player voter = g.getPlayer(voterId);		
		Player target = g.getPlayer(targetId);
		
		
		voter.setHasVoted(true);
		dao.ofy().put(voter);
		target.setVotes(target.getVotes()+1);
		dao.ofy().put(target);
								
		GameEngine engine = new GameEngine(g);
		Player nextVoter = null;
		if(g.getState() == GameState.VOTING_1) {
			nextVoter = engine.determineNextVoterInRound1(voter);			
		} else if(g.getState() == GameState.VOTING_2) {
			nextVoter = engine.determineNextVoterInRound2(voter);
		} else if(g.getState() == GameState.NIGHT) {
			nextVoter = engine.determineNextVoterInNight(voter);
		}
		
		LupusMessage msg = new LupusMessage(MessageType.VOTE, voter);
		msg.setTarget(target);
		msg.setNext(nextVoter);
		if(g.getState() == GameState.NIGHT) {
			msg.broadcastToPlayersByRole(g, voter.getRole());
		} else{
			msg.broadcastToPlayingPlayers(g);
		}
		
				
		if(nextVoter == null) {
			//Votazioni terminate
			if(g.getState() == GameState.VOTING_1) {
				g.setState(GameState.VOTING_2);
				ArrayList<Player> votedPlayers = engine.computeVotedIdsInRound1();
				engine.resetVotes();
				LupusMessage stateMsg = new LupusMessage(MessageType.GAMESTATE, voter);
				stateMsg.setMsg(GameState.VOTING_2.toString());
				stateMsg.setNominated(votedPlayers);				
				stateMsg.setTarget(engine.determineNextVoterInRound2(g.getLastDead()));
				stateMsg.broadcastToPlayingPlayers(g);
			} else if(g.getState() == GameState.VOTING_2) {
				g.setState(GameState.NIGHT);
				Player deadPlayer = engine.computeDeadPlayerInRound2();
				if(deadPlayer != null) {					
					deadPlayer.setAlive(false);
					dao.ofy().put(deadPlayer);				
					engine.resetVotes();
																
					LupusMessage stateMsg = new LupusMessage(MessageType.GAMESTATE, voter);
					stateMsg.setTarget(deadPlayer);
					stateMsg.setMsg(GameState.NIGHT.toString());
					if(log.isLoggable(Level.FINE)) {
						log.fine("About to send message stating that after a vote by " + voter.getName() + ", " + deadPlayer.getName() + " died.");
					}
					stateMsg.broadcastToPlayingPlayers(g);
					
					LupusMessage wolfMessage = new LupusMessage(MessageType.NIGHTVOTE, null);
					wolfMessage.setMsg(PlayerRole.WOLF.toString());
					wolfMessage.setNext(engine.determineNextVoterInNight(null));					
					wolfMessage.broadcastToPlayersByRole(g, PlayerRole.WOLF);	
															
					if(engine.hasGameEnded()) {
						g.setState(GameState.ENDED);
						LupusMessage endMsg = new LupusMessage(MessageType.GAMESTATE, voter);
						endMsg.setMsg(deadPlayer.getRole().toString());
						endMsg.broadcastToPlayingPlayers(g);
						
					} 					
									
				}
			} else if(g.getState() == GameState.NIGHT) {								
				Player deadPlayer = engine.computeDeadPlayerInNight();
				if(deadPlayer != null) {
					deadPlayer.setAlive(false);
					dao.ofy().put(deadPlayer);
					engine.resetVotes();
					if(g.getConfiguration().hasSeer()) {
						LupusMessage seerMessage = new LupusMessage(MessageType.NIGHTVOTE, null);
						seerMessage.setMsg(PlayerRole.SEER.toString());
						seerMessage.broadcastToPlayersByRole(g, PlayerRole.SEER);
					} else {
						LupusMessage dayMessage = new LupusMessage(MessageType.GAMESTATE, null);
						dayMessage.setMsg(GameState.DEBATE.toString());
						dayMessage.setTarget(deadPlayer);
						dayMessage.broadcastToPlayingPlayers(g);
					}
				}
			}
			
		}
			
		dao.ofy().put(g);
	}
}
