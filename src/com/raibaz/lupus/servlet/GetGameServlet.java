package com.raibaz.lupus.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.raibaz.lupus.dao.LupusDAO;
import com.raibaz.lupus.game.Game;

public class GetGameServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)	throws ServletException, IOException {
		String gameId = req.getParameter("game_id");
		LupusDAO dao = new LupusDAO();
		
		Game g = dao.ofy().get(Game.class, gameId);
		if(g == null) {
			return;
		}
		
		resp.setContentType("text/plain");
		resp.getWriter().write(g.toJSONString());		
	}
	

}
