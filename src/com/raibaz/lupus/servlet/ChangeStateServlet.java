package com.raibaz.lupus.servlet;

import java.io.IOException;

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

public class ChangeStateServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String gameId = req.getParameter("game_id");
		String stateStr = req.getParameter("state");
		LupusDAO dao = new LupusDAO();
		
		Game g = dao.ofy().find(Game.class, gameId);
		
		GameState state = GameState.valueOf(stateStr);
		g.setState(state);
		
		GameEngine engine = new GameEngine(g);
								
		engine.resetVotes();
		LupusMessage msg = new LupusMessage(MessageType.GAMESTATE, g.getOwner());
		msg.setMsg(stateStr);
		msg.setTarget(engine.determineNextVoterInRound1(g.getLastDead()));
		msg.broadcastToPlayingPlayers(g);
		dao.ofy().put(g);
		
		
	}

}
