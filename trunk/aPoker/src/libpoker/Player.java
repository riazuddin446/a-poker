package libpoker;

import com.zorrozua.asier.Card;

public class Player {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	String name;
	int id, stake, stake_before;
	private Card card1, card2;
	private Action last_action;
	private SchedAction next_action;

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
		this.stake = chips;
	}

	public Player (String name, int chips, int id){
		this.name = name;
		this.stake = chips;
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

	public int getStake() {
		return stake;
	}

	public void setStake(int stake) {
		this.stake = stake;
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

	public SchedAction getNext_action() {
		return next_action;
	}

	public void setNext_action(SchedAction nextAction) {
		next_action = nextAction;
	}

	public Action getLast_action() {
		return last_action;
	}

	public void setLast_action(Action lastAction) {
		last_action = lastAction;
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

	enum Action{

		None,
		ResetAction,

		Fold,
		Check,
		Call,
		Bet,
		Raise,
		Allin,

		Show,
		Muck,

		Sitout,
		Back;

	}

	class SchedAction{
		boolean valid;
		Action action;
		int amount;
	}
}
