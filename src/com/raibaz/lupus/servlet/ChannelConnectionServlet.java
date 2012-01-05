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

public class ChannelConnectionServlet extends HttpServlet {

	private static final Logger log = Logger.getLogger("ChannelConnectionServlet");
	
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		ChannelService chanServ = ChannelServiceFactory.getChannelService();
		ChannelPresence presence = chanServ.parsePresence(req);
		
		log.info("Received connection from channel with clientId = " + presence.clientId());
		
		ChannelPool.updatePresence(presence.clientId(), true);
		
		super.service(req, resp);
	}	
}
