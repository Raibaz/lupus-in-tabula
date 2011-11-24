package com.raibaz.lupus;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;

public class SendMessageServlet extends HttpServlet {
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String channelToken = req.getParameter("channel");
		ChannelService chanServ = ChannelServiceFactory.getChannelService();
		chanServ.sendMessage(new ChannelMessage(channelToken, "badabum cha cha"));
	}
	

}
