package com.raibaz.lupus.game;

public enum PlayerRole {
	WOLF,
	SEER,
	MEDIUM,
	INDEMONIATED,
	BODYGUARD,
	OWL,
	CITIZEN;
	
	public boolean isWolf() {
		return this == WOLF;
	}
}
