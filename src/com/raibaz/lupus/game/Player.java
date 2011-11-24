package com.raibaz.lupus.game;

import javax.persistence.Id;

public class Player extends AbsJsonable {
	
	private String name;
	@Id
	private String fbId;
	private String pictureUrl;
	private String fbToken;
	private PlayerRole role;
	
	private int votes;
	private boolean hasVoted = false;
	private boolean nominated = false;
	private boolean alive = true;	
			
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof Player)) {
			return false;
		}
		Player other = (Player)o;
		return fbId.equals(other.getFbId());
	}
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getFbId() {
		return fbId;
	}
	public void setFbId(String fbId) {
		this.fbId = fbId;
	}
	public String getPictureUrl() {
		return pictureUrl;
	}
	public void setPictureUrl(String pictureUrl) {
		this.pictureUrl = pictureUrl;
	}
	public String getFbToken() {
		return fbToken;
	}
	public void setFbToken(String fbToken) {
		this.fbToken = fbToken;
	}

	@JsonVisibility(hide=true)
	public PlayerRole getRole() {
		return role;
	}
	public void setRole(PlayerRole role) {
		this.role = role;
	}
	public int getVotes() {
		return votes;
	}
	public void setVotes(int votes) {
		this.votes = votes;
	}
	public boolean isAlive() {
		return alive;
	}
	public void setAlive(boolean alive) {
		this.alive = alive;
	}
	public boolean hasVoted() {
		return hasVoted;
	}
	public void setHasVoted(boolean hasVoted) {
		this.hasVoted = hasVoted;
	}
	public boolean isNominated() {
		return nominated;
	}
	public void setNominated(boolean nominated) {
		this.nominated = nominated;
	}
}
