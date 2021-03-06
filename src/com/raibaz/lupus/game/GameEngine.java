package com.raibaz.lupus.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.raibaz.lupus.dao.LupusDAO;

public class GameEngine {

	private static final Logger log = Logger.getLogger(GameEngine.class.getName());
	
	private Game g;
	
	public GameEngine(Game g) {
		this.g = g;
	}
	
	public void resetAllPlayers() {
		LupusDAO dao = new LupusDAO();
		for(Player p : g.getPlayers(true)) {
			p.setAlive(true);
			p.setNominated(false);
			p.setHasVoted(false);
			p.setVotes(0);
			p.setRole(null);
			dao.ofy().put(p);
		}		
	}
	
	public void assignRoles() {
		LupusDAO dao = new LupusDAO();			
		GameConfiguration conf = g.getConfiguration();		
			
		for(int i = 0; i < conf.getHowManyWolves(); i++) {
			Player cur = getRandomAvailablePlayer();
			cur.setRole(PlayerRole.WOLF);
			dao.ofy().put(cur);
		}
		
		Player nextPlayer = null;
		if(conf.hasSeer()) {
			nextPlayer = getRandomAvailablePlayer();
			if(nextPlayer != null) {
				nextPlayer.setRole(PlayerRole.SEER);
				dao.ofy().put(nextPlayer);
			}
		}
		
		if(conf.hasMedium()) {
			nextPlayer = getRandomAvailablePlayer();
			if(nextPlayer != null) {
				nextPlayer.setRole(PlayerRole.MEDIUM);
				dao.ofy().put(nextPlayer);
			}
		}
		
		if(conf.hasIndemoniated()) {
			nextPlayer = getRandomAvailablePlayer();
			if(nextPlayer != null) {
				nextPlayer.setRole(PlayerRole.INDEMONIATED);
				dao.ofy().put(nextPlayer);
			}
		}
		
		if(conf.hasBodyguard()) {
			nextPlayer = getRandomAvailablePlayer();
			if(nextPlayer != null) {
				nextPlayer.setRole(PlayerRole.BODYGUARD);
				dao.ofy().put(nextPlayer);
			}
		}
		
		if(conf.hasOwl()) {
			nextPlayer = getRandomAvailablePlayer();
			if(nextPlayer != null) {
				nextPlayer.setRole(PlayerRole.OWL);
				dao.ofy().put(nextPlayer);
			}
		}

				
		nextPlayer = getRandomAvailablePlayer();
		while(nextPlayer != null) {
			nextPlayer.setRole(PlayerRole.CITIZEN);
			dao.ofy().put(nextPlayer);
			nextPlayer = getRandomAvailablePlayer();
		}		
		//Paranoia check
		for(Player p : g.getPlayers(true)) {
			if(p.getRole() == null) {
				p.setRole(PlayerRole.CITIZEN);
				dao.ofy().put(p);
			}
		}
		g.refreshPlayers();
	}
	
	private Player getRandomAvailablePlayer() {
		Random rand = new Random();
		//FIXME: magari tutte ste query non sono indispensabili
		ArrayList<Player> players = (ArrayList<Player>)g.getPlayers(true);
		
		for(int i = 0; i < players.size(); i++) {
			int index = rand.nextInt(players.size());
			Player currentPlayer = players.get(index);
			if(currentPlayer.getRole() != null) {
				continue;
			}		
			return currentPlayer;
		}
		return null;
	}
	
	public Player determineNextVoterInRound1(Player lastVoter) {
		ArrayList<Player> players = (ArrayList<Player>)g.getPlayers(true);
		if(lastVoter == null) {
			return players.get(0);
		} else {
			int nextVoterIndex = 0;
			for(int i = 0; i < players.size(); i++){
				if(players.get(i).getFbId().equals(lastVoter.getFbId())) {
					nextVoterIndex = i;
					do {
						nextVoterIndex++;
						if(nextVoterIndex >= players.size()) {
							nextVoterIndex = 0;
						}	
						if(log.isLoggable(Level.FINE)) {
							log.fine("NextVoterIndex = " + nextVoterIndex + ", playerId = " + players.get(nextVoterIndex).getFbId() + ", hasVoted = " + players.get(nextVoterIndex).hasVoted());
						}						
					} while(players.get(nextVoterIndex).hasVoted() && nextVoterIndex != i);										
					if(nextVoterIndex == i) {
						return null;
					}
					break;
				}
			}
			return players.get(nextVoterIndex);
		}		
	}
	
	public Player determineNextVoterInRound2(Player lastVoter) {
		ArrayList<Player> players = (ArrayList<Player>)g.getPlayers(true);
		if(lastVoter == null) {
			for(int i = 0; i < players.size(); i++) {
				if(!players.get(i).isNominated()) {
					return players.get(i);
				}
			}
			return players.get(0);
		} else {
			int nextVoterIndex = 0;
			for(int i = 0; i < players.size(); i++){
				if(players.get(i).getFbId().equals(lastVoter.getFbId())) {
					nextVoterIndex = i;
					Player cur = players.get(nextVoterIndex);
					do {
						nextVoterIndex++;								
						if(nextVoterIndex >= players.size()) {
							nextVoterIndex = 0;
						}		
						cur = players.get(nextVoterIndex);
						if(log.isLoggable(Level.FINE)) {							
							log.fine("NextVoterIndex = " + nextVoterIndex 
									+ ", playerId = " + cur.getFbId() 
									+ ", hasVoted = " + cur.hasVoted()
									+ ", isAlive = " + cur.isAlive()
									+ ", isNominated = " + cur.isNominated());
						}
					} while((cur.isNominated() || !cur.isAlive() || cur.hasVoted()) && nextVoterIndex != i);										
					if(nextVoterIndex == i) {
						return null;
					}
					break;
				}
			}
			return players.get(nextVoterIndex);
		}	
	}
	
	public Player determineNextVoterInNight(Player lastVoter) {		
		if(lastVoter == null || lastVoter.getRole() == PlayerRole.WOLF) {
			for(Player p : g.getPlayers(true)) {
				if(p.getRole() == PlayerRole.WOLF && !p.hasVoted()) {
					return p;
				}
			}
			if(computeDeadPlayerInNight() != null) {
				return g.getPlayerByRole(PlayerRole.SEER);
			} else {
				resetVotes();
				return g.getPlayerByRole(PlayerRole.WOLF);
			}
		} else if(lastVoter.getRole() == PlayerRole.SEER) {
			return g.getPlayerByRole(PlayerRole.MEDIUM);
		} else if(lastVoter.getRole() == PlayerRole.MEDIUM) {
			if(g.getConfiguration().hasBodyguard()) {
				return g.getPlayerByRole(PlayerRole.BODYGUARD);
			} else if(g.getConfiguration().hasOwl()) {
				return g.getPlayerByRole(PlayerRole.OWL);
			}
		} else if(lastVoter.getRole() == PlayerRole.BODYGUARD) {
			if(g.getConfiguration().hasOwl()) {
				return g.getPlayerByRole(PlayerRole.OWL);
			}
		}
		return null;
	}
	
		
	
	public ArrayList<Player> computeVotedIdsInRound1() {
		ArrayList<Player> ret = new ArrayList<Player>();
		LupusDAO dao = new LupusDAO();
		List<Player> players = g.getPlayers(true);
		
		int maxVotes = 0;
		for(Player p : players) {
			if(p.getVotes() > maxVotes) {
				maxVotes = p.getVotes();
			}
		}
		
		do {
			for(Player p : players) {
				if(p.getVotes() == maxVotes || p.isOwled()) {		
					log.fine("About to set nominated == true for player = " + p.getFbId());
					p.setNominated(true);					
					dao.ofy().put(p);
					persistGame();
					ret.add(p);
				}				
			}
			maxVotes--;
			//TODO l'1 � temporary, il valore giusto � 2
		} while(ret.size() < 2 || maxVotes > 0);
					
		return ret;
	}
	
	public Player computeDeadPlayerInRound2() {
		int maxVotes = 0;
		List<Player> players = g.getPlayers(true);
		for(Player p : players) {
			if(p.isAlive() && p.getVotes() > maxVotes) {
				maxVotes = p.getVotes();
			}			
		}
		for(Player p : players) {
			if(p.isAlive() && p.getVotes() == maxVotes) {
				log.info("Player " + p.getName() + " just died");
				return p;
			}
		}		
		//Should never happen		
		log.warning("Wrong situation, did not find a player to set as dead in round 2");
		return null;
	}
	
	public Player computeDeadPlayerInNight() {
		int requiredVotes = g.getConfiguration().getHowManyWolves();
		Player dead = null;
		List<Player> players = g.getPlayers(true);
		for(Player p : players) {
			if(p.getVotes() == requiredVotes) {
				dead = p;
			}
		}		
		if(dead != null && dead.isBodyguarded()) {
			return null;
		} else {
			return dead;
		}
	}
	
	public boolean hasGameEnded() {
		int countOfAliveWolves = 0;
		int countOfAliveCitizens = 0;
		for(Player p : g.getPlayers()) {
			if(p.isAlive()) {
				if(p.getRole() == PlayerRole.WOLF) {
					countOfAliveWolves++;
				} else {
					countOfAliveCitizens++;
				}
			}
		}
		return countOfAliveWolves == 0 || countOfAliveWolves >= countOfAliveCitizens;
	}
	
	public void resetVotes() {
		LupusDAO dao = new LupusDAO();
		for(Player p : g.getPlayers(true)) {			
			p.setVotes(0);
			p.setHasVoted(false);
			p.setBodyguarded(false);
			p.setOwled(false);
			dao.ofy().put(p);						
		}		
		g.refreshPlayers();
		persistGame();
	}
	
	public void updateGame(Game g) {		
		this.g = g;
	}
	
	private void persistGame() {
		LupusDAO dao = new LupusDAO();
		dao.ofy().put(g);
	}
}
