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
	private Card card1, card2;

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

	public Card getCard1() {
		return card1;
	}

	public void setCard1(Card card1) {
		this.card1 = card1;
	}

	public Card getCard2() {
		return card2;
	}

	public void setCard2(Card card2) {
		this.card2 = card2;
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	public void setCards(final Card pCard1, final Card pCard2){

		this.setCard1(pCard1);
		this.setCard2(pCard2);

	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
