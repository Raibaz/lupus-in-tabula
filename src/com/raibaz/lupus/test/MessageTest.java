package com.raibaz.lupus.test;

import org.junit.Rule;
import org.junit.Test;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.raibaz.lupus.game.Player;

public class MessageTest extends BaseTest {
	
	@Rule
	public EmbeddedChannelService chan = new EmbeddedChannelService();
	
	@Test
	public void testMessaging() {
		ChannelService chanServ = ChannelServiceFactory.getChannelService();
		for(Player p : g.getPlayers()) {
			String channeltoken = chanServ.createChannel(p.getFbId() + "-playing");
			chanServ.sendMessage(new ChannelMessage(channeltoken, "banana"));
		}
			
	}

}
