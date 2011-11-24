package com.raibaz.lupus.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.repackaged.org.json.JSONArray;
import com.raibaz.lupus.dao.LupusDAO;
import com.raibaz.lupus.game.Game;

public class GameListServlet extends HttpServlet {
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		LupusDAO dao = new LupusDAO();
		
		List<Game> games = dao.listWaitingGames();
		JSONArray arr = new JSONArray(games);
		resp.setContentType("text/plain");
		resp.getWriter().write(arr.toString());		
	}
}
