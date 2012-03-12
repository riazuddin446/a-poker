package client;

import java.util.HashMap;
import java.util.Vector;

import client.Table.BettingRound;
import client.Table.Pot;
import client.Table.State;

import logic.Card;
import logic.HoleCards;
import logic.Player;
import logic.PokerHandEvaluator;
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
		if(action == Action.ResetAction)
		{
			p.next_action.valid = false;
		}
		else if(action == Action.Sitout)
		{
			p.sitout = true;
		}
		else if(action == Action.Back)
		{
			p.sitout = true;
		}

		p.next_action.valid = true;
		p.next_action.action = action;
		p.next_action.amount = amount;

	}

	public void start(){

	}

	public int tick(){
		return 0;
	}

	protected void createWinList(Table t, Vector<Vector<PokerHandStrength>> winList){

		Vector<PokerHandStrength> wl;

		int showdown_player = t.lastBetPlayer;
		for(int i=0; i<t.countActivePlayers(); i++)
		{
			Player p = t.seats.get(showdown_player).player;

			PokerHandStrength strength;
			//PokerHandEvaluator evaluator = new PokerHandEvaluator();
			//TODO evaluator.getStrength();

		}
	}

	protected int determineMinimumBet(Table t)
	{
		if(t.bet_amount == 0)
			return blind.amount;
		else
			return t.bet_amount + (t.bet_amount - t.last_bet_amount);
	}

	protected void stateNewRound()
	{

	}

	protected void stateBlinds(Table t)
	{
		t.bet_amount = blind.amount;

		Player pSmall = t.seats.get(t.sb).player;
		Player pBig = t.seats.get(t.bb).player;

		//Set the player's SB
		int amount = blind.amount/2;

		if(amount > pSmall.stake)
			amount = pSmall.stake;

		t.seats.get(t.sb).bet = amount;
		pSmall.stake -= amount;

		//Set the player's BB
		amount = blind.amount;

		if(amount > pBig.stake)
			amount = pBig.stake;

		t.seats.get(t.bb).bet = amount;
		pBig.stake -= amount;

		//Initialize the player's timeout
		//TODO t.timeout_start = Time(null);

		//Give out hole-cards
		dealHole(t);

		//Check if there is any more action possible
		if(t.isAllin())
		{
			if((pBig.stake == 0 && pSmall.stake == 0) || //Bot players are allin 
					(pBig.stake==0 && t.seats.get(t.sb).bet >= t.seats.get(t.bb).bet) //BB is allin and SB has bet more or equal than BB
					|| (pSmall.stake == 0)) //SB is allin
			{
				t.nomoreaction = true;
			}
		}

		t.betround = BettingRound.Preflop;
		t.scheduleState(State.Betting, 3);
	}

	protected void stateBetting(Table t)
	{
		Player p = t.seats.get(t.currentPlayer).player;

		boolean allowed_action = false; //Is action allowed?
		boolean auto_action = false;

		Action action = null;
		int amount = 0;

		int minimun_bet = determineMinimumBet(t);

		if(t.nomoreaction || //Early showdonw, no more action at table possible
				p.stake == 0) //Or player is allin and has no more options
		{
			action = Action.None;
			allowed_action = true;
		}
		else if(p.next_action.valid) //Has player set an action?
		{
			action = p.next_action.action;

			if (action == Action.Fold)
				allowed_action = true;
			else if(action == Action.Check)
			{
				//Allowed check?
				if(t.seats.get(t.currentPlayer).bet < t.bet_amount)
				{
					//TODO Send message that cannot chet7
				}
				else
					allowed_action = true;
			}

			else if(action == Action.Call) //FIXME Menos ifs
			{
				if(t.bet_amount == 0 || t.bet_amount == t.seats.get(t.currentPlayer).bet)
				{
					//Cannot call, just check or fold

					//Retry with this action
					p.next_action.action = Action.Check;
					return;
				}
				else if(t.bet_amount > (t.seats.get(t.currentPlayer).bet + p.stake))
				{
					//Convert this action to allin
					p.next_action.action = Action.Allin;
					return;
				}
				else
				{
					allowed_action = true;
					amount = t.bet_amount - t.seats.get(t.currentPlayer).bet;
				}
			}

			else if(action == Action.Bet) //FIXME Un solo if
			{
				if(t.bet_amount > 0){
					//TODO there already was a bet
				}
				else if(p.next_action.amount < minimun_bet){
					//Cannont bet this amount because its less than the minimun one
				}
				else
				{
					allowed_action = true;
					amount = p.next_action.amount - t.seats.get(t.currentPlayer).bet;
				}
			}

			else if(action == Action.Raise)
			{
				if(t.bet_amount == 0)
				{
					//Cant raise, nothing was bet

					//Retry with this action
					p.next_action.action = Action.Bet;
					return;
				}
				else if(p.next_action.amount < minimun_bet)
				{
					//TODO Cant raise less than minimun_bet
				}
				else
				{
					allowed_action = true;
					amount = p.next_action.amount - t.seats.get(t.currentPlayer).bet;
				}
			}

			else if(action == Action.Allin)
			{
				allowed_action = true;
				amount = p.stake;
			}

			//Reset player action
			p.next_action.valid = false;
		}

		else //Handle player timeout
		{
			if(p.sitout) //TODO || (time => timeout)
			{
				//Let player sit out (if he is not already)
				p.sitout = true;

				//Auto-action: fold or check if possible
				if(t.seats.get(t.currentPlayer).bet < t.bet_amount)
					action = Action.Fold;
				else
					action = Action.Check;

				allowed_action = true;
				auto_action = true;
			}
		}

		//Return here if no or invalid action
		if(!allowed_action)
			return;

		//Remember action for snapshot TODO Snapshot?
		p.last_action = action;

		//Perform action
		if(action == Action.None)
		{
			//Do nothing
		}
		else if(action == Action.Fold)
		{
			t.seats.get(t.currentPlayer).in_round = false;
			//Send snapshot
		}
		else if(action == Action.Check)
		{
			//Send snapshot
		}
		else
		{
			//Player can't bet/raise more than his stake
			if(amount > p.stake)
				amount = p.stake;

			//Move chips from player's stake to seat-bet
			t.seats.get(t.currentPlayer).bet += amount;
			p.stake -= amount;

			if(action == Action.Bet || action == Action.Raise || action == Action.Allin)
			{
				//Only re-open betting round if amount greater than table-bet
				//FIXME check holdingnuts source

				if(t.seats.get(t.currentPlayer).bet > t.bet_amount)
				{
					t.lastBetPlayer = t.currentPlayer;
					t.last_bet_amount = t.bet_amount; //Needed for minimun-bet

					t.bet_amount = t.seats.get(t.currentPlayer).bet;
				}

				//TODO Send snapshots
			}
		}

		//All players except one folded -> End this hand
		if(t.countActivePlayers() == 1)
		{
			//Collect bets into pot
			t.collectBets();

			t.state = State.AskShow;

			//Set last remaining player as current player
			t.currentPlayer = t.getNextActivePlayer(t.currentPlayer);

			//Initialize the player's timeout
			//TODO t.timeout_start = time(null);

			t.resetLastPlayerActions();
			return;
		}

		//////////////
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

	protected void stateEndRound(Table t)
	{
		//Count up current hand number
		hand_no++;

		t.deck.fill();
		//t.deck.shuffle(); The fill method shuffles the card deck

		//Reset round-related
		t.communitycards.clear();

		t.bet_amount = 0;
		t.last_bet_amount = 0;
		t.nomoreaction = false;

		//Clear old pots and create initial main pot
		t.pots.clear();
		Table.Pot pot = t.new Pot(); //Inner class instantiation
		pot.amount = 0;
		pot.final1 = false;
		t.pots.add(pot);

		//Reset player-related
		for(int i=0; i<5; i++){
			if(t.seats.get(i).occupied)
			{
				t.seats.get(i).in_round = true;
				t.seats.get(i).showcards = false;
				t.seats.get(i).bet = 0;

				Player  p = t.seats.get(i).player;
				p.holecards.empty();
				p.resetLastAction();
				p.stake_before = p.stake; //Remember stake before this hand
			}
		}

		//Determine who is the next SB and next BB
		boolean headsup_rule = (t.countPlayers() == 2);

		if(headsup_rule)
		{
			t.bb = t.getNextPlayer(t.dealer);
			t.sb = t.getNextPlayer(t.bb);
		}
		else
		{
			t.sb = t.getNextPlayer(t.dealer);
			t.bb = t.getNextPlayer(t.sb);
		}

		//Player under the gun
		t.currentPlayer = t.getNextPlayer(t.bb);
		t.lastBetPlayer = t.currentPlayer;
	}

	protected void stateDelay() //Pseudo-state for delays
	{

	}

	protected void dealHole(Table t)
	{		
		//Player in SB gets first cards
		for(int i=t.sb, c=0; c<t.countPlayers(); i=t.getNextPlayer(i))
		{
			if(!t.seats.get(i).occupied)
			{
				Player p = t.seats.get(i).player;

				HoleCards h;
				Card c1, c2;

				c1 = t.deck.pop();
				c2 = t.deck.pop();

				p.holecards.setCards(c1, c2);

				c++;
			}
		}
	}

	protected void dealFlop(Table t)
	{
		Card f1, f2, f3;
		f1 = t.deck.pop();
		f2 = t.deck.pop();
		f3 = t.deck.pop();

		t.communitycards.setFlop(f1, f2, f3);
	}


	protected void dealTurn(Table t)
	{
		Card trn;
		trn = t.deck.pop();

		t.communitycards.setTurn(trn);
	}

	protected void dealRiver(Table t)
	{
		Card r;
		r = t.deck.pop();

		t.communitycards.setRiver(r);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	private class Blind{

		int start;
		int amount;
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
