package com.raibaz.lupus.servlet.admin;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.raibaz.lupus.dao.LupusDAO;

public class ArchiveOldGamesServlet extends HttpServlet {

	private int WAITING_GAMES_TRESHOLD = 1000 * 60 * 60 * 4;
	private int PLAYING_GAMES_TRESHOLD = 1000 * 60 * 60 * 24;
	
	private static final Logger log = Logger.getLogger("ArchiveOldGamesServlet");
	
	@Override
	protected void service(HttpServletRequest arg0, HttpServletResponse arg1)
			throws ServletException, IOException {
		
		LupusDAO dao = new LupusDAO();
				
		Calendar waitingCal = Calendar.getInstance();
		waitingCal.add(Calendar.MILLISECOND, WAITING_GAMES_TRESHOLD * -1);				
		int waiting = dao.archiveWaitingGamesOlderThan(waitingCal.getTime());
		
		log.info("Archived " + waiting + " waiting games");
		
		Calendar startedCal = Calendar.getInstance();
		startedCal.add(Calendar.MILLISECOND, PLAYING_GAMES_TRESHOLD * -1);		
		int started = dao.archiveGamesOlderThan(new Date(PLAYING_GAMES_TRESHOLD));
		
		log.info("Archived " + started + " started games");				
	}

}
