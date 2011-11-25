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
import com.raibaz.lupus.game.Player;

public class ChatServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String gameId = req.getParameter("game_id");
		String playerId = req.getParameter("player_id");
		String msg = req.getParameter("msg");
		LupusDAO dao = new LupusDAO();
		
				
		Game g = dao.ofy().get(Game.class, gameId);
		Player p = dao.ofy().get(Player.class, playerId);
		if(!p.isAlive()) {
			return;
		}
		
		LupusMessage message = new LupusMessage(MessageType.CHAT, p);
		message.setMsg(msg);
		message.broadcastToPlayingPlayers(g);					
	}
}
