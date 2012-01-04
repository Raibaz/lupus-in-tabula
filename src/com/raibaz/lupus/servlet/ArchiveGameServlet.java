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
import com.raibaz.lupus.game.GameState;
import com.raibaz.lupus.game.Player;

public class ArchiveGameServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		String gameId = req.getParameter("game_id");
		String playerId = req.getParameter("player_id");
		
		LupusDAO dao = new LupusDAO();
		
		Game g = dao.ofy().find(Game.class, gameId);
		Player p = dao.ofy().find(Player.class, playerId);
		if(!g.getOwner().getFbId().equals(p.getFbId())) {
			return;
		}
		
		g.setState(GameState.ARCHIVED);
		dao.ofy().put(g);
		dao.deleteGameInvites(g.getId());
		
		LupusMessage archivedMsg = new LupusMessage(MessageType.GAMESTATE, p);
		archivedMsg.setMsg(GameState.ARCHIVED.toString());
		archivedMsg.broadcastToPlayingPlayers(g);
		archivedMsg.broadcastToWaitingPlayers(g);
	}
}
