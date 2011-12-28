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
			if(voter.getRole() == PlayerRole.SEER) {
				LupusMessage seerMessage = new LupusMessage(MessageType.NIGHTINFO, voter);
				seerMessage.setTarget(target);
				seerMessage.setMsg("" + target.getRole().isWolf());
				seerMessage.broadcastToPlayersByRole(g, PlayerRole.SEER);				
			} else if(voter.getRole() == PlayerRole.BODYGUARD) {
				target.setBodyguarded(true);
				LupusMessage seerMessage = new LupusMessage(MessageType.NIGHTINFO, voter);
				seerMessage.setTarget(target);				
				seerMessage.broadcastToPlayersByRole(g, PlayerRole.BODYGUARD);
			} else if(voter.getRole() == PlayerRole.OWL) {
				target.setOwled(true);
				LupusMessage seerMessage = new LupusMessage(MessageType.NIGHTINFO, voter);
				seerMessage.setTarget(target);				
				seerMessage.broadcastToPlayersByRole(g, PlayerRole.OWL);
			}
			
			nextVoter = engine.determineNextVoterInNight(voter);
			if(nextVoter != null) {
				notifyNextCharacter(g, nextVoter);
				//Il medium non vota
				if(nextVoter.getRole() == PlayerRole.MEDIUM) {
					LupusMessage mediumMessage = new LupusMessage(MessageType.NIGHTINFO, null);
					mediumMessage.setTarget(g.getLastDead());
					mediumMessage.setMsg("" + g.getLastDead().getRole().isWolf());
					mediumMessage.broadcastToPlayersByRole(g, nextVoter.getRole());
					nextVoter = engine.determineNextVoterInNight(nextVoter);
					if(nextVoter != null) {
						notifyNextCharacter(g, nextVoter);
					} else {
						handleVotesFinished(g, voter);
						dao.ofy().put(g);
						return;
					}
				}
				dao.ofy().put(g);												
				if(nextVoter.getRole() != PlayerRole.WOLF) {
					return;
				}
			}
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
			handleVotesFinished(g, voter);
			
		}
			
		dao.ofy().put(g);
	}

	private void notifyNextCharacter(Game g, Player nextVoter) {
		LupusMessage stateMsg = new LupusMessage(MessageType.GAMESTATE, null);			
		stateMsg.setMsg(nextVoter.getRole().toString());
		stateMsg.broadcastToPlayingPlayers(g);
		
		if(nextVoter.getRole() != PlayerRole.MEDIUM) {
			LupusMessage nextMsg = new LupusMessage(MessageType.NIGHTVOTE, null);
			nextMsg.setMsg(nextVoter.getRole().toString());
			nextMsg.setNext(nextVoter);
			nextMsg.broadcastToPlayersByRole(g, nextVoter.getRole());
		}
	}

	private void handleVotesFinished(Game g, Player voter) {
		GameEngine engine = new GameEngine(g);
		LupusDAO dao = new LupusDAO();
		if(g.getState() == GameState.VOTING_1) {
			g.setState(GameState.DEFENSE);
			ArrayList<Player> votedPlayers = engine.computeVotedIdsInRound1();
			engine.resetVotes();
			LupusMessage stateMsg = new LupusMessage(MessageType.GAMESTATE, voter);
			stateMsg.setMsg(GameState.DEFENSE.toString());
			stateMsg.setNominated(votedPlayers);
			stateMsg.setTarget(votedPlayers.get(0));
			//stateMsg.setTarget(engine.determineNextVoterInRound2(g.getLastDead()));
			stateMsg.broadcastToPlayingPlayers(g);
		} else if(g.getState() == GameState.VOTING_2) {
			g.setState(GameState.NIGHT);
			Player deadPlayer = engine.computeDeadPlayerInRound2();
			if(deadPlayer != null) {					
				deadPlayer.setAlive(false);
				g.setLastDead(deadPlayer);
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
					endMsg.setMsg(GameState.ENDED.toString());
					endMsg.broadcastToPlayingPlayers(g);						
				} 					
								
			}
		} else if(g.getState() == GameState.NIGHT) {								
			Player deadPlayer = engine.computeDeadPlayerInNight();
			if(deadPlayer != null) {
				deadPlayer.setAlive(false);
				dao.ofy().put(deadPlayer);
				g.setLastDead(deadPlayer);
			}
			engine.resetVotes();

			LupusMessage dayMessage = new LupusMessage(MessageType.GAMESTATE, null);
			dayMessage.setMsg(GameState.DEBATE.toString());
			dayMessage.setTarget(deadPlayer);
			dayMessage.broadcastToPlayingPlayers(g);				
		}
	}
}
