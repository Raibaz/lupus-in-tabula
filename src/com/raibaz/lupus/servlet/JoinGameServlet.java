package com.raibaz.lupus.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.raibaz.lupus.LupusMessage;
import com.raibaz.lupus.LupusMessage.MessageType;
import com.raibaz.lupus.dao.LupusDAO;
import com.raibaz.lupus.game.Game;
import com.raibaz.lupus.game.Player;

public class JoinGameServlet extends HttpServlet {
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String gameId = req.getParameter("game");
		String playerId = req.getParameter("player_id");
		
		LupusDAO dao = new LupusDAO();
		
		Game g = dao.ofy().find(Game.class, gameId);
		Player p = dao.ofy().find(Player.class, playerId);
		if(g == null || p == null) {
			//TODO Errore!
		}
		
		
		//TODO fai l'add solo se il giocatore manca
		if(!g.getPlayers().contains(p)) {
			g.getPlayers().add(p);
			dao.ofy().put(g);
		}
		
		ChannelService chanServ = ChannelServiceFactory.getChannelService();
		
		String channelToken = chanServ.createChannel(p.getFbId() + "-waiting");
		
		LupusMessage msg = new LupusMessage(MessageType.JOIN, p);
		for(Player cur : g.getPlayers()) {
			Player persisted = dao.ofy().get(Player.class, cur.getFbId());					
			chanServ.sendMessage(new ChannelMessage(persisted.getFbId() + "-waiting", msg.toJSONString()));
			
		}
		
		JSONObject jsonResp = new JSONObject(g);
		try {
			jsonResp.put("channelToken", channelToken);
		} catch (JSONException je) {
			je.printStackTrace();
		}
		resp.setContentType("text/plain");
		resp.getWriter().write(jsonResp.toString());		
	}

}
