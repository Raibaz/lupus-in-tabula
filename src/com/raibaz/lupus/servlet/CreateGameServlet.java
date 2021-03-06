package com.raibaz.lupus.servlet;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.raibaz.lupus.channel.ChannelPool;
import com.raibaz.lupus.channel.LupusPresence;
import com.raibaz.lupus.dao.LupusDAO;
import com.raibaz.lupus.game.Game;
import com.raibaz.lupus.game.Player;

public class CreateGameServlet extends HttpServlet {

	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		Game g = new Game();
		
		String playerId = req.getParameter("player_id");
		
		LupusDAO dao = new LupusDAO();
		
		Player owner = dao.getPlayer(playerId);
		if(owner == null) {			
			return;
		}
							
		g.setName("Partita");		
		g.setId(owner.getFbId() + "-" + new Date().getTime());
		g.setCreationDate(new Date());
		
		ChannelService chanServ = ChannelServiceFactory.getChannelService();
		//String chanToken = chanServ.createChannel(owner.getFbId() + "-waiting");
		LupusPresence channelPresence = ChannelPool.getPresence();
		String chanToken = channelPresence.getChannelToken();
		owner.setChannelClientId(channelPresence.getClientId());
		dao.ofy().put(owner);
		
		g.setOwner(owner);
		g.getPlayers().add(owner);
		
		dao.ofy().put(g);
		
		resp.setContentType("text/plain");
		JSONObject jsonResp = new JSONObject(g);
		try { 
			jsonResp.put("channelToken", chanToken);
		} catch (JSONException jsone) {
			jsone.printStackTrace();
		}
		resp.getWriter().write(jsonResp.toString());
	}
}
