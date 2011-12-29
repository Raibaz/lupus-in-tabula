package com.raibaz.lupus.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.log.Log;

import com.google.appengine.repackaged.org.json.JSONArray;
import com.raibaz.lupus.dao.LupusDAO;
import com.raibaz.lupus.game.Game;
import com.raibaz.lupus.game.Invite;

public class GameListServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger("GameListServlet");
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		
		LupusDAO dao = new LupusDAO();
		
		String playerId = req.getParameter("player_id");
		List<Invite> invites = new ArrayList<Invite>();
		if(playerId != null) {			
			invites = dao.getUserInvites(playerId);			
		}
		
		List<Game> games = dao.listWaitingGames();		
		for(Invite i : invites) {
			for(Game g : games) {
				if(g.getId().equals(i.getGameId())) {					
					g.setInvited(true);
				}				
			}
		}
				
		JSONArray arr = new JSONArray(games);		
		resp.setContentType("text/plain");
		resp.getWriter().write(arr.toString());		
	}
}
