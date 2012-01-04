package com.raibaz.lupus.servlet;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.raibaz.lupus.LupusMessage;
import com.raibaz.lupus.LupusMessage.MessageType;
import com.raibaz.lupus.dao.LupusDAO;
import com.raibaz.lupus.game.Game;
import com.raibaz.lupus.game.Player;
import com.raibaz.lupus.game.PlayerRole;

public class GetRoleServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String gameId = req.getParameter("game_id");
		String playerId = req.getParameter("player_id");
		
		LupusDAO dao = new LupusDAO();
		Game g = dao.ofy().find(Game.class, gameId);
		Player p = dao.ofy().find(Player.class, playerId);
		
		LupusMessage roleMsg = new LupusMessage(MessageType.ROLEINFO, p);
		roleMsg.setMsg(p.getRole().toString());
		
		if(p.getRole() == PlayerRole.WOLF) {
			ArrayList<Player> wolves = new ArrayList<Player>();
			for(Player cur : g.getPlayers(true)) {
				if(cur.getRole() == PlayerRole.WOLF && !cur.getFbId().equals(p.getFbId())) {
					wolves.add(cur);
				}
			}
			roleMsg.setNominated(wolves);
		}
		resp.getWriter().write(roleMsg.toJSONString());		
	}	
}
