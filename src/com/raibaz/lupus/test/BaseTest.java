package com.raibaz.lupus.test;

import java.util.Date;

import org.junit.Before;

import com.raibaz.lupus.dao.LupusDAO;
import com.raibaz.lupus.game.Game;
import com.raibaz.lupus.game.GameEngine;
import com.raibaz.lupus.game.Player;

public abstract class BaseTest {
	
	
	protected Game g;
	protected Player raibaz = new Player();
	protected Player mattia = new Player();
	protected Player silvia = new Player();
	protected Player gianluca = new Player();
	protected Player mighel = new Player();
	protected GameEngine engine;
	protected LupusDAO dao = new LupusDAO();
	
	@Before
	public void setUp() {
		raibaz.setFbId("1234567890");
		raibaz.setName("Raibaz Raibansani");
		mattia.setFbId("0987654321");
		mattia.setName("Mattia Tommasone");
		silvia.setFbId("2468013579");
		silvia.setName("Silvia Clarin");
		gianluca.setFbId("1357902468");
		gianluca.setName("Gianluca Fanuppi");
		mighel.setFbId("423343421231");
		mighel.setName("Mighel Dannopoli");
		g = new Game();
		g.setOwner(raibaz);
		g.setId(raibaz.getFbId() + "-" + new Date());
		g.getPlayers().add(raibaz);
		g.getPlayers().add(mattia);
		g.getPlayers().add(silvia);						
		engine = new GameEngine(g);
	}

}
