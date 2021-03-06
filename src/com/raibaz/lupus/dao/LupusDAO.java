package com.raibaz.lupus.dao;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.DAOBase;
import com.raibaz.lupus.channel.LupusPresence;
import com.raibaz.lupus.game.Game;
import com.raibaz.lupus.game.GameState;
import com.raibaz.lupus.game.Invite;
import com.raibaz.lupus.game.Player;

public class LupusDAO extends DAOBase {

	static {
		ObjectifyService.register(Player.class);
		ObjectifyService.register(Game.class);
		ObjectifyService.register(Invite.class);
		ObjectifyService.register(LupusPresence.class);
	}
	
	public Player getPlayer(String fbId) {
		return ofy().find(Player.class, fbId);
	}
	
	public List<Game> listWaitingGames() {
		return ofy().query(Game.class).filter("state", GameState.WAITING).list();
	}
	
	public Game getOwnedGame(Player p) {
		List<Game> games = ofy().query(Game.class).filter("owner.fbId", p.getFbId()).list();
		if(!games.isEmpty()) {
			for(Game g : games) {
				if(g.getState() != GameState.ARCHIVED) {
					return g;
				}
			}
			return null;
		} else {
			return null;
		}
	}
	
	public List<Invite> getUserInvites(String playerId) {
		return ofy().query(Invite.class).filter("invitedId", playerId).list();		
	}
	
	public void deleteGameInvites(String gameId) {
		List<Invite> invites = ofy().query(Invite.class).filter("gameId", gameId).list();
		for(Invite i : invites) {
			ofy().delete(i);
		}
	}
	
	public int archiveWaitingGamesOlderThan(Date treshold) {
		List<Game> olderGames = ofy().query(Game.class).filter("creationDate < ", treshold).filter("state", GameState.WAITING.toString()).list();
		int ret = 0;
		for(Game g : olderGames) {
			if(g.getState() == GameState.WAITING) {
				g.setState(GameState.ARCHIVED);				
				ofy().put(g);			
				List<Invite> invites = ofy().query(Invite.class).filter("gameId", g.getId()).list();
				for(Invite i : invites) {
					ofy().delete(i);
				}
				ret++;
			}
		}		
		return ret;
	}
	
	public int archiveGamesOlderThan(Date treshold) {
		List<Game> olderGames = ofy().query(Game.class).filter("creationDate < ", treshold).list();
		int ret = 0;
		for(Game g : olderGames) {
			if(g.getState() != GameState.ARCHIVED) {
				g.setState(GameState.ARCHIVED);
				ofy().put(g);
				List<Invite> invites = ofy().query(Invite.class).filter("gameId", g.getId()).list();
				for(Invite i : invites) {
					ofy().delete(i);
				}
				ret++;
			}
		}
		return ret;
	}
	
	public LupusPresence getFirstAvailablePresence() {
		Calendar treshCal = Calendar.getInstance();
		treshCal.add(Calendar.MILLISECOND, 1000 * 60 * 15 * -1);
		List<LupusPresence> presences = ofy().query(LupusPresence.class).filter("assigned", Boolean.FALSE).filter("lastSeen <", treshCal.getTime()).order("lastSeen").limit(1).list();
		if(presences.size() == 1) {
			return presences.get(0);
		} else {
			return null;
		}
	}
	
	public int disconnectPresencesOlderthan(Date treshold) {
		List<LupusPresence> presences = ofy().query(LupusPresence.class).filter("connected", Boolean.TRUE).filter("lastSeen <", treshold).list();
		for(LupusPresence lp : presences) {			
			ofy().delete(lp);
		}
		
		int deleted = presences.size();
		
		presences = ofy().query(LupusPresence.class).filter("connected", Boolean.FALSE).filter("assigned", Boolean.TRUE).filter("lastSeen <", treshold).list();
		for(LupusPresence lp : presences) {
			lp.setAssigned(false);
			ofy().put(lp);
		}
		
		return presences.size() + deleted;
	}
	
}
