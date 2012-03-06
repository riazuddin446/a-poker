package client;

import java.util.HashMap;
import java.util.Vector;

import logic.Player;
import logic.PokerHandStrength;
import logic.Player.Action;

public class GameController {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private HashMap<Integer, Player> players;

	private int game_id;
	private int max_players;
	private int timeout;
	private int hand_no;
	private int player_stakes;

	private boolean started;
	private boolean restart;
	private boolean ended;

	Blind blind;

	private String name;

	// ===========================================================
	// Constructors
	// ===========================================================

	public GameController(){

		game_id = -1;

		started = false;
		ended = false;
		max_players = 5;
		restart = false;

		player_stakes = 1500;

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

	public int getPlayerStakes() {
		return player_stakes;
	}

	public void setPlayerStakes(int playerStakes) {
		player_stakes = playerStakes;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getMaxPlayers() {
		return max_players;
	}

	public void setMaxPlayers(int maxPlayers) {
		max_players = maxPlayers;
	}

	public boolean isRestart() {
		return restart;
	}

	public void setRestart(boolean restart) {
		this.restart = restart;
	}

	public boolean isStarted() {
		return started;
	}

	public void setStarted(boolean started) {
		this.started = started;
	}

	public boolean isEnded() {
		return ended;
	}

	public void setEnded(boolean ended) {
		this.ended = ended;
	}

	public int getPlayerCount(){
		return players.size();
	}

	public void getPlayerList(HashMap<Integer, Player> list){
		list = players;
	}

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	public void addPlayer(Player p){

	}

	public void removePlayer(Player p){

	}

	public boolean isPlayer(Player p){
		return false;
	}

	public void setPlayerAction(Player p, Action action, int amount){


	}

	public void start(){

	}

	public int tick(){
		return 0;
	}

	protected void createWinList(Vector<Vector<PokerHandStrength>> winList){

		Vector<PokerHandStrength> wl;
		
		//int showdown_player = 
	}


	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	private class Blind{

		int start_chips;
		int amount_chips;
		int blinds_factor;

		public void setBlindsStart(int startChips) {
			start_chips = startChips;
		}

		public int getBlindsStart() {
			return start_chips;
		}

		public int getBlindsTime() {
			return blinds_factor;
		}

		public void setBlindsTime(int blindsTime) {
			blinds_factor = blindsTime;
		}

	}

}
