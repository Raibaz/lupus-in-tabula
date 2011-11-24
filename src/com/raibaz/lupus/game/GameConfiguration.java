package com.raibaz.lupus.game;

public class GameConfiguration extends AbsJsonable {
	
	private int howManyWolves = 0;
	private boolean hasSeer = false;
	private boolean hasMedium = false;
	private boolean hasBodyguard = false;
	private boolean hasOwl = false;
	private boolean hasIndemoniated = false;

	private GameConfiguration() {
		
	}
	
	public static GameConfiguration getDefaultConfiguration(int numberOfPlayers) {
		GameConfiguration ret = new GameConfiguration();
		if(numberOfPlayers == 2) {
			ret.setHowManyWolves(1);
		} else if(numberOfPlayers < 10) {
			ret.setHowManyWolves(2);
			ret.setHasOwl(false);
		} else {
			ret.setHowManyWolves(3);
			ret.setHasOwl(true);
		}
		if(numberOfPlayers < 8) {
			ret.setHasSeer(false);
			ret.setHasMedium(false);
		} else {
			ret.setHasSeer(true);
			ret.setHasMedium(true);
		}
		
		if(numberOfPlayers < 12) {
			ret.setHasBodyguard(false);
		} else {
			ret.setHasBodyguard(true);
		}
		
		ret.setHasIndemoniated(true);
		
		return ret;
	}
	
	public int getHowManyWolves() {
		return howManyWolves;
	}
	public void setHowManyWolves(int howManyWolves) {
		this.howManyWolves = howManyWolves;
	}

	public boolean hasSeer() {
		return hasSeer;
	}

	public void setHasSeer(boolean hasSeer) {
		this.hasSeer = hasSeer;
	}

	public boolean hasMedium() {
		return hasMedium;
	}

	public void setHasMedium(boolean hasMedium) {
		this.hasMedium = hasMedium;
	}

	public boolean hasBodyguard() {
		return hasBodyguard;
	}

	public void setHasBodyguard(boolean hasBodyguard) {
		this.hasBodyguard = hasBodyguard;
	}

	public boolean hasOwl() {
		return hasOwl;
	}

	public void setHasOwl(boolean hasOwl) {
		this.hasOwl = hasOwl;
	}

	public boolean hasIndemoniated() {
		return hasIndemoniated;
	}

	public void setHasIndemoniated(boolean hasIndemoniated) {
		this.hasIndemoniated = hasIndemoniated;
	}
}
