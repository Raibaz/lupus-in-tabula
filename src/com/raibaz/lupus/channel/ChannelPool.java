package com.raibaz.lupus.channel;

import java.util.Date;
import java.util.logging.Logger;

import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.raibaz.lupus.dao.LupusDAO;

public class ChannelPool {
	
	private static final Logger log = Logger.getLogger("ChannelPool");
	
	public static LupusPresence getPresence() {
		LupusDAO dao = new LupusDAO();
		
		LupusPresence presence = dao.getFirstAvailablePresence();
		if(presence == null) {
			presence = new LupusPresence();
			presence.setClientId("" + new Date().getTime());
			ChannelService chanServ = ChannelServiceFactory.getChannelService();
			presence.setChannelToken(chanServ.createChannel(presence.getClientId()));
			dao.ofy().put(presence);
		} else {
			log.info("Recycling presence with clientId = " + presence.getClientId());
		}
				
		return presence;
	}
	
	public static void updatePresence(String clientId, boolean status) {
		LupusDAO dao = new LupusDAO();
		LupusPresence presence = dao.ofy().find(LupusPresence.class, clientId);
		if(presence != null) {
			presence.setConnected(status);
			dao.ofy().put(presence);
		}
	}
}
