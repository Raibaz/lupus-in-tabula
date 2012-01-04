package com.raibaz.lupus.game;

import javax.persistence.Id;

import com.googlecode.objectify.annotation.Unindexed;

public class Invite extends AbsJsonable {
	
	@Id
	private Long id;
	
	private String invitedId;
	private String inviterId;
	private String gameId;	

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getInvitedId() {
		return invitedId;
	}
	public void setInvitedId(String invitedId) {
		this.invitedId = invitedId;
	}
	public String getGameId() {
		return gameId;
	}
	public void setGameId(String gameId) {
		this.gameId = gameId;
	}
	public String getInviterId() {
		return inviterId;
	}
	public void setInviterId(String inviterId) {
		this.inviterId = inviterId;
	}
}
