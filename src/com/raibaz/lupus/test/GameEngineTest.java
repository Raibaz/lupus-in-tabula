package com.raibaz.lupus.test;

import java.util.ArrayList;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;

import com.raibaz.lupus.dao.LupusDAO;
import com.raibaz.lupus.game.Game;
import com.raibaz.lupus.game.Player;
import com.raibaz.lupus.game.PlayerRole;

public class GameEngineTest extends BaseTest {

	@Rule
	public EmbeddedDataStore store = new EmbeddedDataStore();
		
	@Test
	public void testAssignRoles() {
		g.getPlayers().add(gianluca);
		g.getPlayers().add(mighel);
		engine.updateGame(g);
		
		engine.assignRoles();
		ArrayList<Player> players = (ArrayList<Player>)g.getPlayers(true);
		int countWolves = 0;
		int countSeers = 0;
		int countMediums = 0;
		int countCitizens = 0;
		int countIndemoniated = 0;
		for(Player p : players) {
			if(p.getRole() == PlayerRole.WOLF) {
				countWolves++;
			} else if(p.getRole() == PlayerRole.SEER) {
				countSeers++;
			} else if(p.getRole() == PlayerRole.MEDIUM) {
				countMediums++;
			} else if(p.getRole() == PlayerRole.CITIZEN) {
				countCitizens++;
			} else if(p.getRole() == PlayerRole.INDEMONIATED) {
				countIndemoniated++;
			}
		}
				
		Assert.assertEquals(2, countWolves);
		Assert.assertEquals(0, countSeers);
		Assert.assertEquals(0, countMediums);
		Assert.assertEquals(1, countIndemoniated);
		Assert.assertEquals(2, countCitizens);
		Assert.assertEquals(players.size(), (countWolves + countMediums + countSeers + countCitizens + countIndemoniated));
		
	}
	
	@Test
	public void testRound1() {		
		Player nextVoter = engine.determineNextVoterInRound1(null);
		Assert.assertEquals(nextVoter.getName(), raibaz.getName());
		raibaz.setHasVoted(true);
		dao.ofy().put(raibaz);
		mattia.setVotes(mattia.getVotes()+1);
		dao.ofy().put(mattia);
		nextVoter = engine.determineNextVoterInRound1(raibaz);
		Assert.assertEquals(nextVoter.getName(), mattia.getName());
		mattia.setHasVoted(true);
		dao.ofy().put(mattia);
		raibaz.setVotes(raibaz.getVotes()+1);
		dao.ofy().put(raibaz);
		nextVoter = engine.determineNextVoterInRound1(mattia);
		Assert.assertEquals(nextVoter.getName(), silvia.getName());
		silvia.setHasVoted(true);
		dao.ofy().put(silvia);
		raibaz.setVotes(raibaz.getVotes()+1);
		dao.ofy().put(raibaz);
		nextVoter = engine.determineNextVoterInRound1(silvia);
		Assert.assertNull(nextVoter);
		ArrayList<Player> nominated = engine.computeVotedIdsInRound1();
		Assert.assertEquals(nominated.size(), 2);
		Assert.assertEquals(nominated.get(0).getName(), raibaz.getName());
		Assert.assertEquals(nominated.get(1).getName(), mattia.getName());
		
		engine.resetVotes();
//		Assert.assertEquals(raibaz.isNominated(), true);
//		Assert.assertEquals(mattia.isNominated(), true);
//		Assert.assertEquals(silvia.isNominated(), false);
		Assert.assertEquals(raibaz.getVotes(), 0);
		Assert.assertEquals(mattia.getVotes(), 0);
		Assert.assertEquals(silvia.getVotes(), 0);
		
		nextVoter = engine.determineNextVoterInRound2(null);
		Assert.assertEquals(nextVoter.getName(), silvia.getName());
		raibaz.setVotes(raibaz.getVotes()+1);
		mattia.setHasVoted(true);
		nextVoter = engine.determineNextVoterInRound2(silvia);
		Assert.assertNull(nextVoter);
		Player dead = engine.computeDeadPlayerInRound2();
		Assert.assertEquals(dead.getName(), raibaz.getName());		
	}
	
	@Test
	public void testNight() {
		g.getPlayers().add(gianluca);
		g.getPlayers().add(mighel);
		engine.updateGame(g);
		
		engine.assignRoles();
		engine.resetVotes();
		Player firstWolf = null;
		for(Player p : g.getPlayers(true)) {
			if(p.getRole() == PlayerRole.WOLF) {
				firstWolf = p;
				break;
			}
		}
		Assert.assertNotNull(firstWolf);
		
		firstWolf.setHasVoted(true);
		raibaz.setVotes(raibaz.getVotes()+1);
		
		Player nextWolf = engine.determineNextVoterInNight(firstWolf);
		Assert.assertNotNull(nextWolf);
		
		nextWolf.setHasVoted(true);
		raibaz.setVotes(raibaz.getVotes()+1);
		
		nextWolf = engine.determineNextVoterInNight(nextWolf);
		Assert.assertNull(nextWolf);
		
		Player deadPlayer = engine.computeDeadPlayerInNight();
		Assert.assertEquals(deadPlayer.getName(), raibaz.getName());
		
	}
	
	@Test
	public void testPersistGame() {
		engine.assignRoles();
		engine.resetVotes();
		LupusDAO dao = new LupusDAO();
		g = dao.ofy().get(Game.class, g.getId());
		for(Player p : g.getPlayers()) {
			Assert.assertNotNull(p.getRole());
		}
		for(Player p : g.getPlayers(true)) {
			Assert.assertNotNull(p.getRole());
		}
	}
}
