package client;

public class GameController {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private int game_id;

	private boolean started;
	private int max_players;

	private int timeout;

	Blind blind;

	private int hand_no;

	private boolean restart;

	private boolean ended;

	private String name;

	// ===========================================================
	// Constructors
	// ===========================================================

	public GameController(){



	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public int getGameId() {
		return game_id;
	}

	public void setGameId(int gameId) {
		game_id = gameId;
	}

	public int getPlayerTimeout() {
		return timeout;
	}

	public void setPlayerTimeout(int timeout) {
		this.timeout = timeout;
	}

	public void setBlindsStart(int blinds_start){
		blind.setBlindsStart(blinds_start);
	}

	public int getBlindsStart(){
		return blind.getBlindsStart();
	}

	public int getBlindsTime() {
		return blind.getBlindsTime();
	}

	public void setBlindsTime(int blindsTime) {
		blind.setBlindsTime(blindsTime);
	}

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================



	private class Blind{

		int start_chips;
		int amount_chips;
		int blinds_time;

		public void setBlindsStart(int startChips) {
			start_chips = startChips;
		}

		public int getBlindsStart() {
			return start_chips;
		}

		public int getBlindsTime() {
			return blinds_time;
		}

		public void setBlindsTime(int blindsTime) {
			blinds_time = blindsTime;
		}

	}

}
