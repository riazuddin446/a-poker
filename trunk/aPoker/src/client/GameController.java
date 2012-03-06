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

	private Table table;
	private HashMap<Integer, Player> players;

	private int game_id;

	private boolean started;
	private int max_players;

	private int player_stakes;	
	private int timeout;

	Blind blind;

	private int hand_no;

	private boolean ended;
	private boolean restart;

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

		blind.blinds_factor = 2;
		blind.start = 10;

		hand_no = 0;

		name = "Game";

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

	boolean addPlayer(Player p) //FIXME Quitar las dos if y la forma de insertar el nuevo jugador
	{
		//Is the game already started or full?
		if(started || players.size() == max_players)
			return false;

		if(isPlayer(p))
			return false;

		p.setStake(player_stakes);

		players.put(players.size(), p);

		return true;
	}

	public void removePlayer(Player p)
	{
		//Don't allow removing if the game has already been started
		if(!started)
		{
			//TODO Comprobar que el jugador no sea el owner de la partida
			players.remove(p);
		}
	}

	public boolean isPlayer(Player p){

		if(players.containsKey(p))
			return true;
		else 
			return false;
	}

	public void setPlayerAction(Player p, Action action, int amount)
	{
		if(true)
		{
			p.getNexAction();
		}
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

	protected int determineMinimumBet()
	{
		return 0;
	}

	protected void stateNewRound()
	{

	}

	protected void stateBlinds()
	{

	}

	protected void stateBetting()
	{

	}

	protected void stateBettingEnd() //Pseudo-state
	{

	}

	protected void stateAskShow()
	{

	}

	protected void stateAllFolded()
	{

	}

	protected void stateShowdown()
	{

	}

	protected void stateEndRound()
	{

	}

	protected void stateDelay() //Pseudo-state for delays
	{

	}

	protected void dealHole()
	{

	}

	protected void dealFlop()
	{

	}

	protected void dealTurn()
	{

	}

	protected void dealRiver()
	{

	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	private class Blind{

		int start;
		int amount_chips;
		int blinds_factor;

		public void setBlindsStart(int startChips) {
			start = startChips;
		}

		public int getBlindsStart() {
			return start;
		}

		public int getBlindsTime() {
			return blinds_factor;
		}

		public void setBlindsTime(int blindsTime) {
			blinds_factor = blindsTime;
		}

	}

}
