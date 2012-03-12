package logic;


public class Player {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	public String name;
	
	public int id, stake, stake_before;
	
	public HoleCards holecards;
	
	public Action last_action;
	public SchedAction next_action;
	
	public boolean sitout;

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


	public SchedAction getNexAction() {
		return next_action;
	}

	public void setNextAction(SchedAction nextAction) {
		next_action = nextAction;
	}

	public Action getLastAction() {
		return last_action;
	}

	public void setLastAction(Action lastAction) {
		last_action = lastAction;
	}

	public void resetLastAction(){
		last_action = Action.None;
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

	public enum Action{

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

	public class SchedAction{
		public boolean valid;
		public Action action;
		public int amount;
	}
}
