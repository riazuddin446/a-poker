package server;

import java.util.ArrayList;
import java.util.Vector;

import logic.CommunityCards;
import logic.Deck;
import logic.Player;
import logic.PokerHandStrength;
import android.text.format.Time;

public class Table
{
	// ===========================================================
	// Elements
	// ===========================================================

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	public Deck deck;
	public CommunityCards communitycards;

	public State state; //Estado de la mesa

	public ArrayList<Seat> seats;
	public int dealer, sb, bb;
	public int currentPlayer;
	public int lastBetPlayer; //Ultimo jugador que a apostado

	public BettingRound betround; //Ronda de apuestas

	public int bet_amount; //Apuesta en curso
	public int last_bet_amount; //Ultima apuesta

	public Vector<Pot> pots; //Botes

	public boolean nomoreaction;

	// ===========================================================
	// Constructors
	// ===========================================================

	public Table()
	{
		deck = new Deck();
		communitycards = new CommunityCards();
		seats = new ArrayList<Seat>();
		pots = new Vector<Pot>();

		state = State.GameStart;
		betround = BettingRound.Preflop;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	/**
	 * Busca el siguiente jugador a partir de i
	 *
	 * @param i Jugador a partir del cual queremos buscar
	 * @return Integer apuntando al siguiente jugador
	 */
	public int getNextPlayer(int i)
	{
		int start, cur;
		start = i;
		cur = i;
		boolean found = false;

		while (!found)
		{
			cur++;

			if(cur >= 5) //We have done the full turn
				cur = 0;

			if(cur == start) //No next player
				return -1;

			else if(seats.get(cur).occupied)
				found = true;
		}

		return cur;
	}

	/**
	 * Busca el siguiente jugador ACTIVO a partir de i
	 *
	 * @param i Jugador a partir del cual queremos buscar
	 * @return Integer apuntando al siguiente jugador ACTIVO
	 */
	public int getNextActivePlayer(int i)
	{
		int start, cur;
		start = i;
		cur = i;
		boolean found = false;

		while (!found)
		{
			cur++;

			if(cur >= 5) //We have done the full turn
				cur = 0;

			if(cur == start) //No active player left
				return -1;

			else if(seats.get(cur).occupied && seats.get(cur).in_round)
				found = true;
		}

		return cur;
	}

	/**
	 * Cuenta el numero de seats ocupados por jugadores
	 * 
	 * @return Numero de seats ocupados por jugadores
	 */
	public int countPlayers(){

		int count = 0;

		for(int i=0; i<5; i++){
			if(seats.get(i).occupied)
				count++;
		}

		return count;
	}

	/**
	 * Cuenta el numero de seats ocupados por jugadores ACTIVOS
	 * 
	 * @return Numero de seats ocupados por jugadores ACTIVOS
	 */
	public int countActivePlayers(){

		int count = 0;

		for(int i=0; i<5; i++){
			if(seats.get(i).occupied && seats.get(i).in_round)
				count++;
		}

		return count;
	}

	/**
	 * Comprueba que todos los jugadores, o excepto uno, van con allin
	 * 
	 * @return True si todos los jugadores, o excepto uno, van con allin
	 */
	public boolean isAllin() //All (or except one) players are allin
	{
		int count, active_players;
		count = 0;
		active_players = 0;

		for (int i=0; i<5; i++)
		{
			if(seats.get(i).occupied && seats.get(i).in_round){
				active_players++;

				if(seats.get(i).player.getStake() == 0){
					count++;
				}
			}
		}

		return (count >= active_players-1);
	}

	/**
	 * Comprueba si el asiento de la posicion 's' esta involucrado en el bote 'pot'
	 * 
	 * @param pot Pot en el que tenemos que comprobar si el seat esta involucrado
	 * @param s Posicion del seat
	 * @return True si el jugador en el seat 's'esta involucrado en el bote 'pot'
	 */
	public boolean isSeatInvolvedInPot(Pot pot, int s)
	{
		for (int i=0; i<pot.vsteats.size(); i++){
			if(pot.vsteats.get(i)==s)
				return true;
		}

		return false;
	}

	/**
	 * Cuenta el numero de seats involucrados en un pot
	 * 
	 * @param pot Bote en el que estan involucrados varios seats
	 * @param wl Lista de HandStrength ganadoras
	 * @return Numero de seats involucrados en el bote 'pot'
	 */
	public int getInvolvedInPotCount(Pot pot, Vector<PokerHandStrength> wl)
	{
		int involved_count = 0;

		for(int i=0; i<pot.vsteats.size(); i++)
		{
			int s = pot.vsteats.get(i);

			for(int j=0; j<wl.size(); j++)
			{
				if(wl.get(j).getId() == s)
					involved_count++;
			}
		}

		return involved_count;
	}

	/**
	 * 
	 */
	public void collectBets(){

		while(true)
		{
			//Find smallest bet
			int smallest_bet = 0;
			boolean need_sidepot = false;
			for(int i=0; i<5; i++)
			{
				Seat s = seats.get(i);

				//Skip folded and already handled players
				if(!s.occupied || !s.in_round || s.bet == 0)
					continue;

				//Set and initial value
				if(smallest_bet==0)
					smallest_bet = s.bet;
				else if (s.bet < smallest_bet) //New smallest bet
				{
					smallest_bet = s.bet;
					need_sidepot = true;
				}
				else if (s.bet > smallest_bet) //Bets are not equal
					need_sidepot = true; // So there must be a smallest bet
			}

			//There are no bets, do nothing
			if(smallest_bet == 0)
				return;

			//Last pot is current pot
			Pot cur_pot = pots.lastElement();

			//If current pot is final, create a new one
			if(cur_pot.isFinal)
			{
				Pot pot = new Pot();
				pot.amount = 0;
				pot.isFinal = false;
				pots.add(pot);

				cur_pot = pots.lastElement();
			}

			//Collect the bet of each player
			for(int i=0; i<5; i++)
			{
				Seat s = seats.get(i);

				//Skip invalid seats
				if(!s.occupied)
					continue;
				//Skip already handled players
				if(s.bet==0)
					continue;

				//Collect bet of folded players and skip them
				if(!s.in_round)
				{
					cur_pot.amount += s.bet;
					s.bet = 0;
					continue;
				}

				//Collect the bet into pot
				if(!need_sidepot) //We dont need sidepot
				{
					cur_pot.amount += s.bet;
					s.bet=0;
				}
				else //We need sidepot
				{
					cur_pot.amount += smallest_bet;
					s.bet -= smallest_bet;
				}

				//Mark pot as final if at least one player is allin
				if(s.player.getStake() == 0)
					cur_pot.isFinal = true;

				//Set player 'involved in pot'
				if(!isSeatInvolvedInPot(cur_pot, i)) //Check if it is not already involved
					cur_pot.vsteats.add(i);					
			}

			if(!need_sidepot) //All players bets are the same, end here
				break;
		}
	}

	public void resetLastPlayerActions()
	{
		//Reset last player action
		for(int i=0; i<5; i++)
		{
			Seat s = seats.get(i);

			if(s.occupied)
				s.player.resetLastAction();
		}

	}

	public void scheduleState(State schedState, int delay_sec)
	{
		state = schedState;
		//delay = delay_sec; //FIXME
		//delay_start = null; //FIXME
	}

	public void addPlayerToSeat(Player p)
	{
		Seat newSeat = new Seat();
		newSeat.occupied = true;
		newSeat.seat_no = seats.size()-1;
		newSeat.player = p;
		newSeat.bet = 0;
		newSeat.in_round = false;
		newSeat.showcards = false;

		seats.add(p.id, newSeat);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	public enum State {
		GameStart,
		ElectDealer,
		NewRound,
		Blinds,
		Betting,
		BettingEnd, //pseudo-state
		AskShow,
		AllFolded,
		Showdown,
		EndRound;
	}

	public enum BettingRound{
		Preflop,
		Flop,
		Turn,
		River;
	}

	public class Seat
	{
		public boolean occupied;
		public int seat_no;
		public Player player;
		public int bet;
		public boolean in_round; //Is player involved in current hand?
		public boolean showcards; //Does the player want to show cards?
	}

	public class Pot
	{
		public int amount;
		public Vector<Integer> vsteats;
		public boolean isFinal;
	}
}
