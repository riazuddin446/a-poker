package com.zorrozua.asier;

public class Player {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	String name;
	int id, chipCounter;

	// ===========================================================
	// Constructors
	// ===========================================================

	public Player (){

	}

	public Player (String name){
		this.name = name;
	}

	public Player (String name, int chips){
		this.name = name;
		this.chipCounter = chips;
	}

	public Player (String name, int chips, int id){
		this.name = name;
		this.chipCounter = chips;
		this.id = id;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public String getPlayerName() {
		return name;
	}

	public void setPlayerName(String playerName) {
		this.name = playerName;
	}

	public int getPlayerID() {
		return id;
	}

	public void setPlayerID(int playerID) {
		this.id = playerID;
	}

	public int getChipCounter() {
		return chipCounter;
	}

	public void setChipCounter(int chipCounter) {
		this.chipCounter = chipCounter;
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
