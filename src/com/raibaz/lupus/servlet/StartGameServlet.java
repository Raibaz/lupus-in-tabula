package com.raibaz.lupus.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.raibaz.lupus.LupusMessage;
import com.raibaz.lupus.LupusMessage.MessageType;
import com.raibaz.lupus.dao.LupusDAO;
import com.raibaz.lupus.game.Game;
import com.raibaz.lupus.game.GameEngine;
import com.raibaz.lupus.game.GameState;
import com.raibaz.lupus.game.Player;

public class StartGameServlet extends HttpServlet {
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String gameId = req.getParameter("game_id");
		
		LupusDAO dao = new LupusDAO();
		
		Game g = dao.ofy().get(Game.class, gameId);
		g.setState(GameState.NIGHT);
		g.initConfiguration();
		GameEngine engine = new GameEngine(g);
		engine.resetAllPlayers();		
		engine.assignRoles();
		
		ChannelService chanServ = ChannelServiceFactory.getChannelService();
		LupusMessage msg = new LupusMessage(MessageType.GAMESTATE, g.getOwner());		
		msg.setGameId(g.getId());
		
		for(Player p : g.getPlayers()) {
			String newChannelToken = chanServ.createChannel(p.getFbId() + "-playing");		
			msg.setTarget(p);
			msg.setMsg("start__" + newChannelToken);
			chanServ.sendMessage(new ChannelMessage(p.getFbId() + "-waiting", msg.toJSONString()));
		}		
	}

}
