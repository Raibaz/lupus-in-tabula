package com.raibaz.lupus.channel;

import java.util.Date;

import javax.persistence.Id;
import javax.persistence.PrePersist;

public class LupusPresence {
	
	@Id
	private String clientId;
	
	private String channelToken;
	
	private boolean connected;
	private Date lastSeen;
	
	@PrePersist
	private void prePersist() {
		lastSeen = new Date();
	}
	
	public String getChannelToken() {
		return channelToken;
	}

	public void setChannelToken(String channelToken) {
		this.channelToken = channelToken;
	}

	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public boolean isConnected() {
		return connected;
	}
	public void setConnected(boolean connected) {
		this.connected = connected;
	}
	public Date getLastSeen() {
		return lastSeen;
	}
	public void setLastSeen(Date lastSeen) {
		this.lastSeen = lastSeen;
	}

}
