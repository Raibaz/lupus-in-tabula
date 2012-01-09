package com.raibaz.lupus.servlet;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.channel.ChannelPresence;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.raibaz.lupus.channel.ChannelPool;

public class ChannelDisconnectionServlet extends HttpServlet {

	private static final Logger log = Logger.getLogger("ChannelDisconnectionServlet");
	
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		ChannelService chanServ = ChannelServiceFactory.getChannelService();
		ChannelPresence presence = chanServ.parsePresence(req);
		
		log.info("Detected disconnection from client with clientId = " + presence.clientId());
		
		ChannelPool.updatePresence(presence.clientId(), false, false);
		
		super.service(req, resp);
	}
		
}
