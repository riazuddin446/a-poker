package server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import logic.Card;
import logic.HoldemHandEvaluator;
import logic.Player;
import logic.Player.Action;
import logic.PokerHandStrength;
import server.Table.BettingRound;
import server.Table.Pot;
import server.Table.Seat;
import server.Table.State;

public class GameController {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private String game_name; //Nombre de la partida

	public Table table; //Mesa donde se jugara la partida
	public ArrayList<Player> players; //Lista de jugadores en la partida

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

	public int minimun_bet;

	// ===========================================================
	// Constructors
	// ===========================================================

	public GameController()
	{		

		//Inicializar las variables
		table = new Table();
		players = new ArrayList<Player>();
		blind = new Blind();

		//Asignar valores por defecto
		game_name = "Game";

		max_players = 5;
		player_stakes = 1500;

		started = false;
		ended = false;
		restart = false;

		blind.start = 50;
		blind.blinds_factor = 2;

		hand_no = 0;

		minimun_bet = determineMinimumBet(table);
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

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	/**
	 * Añade un jugador a la partida
	 * 
	 * @param i Posicion en la que añadir
	 * @param p Jugador a añadir
	 */
	public void addPlayer(int i, Player p)
	{
		//If the game is not started of full and if the player is not already ingame
		if(!started && players.size() != max_players && !isPlayer(p))
		{
			p.setStake(player_stakes);
			players.add(i, p);

			table.addPlayerToSeat(p); //TODO Añadir al jugador en el seat que quiera
		}
		else
			System.out.println("Can't add the player: " + p.getName());
	}

	/**
	 * Elimina un jugador de la partida
	 * 
	 * @param p Jugador a ser eliminado
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

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	/**
	 * Comprueba si el jugador 'p' ya esta añadido
	 * 
	 * @param p Jugador que ha de ser comprobado
	 */
	public boolean isPlayer(Player p)
	{
		if(players.contains(p))
			return true;
		else 
			return false;
	}

	/**
	 * Busca un nuevo owner de la partida
	 */
	public void selectNewOwner()
	{
		Iterator it = players.iterator();
		if(it.hasNext())
		{
			Player tmp = (Player)it.next();
			owner = players.indexOf(tmp);
		}
	}

	/**
	 * Fija la accion de un jugador
	 * 
	 * @param p Jugador involucrado
	 * @param action Accion que se va a fijar
	 * @param amount Fichas involucradas en la accion (si es necesario)
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
			p.sitout = false;
		}
		else
		{
			p.next_action.valid = true;
			p.next_action.action = action;
			p.next_action.amount = amount;
		}

	}

	/**
	 * Crea una lista de las PokerHandStrength's ganadoras
	 * 
	 * @param t Mesa involucrada
	 * @return Vector con las PokerHandStrength's de los ganadores en orden
	 */
	protected Vector< Vector<PokerHandStrength> > createWinList(Table t)
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

		HoldemHandEvaluator tmp = new HoldemHandEvaluator();
		return tmp.getWinList(wl);
	}

	/**
	 * Determina la apuesta minima que puede realizar un jugador
	 * 
	 * @param t Mesa involucrada
	 * @return Apuesta minima
	 */
	protected int determineMinimumBet(Table t)
	{
		if(t.bet_amount == 0)
			return blind.amount;
		else
			return t.bet_amount + (t.bet_amount - t.last_bet_amount);
	}

	/**
	 * Reparte las cartas a los jugadores
	 * 
	 * @param t Mesa involucrada
	 */
	protected void dealHole(Table t)
	{		
		//Player in SB gets first cards
		for(int i=t.sb, c=0; c<t.countPlayers(); i=t.getNextPlayer(i))
		{
			if(t.seats.get(i).occupied)
			{
				Player p = t.seats.get(i).player;

				Card c1, c2;

				c1 = t.deck.pop();
				c2 = t.deck.pop();

				p.holecards.setCards(c1, c2);
			}
			c++;
		}
	}

	/**
	 * Reparte el flop
	 * 
	 * @param t Mesa involucrada
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
	 * Reparte el turn
	 * 
	 * @param t Mesa involucrada
	 */
	protected void dealTurn(Table t)
	{
		Card trn;
		trn = t.deck.pop();

		t.communitycards.setTurn(trn);
	}

	/**
	 * Reparte el river
	 * 
	 * @param t Mesa involucrada
	 */
	protected void dealRiver(Table t)
	{
		Card r;
		r = t.deck.pop();

		t.communitycards.setRiver(r);
	}

	/**
	 * Maneja la mesa en el estado NewRound
	 * Limpia y resetea todas las variables que necesitan estarlo para el inicio de una ronda nueva
	 * 
	 * @param t Mesa involucrada
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
	 * Maneja la mesa en el estado Blind
	 * Auto-apuesta las ciegas, reparte las holecards y comprueba si hay alguna accion mas posible
	 * 
	 * @param t Mesa involucrada
	 */
	protected void stateBlinds(Table t)
	{
		//TODO Comprobar si hace falta aumentar la ciega

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

		try{
			Thread.currentThread().sleep(1000);//sleep for 1000 ms
		}
		catch(InterruptedException ie){

		}

		//Give out hole-cards
		dealHole(t);

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

		t.scheduleState(State.Betting);
	}

	/**
	 * Maneja el estado Betting de la mesa
	 * 
	 * @param t Mesa involucrada
	 */
	protected void stateBetting(Table t)
	{
		Player p = t.seats.get(t.currentPlayer).player;

		boolean allowed_action = false; //Is action allowed?
		boolean auto_action = false;

		Action action = null;
		int amount = 0;

		minimun_bet = determineMinimumBet(t);

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
					//Toast.makeText(activity, "You can't check dude! Try call ;D", 2).show();
				}
				else
					allowed_action = true;
			}
			else if(action == Action.Call) //Igualar apuesta
			{
				if(t.bet_amount == 0 || t.bet_amount == t.seats.get(t.currentPlayer).bet) //Cannot call, try check
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
					//Toast.makeText(activity, "You can't bet dude! There already was a bet, try call or raise.", 2).show();
				}
				else if(p.next_action.amount < minimun_bet)
				{	
					//Toast.makeText(activity, "You can't bet this amount, the minimun bet is: " + minimun_bet, 2).show();
				}
				else
				{
					allowed_action = true;
					amount = p.next_action.amount - t.seats.get(t.currentPlayer).bet;
				}
			}

			else if(action == Action.Raise) //Subir apuesta
			{
				if(t.bet_amount == 0) //Can't raise, nothing was bet
				{
					//Retry with this action
					p.next_action.action = Action.Bet;
					return;
				}
				else if(p.next_action.amount < minimun_bet)
				{
					//Toast.makeText(activity, "You cannot raise this amount. Minimum bet is: " + minimun_bet, 2).show();			
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
			//TODO Manejar el timeout
		}

		//Return here if no or invalid action
		if(!allowed_action)
			return;

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

			t.state = State.Showdown; //State.AskShow;

			//Set last remaining player as current player
			t.currentPlayer = t.getNextActivePlayer(t.currentPlayer);

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

				try{
					Thread.currentThread().sleep(1000);//sleep for 1000 ms
				}
				catch(InterruptedException ie){

				}

				dealFlop(t);
				break;
			case Flop:
				t.betround = BettingRound.Turn;

				try{
					Thread.currentThread().sleep(1000);//sleep for 1000 ms
				}
				catch(InterruptedException ie){

				}

				dealTurn(t);
				break;
			case Turn:
				t.betround = BettingRound.River;

				try{
					Thread.currentThread().sleep(1000);//sleep for 1000 ms
				}
				catch(InterruptedException ie){

				}

				dealRiver(t);
				break;
			case River:
				//last_bet_player must show his hand
				t.seats.get(t.lastBetPlayer).showcards = true;

				//Set the player behind last action as current player
				t.currentPlayer = t.getNextActivePlayer(t.lastBetPlayer);

				//End of hand, do showdown/ask for show
				if(t.nomoreaction)
					t.state = State.Showdown;
				else
					t.state = State.Showdown; //State.AskShow;

				t.resetLastPlayerActions();
				return;
			}

			//Reset the highest bet amount
			t.bet_amount = 0;
			t.last_bet_amount = 0;

			//Set the current player as SB (or next active behing SB)
			t.currentPlayer = t.getNextActivePlayer(t.dealer); //FIXME No debería de ser el dealer el current player?!

			//First action for next betting round is at this player
			t.lastBetPlayer = t.currentPlayer;
			t.resetLastPlayerActions();
			t.scheduleState(State.BettingEnd);			
		}
		else
		{
			//Preflop: if player on whom the last action was folds,
			//assign 'last action' to next active player
			if(action == Action.Fold && t.currentPlayer == t.lastBetPlayer)
				t.lastBetPlayer = t.getNextActivePlayer(t.lastBetPlayer);

			//Find next player
			t.currentPlayer = t.getNextActivePlayer(t.currentPlayer);
			System.out.println("New current player: "+t.currentPlayer);

			//t.timeout_start = time(NULL);

			//Reset current player's last action
			t.seats.get(t.currentPlayer).player.resetLastAction();

			t.scheduleState(State.Betting);
		}
	}

	/**
	 * Pseudo state to make a wait break
	 * 
	 * @param t Mesa involucrada
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
	 * @param t Mesa involucrada
	 */
	protected void stateAllFolded(Table t)
	{
		//Get last remaining player
		Player p = t.seats.get(t.currentPlayer).player;

		//Give the pot to the winner
		p.stake += t.pots.get(0).amount;
		t.seats.get(t.currentPlayer).bet = t.pots.get(0).amount;

		t.scheduleState(State.EndRound);
	}

	/**
	 * Handles the showdown
	 * 
	 * @param t Mesa involucrada
	 */
	protected void stateShowdown(Table t)
	{
		//TODO Start showing the cards on the screen. The player who did the last action is first showing

		//Determine winners
		Vector< Vector<PokerHandStrength> > winList = createWinList(t);

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

		t.scheduleState(State.EndRound);
	}

	/**
	 * 
	 * @param t
	 */
	protected void stateEndRound(Table t)
	{
		//Lets look for broken players to get them off from the game
		for(int i=0; i<5; i++)
		{
			if(t.seats.get(i).occupied) //Skip not occupied seats
			{
				Seat s = t.seats.get(i);

				//Check if player has no stake left
				if(s.player.stake == 0)
					//TODO Tell player that he is out (TOAST)
					s.occupied = false;
			}
		}

		//Determine next dealer
		t.dealer = t.getNextPlayer(t.dealer);

		t.scheduleState(State.NewRound);
	}

	protected int handleTable(Table t)
	{
		if(t.state == State.NewRound)
		{
			stateNewRound(t);
		}
		else if(t.state == State.Blinds)
		{
			stateBlinds(t);
		}
		else if(t.state == State.Betting)
		{
			stateBetting(t);
		}
		else if(t.state == State.BettingEnd)
		{
			stateBettingEnd(t);
		}
		else if(t.state == State.AskShow)
		{
			stateAskShow(t);
		}
		else if(t.state == State.AllFolded)
		{
			stateAllFolded(t);
		}
		else if(t.state == State.Showdown)
		{
			stateShowdown(t);
		}
		else if(t.state == State.EndRound)
		{
			stateEndRound(t);
		}

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

		table.scheduleState(State.NewRound);
	}

	public int tick()
	{
		if(!started) //If the game is not set as started
		{
			System.out.println("!started");
			if(getPlayerCount() == max_players) //Start the game if player count reached
				start();
			else if(getPlayerCount() == 0 && !getRestart()) //Delete the game if no player registered
				return -1;
			else //Nothing to do, early exit
				return 0;
		}
		else if(ended) //If the game is set as ended
		{
			System.out.println("ended");
			//Remove all players
			players.clear();
			return -1;
		}

		//Handle table
		if(handleTable(table) < 0) //Is the table closed?
		{
			System.out.println("¡handleTable(table) < 0!");
			ended = true;
			//TODO ended_time + snap
		}
		return 0;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

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

		public void setBlindFactor(int bFactor) {
			blinds_factor = bFactor;
		}

		public int getBlindFactor() {
			return blinds_factor;
		}
	}
}
