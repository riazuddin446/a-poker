package server;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import logic.Card;
import logic.HoldemHandEvaluator;
import logic.Player;
import logic.PokerHandStrength;
import logic.Player.Action;
import server.Table.BettingRound;
import server.Table.Pot;
import server.Table.Seat;
import server.Table.State;
import android.widget.Toast;

public class GameController {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private String game_name; //Nombre de la partida

	public Table table; //Mesa donde se jugara la partida
	public HashMap<Integer, Player> players; //Lista de jugadores en la partida

	private int max_players; //Maximo de jugadores
	private int player_stakes; //Fichas iniciales de cada jugador

	private int timeout; //Tiempo disponible por los jugadores para cada turno

	public Blind blind; //Ciegas

	private int hand_no; //Numero de mano

	//Booleanos que indican el estado de la partida
	private boolean started;
	private boolean ended;
	private boolean restart;

	//Apuntador hacia el dueño de la partida
	private int owner;

	// ===========================================================
	// Constructors
	// ===========================================================

	public GameController()
	{		
		//Inicializar las variables
		table = new Table();
		players = new HashMap<Integer, Player>();
		blind = new Blind();

		//Asignar valores
		game_name = "Game";

		max_players = 5;
		player_stakes = 1500;

		started = false;
		ended = false;
		restart = false;

		blind.start = 50;
		blind.blinds_factor = 2;

		hand_no = 0;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public String getName() {
		return game_name;
	}

	public void setName(String name) {
		this.game_name = name;
	}

	public int getMaxPlayers() {
		return max_players;
	}

	public void setMaxPlayers(int maxPlayers) {
		if(maxPlayers >=2)
			max_players = maxPlayers;
	}

	public int getPlayerStakes() {
		return player_stakes;
	}

	public void setPlayerStakes(int playerStakes) {
		player_stakes = playerStakes;
	}

	public int getPlayerTimeout() {
		return timeout;
	}

	public void setPlayerTimeout(int timeout) {
		this.timeout = timeout;
	}

	public void setBlindsStart(int blinds_start){
		blind.setBlindStart(blinds_start);
	}

	public int getBlindsStart(){
		return blind.getBlindsStart();
	}

	public int getBlindsFactor() {
		return blind.getBlindFactor();
	}

	public void setBlindsFactor(int blindsTime) {
		blind.setBlindFactor(blindsTime);
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

	public boolean getRestart() {
		return restart;
	}

	public void setRestart(boolean restart) {
		this.restart = restart;
	}

	public int getOwner() {
		return owner;
	}

	public void setOwner(int owner) {
		this.owner = owner;
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

	/**
	 * Returns true if the player is already ingame
	 * 
	 * @param p Player to be checked
	 */
	public boolean isPlayer(Player p){

		if(players.containsKey(p))
			return true;
		else 
			return false;
	}

	/**
	 * Adds a player to the game
	 * 
	 * @param p Player to be added
	 */
	public void addPlayer(int i, Player p)
	{
		//If the game is not started of full and if the player is not already ingame
		if(!started && players.size() != max_players && !isPlayer(p))
		{
			p.setStake(player_stakes);
			players.put(i, p);

			table.addPlayerToSeat(p);
		}
		else
			System.out.println("Can't add the player: " + p.getPlayerName());
	}

	/**
	 * Removes a player from the game
	 * 
	 * @param p Player to be removed
	 */
	public void removePlayer(Player p)
	{
		//Don't allow removing if the game has already been started
		if(!started)
		{
			boolean needNewOwner = false;
			if(p == players.get(owner))
				needNewOwner = true;

			players.remove(p);

			if(needNewOwner)
				selectNewOwner();
		}
	}

	/**
	 * Finds a new owner of the game
	 */
	private void selectNewOwner()
	{
		Iterator it = players.entrySet().iterator();
		if(it.hasNext())
		{
			//FIXME Buscar el int que apunte al jugador correctamente en la lista de players
			Player tmp = (Player)it.next();
			owner = tmp.id;
		}
	}

	/**
	 * Sets the player action
	 * 
	 * @param p Player involved
	 * @param action Action to be set
	 * @param amount The amount of the bet involved in the action, if needed
	 */
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

	/**
	 * Create the list of the winner's hand strengths and their id
	 * 
	 * @param t Table involved
	 * @param winList
	 */
	protected void createWinList(Table t, Vector< Vector<PokerHandStrength> > winList)
	{
		Vector<PokerHandStrength> wl = new Vector<PokerHandStrength>(); //Vector where we are going to add

		int showdown_player = t.lastBetPlayer;

		for(int i=0; i<t.countActivePlayers(); i++)
		{
			Player p = t.seats.get(showdown_player).player;

			PokerHandStrength strength; // = new PokerHandStrength(); FIXME
			HoldemHandEvaluator evaluator = new HoldemHandEvaluator(p.holecards.cards, table.communitycards.cards);
			strength = evaluator.getStrength();
			strength.setId(showdown_player); //Save the id of the player to link with his hand strength

			wl.add(strength); //Add to the list

			showdown_player = t.getNextActivePlayer(showdown_player); //Find next showdown player
		}

		HoldemHandEvaluator tmp = new HoldemHandEvaluator(null, null);
		tmp.getWinList(wl, winList);
	}

	/**
	 * Determines the minimum bet that a player can do
	 * 
	 * @param t Table involved
	 * @return The minimum bet
	 */
	protected int determineMinimumBet(Table t)
	{
		if(t.bet_amount == 0)
			return blind.amount;
		else
			return t.bet_amount + (t.bet_amount - t.last_bet_amount);
	}

	/**
	 * Deals the hole cards to all the players
	 * 
	 * @param t Table involved
	 */
	protected void dealHole(Table t)
	{		
		//Player in SB gets first cards
		for(int i=t.sb, c=0; c<t.countPlayers(); i=t.getNextPlayer(i))
		{
			if(!t.seats.get(i).occupied)
			{
				Player p = t.seats.get(i).player;

				Card c1, c2;

				c1 = t.deck.pop();
				c2 = t.deck.pop();

				p.holecards.setCards(c1, c2);

				c++;
			}
		}
	}

	/**
	 * Deals the flop cards
	 * 
	 * @param t Table involved
	 */
	protected void dealFlop(Table t)
	{
		Card f1, f2, f3;
		f1 = t.deck.pop();
		f2 = t.deck.pop();
		f3 = t.deck.pop();

		t.communitycards.setFlop(f1, f2, f3);
	}

	/**
	 * Deals the turn card
	 * 
	 * @param t Table involved
	 */
	protected void dealTurn(Table t)
	{
		Card trn;
		trn = t.deck.pop();

		t.communitycards.setTurn(trn);
	}

	/**
	 * Deals the river card
	 * 
	 * @param t Table involved
	 */
	protected void dealRiver(Table t)
	{
		Card r;
		r = t.deck.pop();

		t.communitycards.setRiver(r);
	}

	/**
	 * Handle the 'NewRound' state
	 * 
	 * @param t Table involved
	 */
	protected void stateNewRound(Table t)
	{
		//Count up current hand number
		hand_no++;

		//Fill and shuffle the card deck
		t.deck.clearFillAndShuffle();

		//Reset round related
		t.communitycards.clear();
		t.bet_amount = 0;
		t.last_bet_amount = 0;
		t.nomoreaction = false;

		//Clear old pots and create initial main pot
		t.pots.clear();
		Pot pot = t.new Pot();
		pot.amount = 0;
		pot.isFinal = false;
		t.pots.add(pot);

		//Reset player related
		for(int i=0; i<5; i++)
		{
			Seat aux = t.seats.get(i);
			if(aux.occupied)
			{
				aux.in_round = true;
				aux.showcards = false;
				aux.bet = 0;

				aux.player.holecards.clear();
				aux.player.resetLastAction();
				aux.player.stake_before = aux.player.stake; //Remeber stake before this hand
			}
		}

		//Determine who is SB and BB

		boolean headsup_rule = (t.countPlayers()==2);

		if(headsup_rule) //Head-up rule: only 2 players remain so swap blinds
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

		t.state = State.Blinds;
	}

	/**
	 * Handle the 'Blinds' state
	 * 
	 * @param t Table involved
	 */
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

		//TODO Initialize the player's timeout

		//Give out hole-cards
		dealHole(t);

		//TODO Tell the player under the gun that it's his turn

		//Check if there is any more possible action
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

		t.scheduleState(State.Betting, 3); //FIXME Delay
	}

	/**
	 * Handle the betting round
	 * 
	 * @param t Table involved
	 */
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

			if (action == Action.Fold) //Retirarse
				allowed_action = true;
			else if(action == Action.Check) //Pasar
			{
				//Allowed check?
				if(t.seats.get(t.currentPlayer).bet < t.bet_amount)
				{
					//FIXME Send message that cannot check
					Toast toast = Toast.makeText(null, "You can't check dude! Try call ;D", 3);
					toast.show();
				}
				else
					allowed_action = true;
			}
			else if(action == Action.Call) //Igualar apuesta
			{
				if(t.bet_amount == 0 || t.bet_amount == t.seats.get(t.currentPlayer).bet) //Cannot call, just check or fold
				{
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

			else if(action == Action.Bet) //Iniciar apuesta
			{
				if(t.bet_amount > 0){
					//FIXME there already was a bet
					Toast toast = Toast.makeText(null, "You can't bet dude! There already was a bet, try raise.", 3);
					toast.show();
				}
				else if(p.next_action.amount < minimun_bet)
				{
					//FIXME
					Toast toast = Toast.makeText(null, "You can't bet this amount, the minimun bet is: " + minimun_bet, 3);
					toast.show();			
				}
				else
				{
					allowed_action = true;
					amount = p.next_action.amount - t.seats.get(t.currentPlayer).bet;
				}
			}

			else if(action == Action.Raise) //Subir apuesta
			{
				if(t.bet_amount == 0)
				{
					//FIXME Cant raise, nothing was bet
					Toast toast = Toast.makeText(null, "You cannot raise, nothing was bet! Try bet.", 3);
					toast.show();

					//Retry with this action
					p.next_action.action = Action.Bet;
					return;
				}
				else if(p.next_action.amount < minimun_bet)
				{
					//FIXME Cant raise less than minimun_bet
					Toast toast = Toast.makeText(null, "You cannot raise this amount. Minimum bet is: " + minimun_bet, 3);
					toast.show();				
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
		else //Player didn't set next action, handle his timeout
		{
			if(p.sitout) //TODO Timeout
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

		//Remember action for snapshot
		p.last_action = action;

		//PERFORM ACTION
		if(action == Action.None)
		{
			//Do nothing
		}
		else if(action == Action.Fold)
		{
			t.seats.get(t.currentPlayer).in_round = false;
		}
		else if(action == Action.Check)
		{
			//Just continue
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
				//FIXME  bug: other players need to do an action even on none-minimum-bet
				if(t.seats.get(t.currentPlayer).bet > t.bet_amount)
				{
					t.lastBetPlayer = t.currentPlayer;
					t.last_bet_amount = t.bet_amount; //Needed for minimun-bet

					t.bet_amount = t.seats.get(t.currentPlayer).bet;
				}
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

			//TODO Initialize the player's timeout

			t.resetLastPlayerActions();
			return;
		}

		//Is the last player who did the last bet/action? If yes end this betting round
		if(t.getNextActivePlayer(t.currentPlayer) == t.lastBetPlayer)
		{
			//Collect bets into pot
			t.collectBets();

			//All (or all except one) players are allin
			if(t.isAllin())
			{
				//No further action at table possible
				t.nomoreaction = true;
			}

			//Which betting round is next?
			switch (t.betround)
			{
			case Preflop:
				t.betround = BettingRound.Flop;
				dealFlop(t);
				break;
			case Flop:
				t.betround = BettingRound.Turn;
				dealTurn(t);
				break;
			case Turn:
				t.betround = BettingRound.River;
				dealRiver(t);
				break;
			case River:
				//last_bet_player must show his hand
				t.seats.get(t.lastBetPlayer).showcards = true;

				//Set the player behind last action as current player
				t.currentPlayer = t.getNextActivePlayer(t.lastBetPlayer);

				//TODO Initialize the player's timeout

				//End of hand, do showdown/ask for show
				if(t.nomoreaction)
					t.state = State.Showdown;
				else
					t.state = State.AskShow;

				t.resetLastPlayerActions();
				return;
			}

			//Reset the highest bet amount
			t.bet_amount = 0;
			t.last_bet_amount = 0;

			//Set the current player as SB (or next active behing SB)
			t.currentPlayer = t.getNextActivePlayer(t.dealer);

			//TODO Reinitialize the player's timeout

			//First action for next betting round is at this payer
			t.lastBetPlayer = t.currentPlayer;
			t.resetLastPlayerActions();
			t.scheduleState(State.BettingEnd, 2);			
		}
		else
		{
			//Preflop: if player on whom the last action was folds,
			//assign 'last action' to next active player
			if(action == Action.Fold && t.currentPlayer == t.lastBetPlayer)
				t.lastBetPlayer = t.getNextActivePlayer(t.lastBetPlayer);

			//Find next player
			t.currentPlayer = t.getNextActivePlayer(t.currentPlayer);
			//t.timeout_start = time(NULL);

			//Reset current player's last action
			p = t.seats.get(t.currentPlayer).player;
			p.resetLastAction();

			t.scheduleState(State.Betting, 1);
		}
	}

	/**
	 * Pseudo state to make a wait break
	 * 
	 * @param t Table involved
	 */
	protected void stateBettingEnd(Table t) //Pseudo-state
	{	
		t.state = State.Betting;
	}

	/**
	 * Ask to every player involved in the ended bet to show his cards
	 * 
	 * @param t Table involved
	 */
	protected void stateAskShow(Table t)
	{
		boolean chose_action = false;

		Player p = t.seats.get(t.currentPlayer).player;

		if(p.stake == 0 && t.countActivePlayers() > 1) //Player went allin and has no option to show/muck
		{
			t.seats.get(t.currentPlayer).showcards = true;
			chose_action = true;
			p.next_action.valid = false;
		}
		else if(p.next_action.valid) //Has player set an action?
		{
			if(p.next_action.action == Action.Muck)
			{
				//Muck cards
				chose_action = true;
			}
			else if (p.next_action.action == Action.Show)
			{
				//Show cards
				t.seats.get(t.currentPlayer).showcards = true;

				chose_action = true;
			}

			//Reset scheduled action
			p.next_action.valid = false;
		}
		else //Wait till player set an action
		{
			//Handle player timeout
			if(p.sitout) //TODO
			{
				// default on showdown is "to show"
				// Note: client needs to determine if it's hand is
				//       already lost and needs to fold if wanted
				if(t.countActivePlayers() > 1)
					t.seats.get(t.currentPlayer).showcards = true;

				chose_action = true;
			}
		}

		//Return here if no action chosen till now
		if(!chose_action)
			return;

		//Remember action
		if(t.seats.get(t.currentPlayer).showcards)
			p.last_action = Action.Show;
		else
			p.last_action = Action.Muck;

		//All players, except one, folded or showdown
		if(t.countActivePlayers() == 1)
		{
			t.state = State.AllFolded;
		}
		else
		{
			//Player is out if he don't want to show his cards
			if(t.seats.get(t.currentPlayer).showcards == false)
				t.seats.get(t.currentPlayer).in_round = false;

			if(t.getNextActivePlayer(t.currentPlayer) == t.lastBetPlayer)
			{
				t.state = State.Showdown;
				return;
			}
			else
			{
				//Find next player
				t.currentPlayer = t.getNextActivePlayer(t.currentPlayer);

				//TODO Start timeout?
			}
		}
	}

	/**
	 * Handles the case that all player have folded
	 * 
	 * @param t Table involved
	 */
	protected void stateAllFolded(Table t)
	{
		//Get last remaining player
		Player p = t.seats.get(t.currentPlayer).player;

		//Give the pot to the winner
		p.stake += t.pots.get(0).amount;
		t.seats.get(t.currentPlayer).bet = t.pots.get(0).amount;

		t.scheduleState(State.EndRound, 2);
	}

	/**
	 * Handles the showdown
	 * 
	 * @param t Table involved
	 */
	protected void stateShowdown(Table t)
	{
		//TODO Start showing the cards on the screen. The player who did the last action is first showing

		//Determine winners
		Vector< Vector<PokerHandStrength> > winList = new Vector< Vector<PokerHandStrength> >();
		createWinList(t, winList);

		//For each winner list:
		for (int i=0; i < winList.size(); i++)
		{
			Vector<PokerHandStrength> tw = winList.get(i); //Take one winner list
			int winner_count = tw.size(); //Count of players of the winner list

			//For each pot:
			for(int poti=0; poti < t.pots.size(); poti++)
			{
				Pot pot = t.pots.get(poti); //Get the pot
				int involved_count = t.getInvolvedInPotCount(pot, tw); //Get the count of players of the win list involved in this pot

				int win_amount = 0; 
				int odd_chips = 0;

				if(involved_count > 0)
				{
					//Pot is divided by the number of players involved in
					win_amount = pot.amount / involved_count;

					//Odd chips
					odd_chips = pot.amount - (win_amount * involved_count);
				}

				int cashout_amount = 0;

				//For each winning player
				for(int pi=0; pi < winner_count; pi++)
				{
					int seat_num = tw.get(pi).getId();
					Seat seat = t.seats.get(seat_num);
					Player p = seat.player;

					//Skip pot if player is not involved in it
					if(!t.isSeatInvolvedInPot(pot, seat_num))
						continue;

					if(win_amount > 0)
					{
						//Transfer winning amount to player
						p.stake += win_amount;

						//FIXME Put winnings to seat (needed for snapshot)
						//seat.bet += win_amount;

						//Count up overall cashed-out
						cashout_amount += win_amount;

					}
				}

				//Distribute odd chips
				if(odd_chips == 1)
				{
					//Find the next player behind button which is involved in pot
					int oddchips_player = t.getNextActivePlayer(t.dealer);

					while(!t.isSeatInvolvedInPot(pot, oddchips_player))
						oddchips_player = t.getNextActivePlayer(oddchips_player);

					Seat seat = t.seats.get(oddchips_player);
					Player p = seat.player;

					p.stake += odd_chips;
					//FIXME seat.bet += odd_chips;

					cashout_amount += odd_chips;
				}

				//Reduce pot about the overall cashed-out
				pot.amount -= cashout_amount;
			}
		}

		//Check for fatal error: not all pots were distributed
		for(int j=0; j < t.pots.size(); j++)
		{
			Pot pot = t.pots.get(j);

			if(pot.amount > 0)
			{
				System.out.println("WinList -> Error remaining chips in pot: " + pot.amount);
			}
		}

		//Reset all pots
		t.pots.clear();

		t.scheduleState(State.EndRound, 2);
	}

	protected void stateEndRound(Table t)
	{
		HashMap<Integer, Integer> broken_players = new HashMap<Integer, Integer>();

		//Lets look for broken players
		for(int i=0; i<5; i++)
		{
			if(!t.seats.get(i).occupied) //Skip not occupied seats
				continue;

			Player p = t.seats.get(i).player;

			//Check if player has no stake left
			if(p.stake == 0)
				broken_players.put(p.stake_before, i);
		}

		//Remove player in rigth order (sorted by stake_before)
		for(int i=0; i<broken_players.size(); i++)
		{
			int seat_num = broken_players.get(i);

			Player p = t.seats.get(seat_num).player;

			//TODO Tell player that he is out

			t.seats.get(seat_num).occupied = false;
		}

		//Determine next dealer
		t.dealer = t.getNextPlayer(t.dealer);

		t.scheduleState(State.NewRound, 2);
	}

	protected int handleTable(Table t)
	{
		if(t.state == State.NewRound)
			stateNewRound(t);
		else if(t.state == State.Blinds)
			stateBlinds(t);
		else if(t.state == State.Betting)
			stateBetting(t);
		else if(t.state == State.BettingEnd)
			stateBettingEnd(t);
		else if(t.state == State.AskShow)
			stateAskShow(t);
		else if(t.state == State.AllFolded)
			stateAllFolded(t);
		else if(t.state == State.Showdown)
			stateShowdown(t);
		else if(t.state == State.EndRound)
			stateEndRound(t);

		//If there is one player left, close the table
		if(t.countPlayers()==1)
			return -1;

		return 0;
	}

	public void start()
	{
		//Game not started or at least 2 players needed
		if(started || players.size() < 2)
			return;

		started = true;

		table.seats.clear();

		//Place players on seats
		boolean chose_dealer = false;
		for(int i=0; i < players.size(); i++)
		{
			Seat seat = table.new Seat();

			seat.seat_no = i;

			if(players.get(i) != null) //TODO comprobar
			{
				seat.occupied = true;
				seat.player = players.get(i);

				if(!chose_dealer)
				{
					table.dealer = i;
					chose_dealer = true;
				}
			}
			else
				seat.occupied = false;

			table.seats.add(i, seat);
		}

		table.state = State.GameStart;

		blind.amount = blind.start;

		table.scheduleState(State.NewRound, 5);
	}

	public int tick()
	{
		if(!started) //If the game is not set as started
		{
			if(getPlayerCount() == max_players) //Start the game if player count reached
				start();
			else if(getPlayerCount() == 0 && !getRestart()) //Delete the game if no player registered
				return -1;
			else //Nothing to do, early exit
				return 0;
		}
		else if(ended) //If the game is set as ended
		{
			//Remove all players
			players.clear();
			return -1;
		}

		//Handle table
		if(handleTable(table) < 0) //Is the table closed?
		{
			ended = true;
			//TODO ended_time + snap
		}
		return 0;
	}

	private class Blind{

		int start; //Ciega inicial
		int amount; //Ciega actual
		int blinds_factor; //Factor de aumento para la ciega

		public void setBlindStart(int startChips) {
			start = startChips;
		}

		public int getBlindsStart() {
			return start;
		}

		public int getBlindFactor() {
			return blinds_factor;
		}

		public void setBlindFactor(int bFactor) {
			blinds_factor = bFactor;
		}

	}

}
