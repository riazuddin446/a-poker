package logic;

import java.util.Vector;

/**
 * <pre>
 * Ranking          Rank-card(s)           Kicker-card(s)
 * ------------------------------------------------------
 * HighCard         Top card               Remaining 4
 * OnePair          Pair card              Remaining 3
 * TwoPair          1st & 2nd Pair card    Remaining 1
 * ThreeOfAKind     Trips card             Remaining 2
 * Straight         Top card               -
 * Flush            Flush cards            -
 * FullHouse        Trips & Pair card      -
 * FourOfAKind      FourOfAKind card       Remaining 1
 * StraightFlush    Top card               -
 * </pre>
 */

public class PokerHandStrength implements Comparable<PokerHandStrength>
{
	// ===========================================================
	// Elements
	// ===========================================================

	/**
	 * Lista de jugadas (HandRankings) posibles
	 * 
	 * Ordenadas de menor a mayor valor
	 */
	public enum HandRanking
	{
		HIGHCARD, ONEPAIR, TWOPAIR, THREEOFAKIND, STRAIGHT,
		FLUSH, FULLHOUSE, FOUROFAKIND, STRAIGHTFLUSH;

		@Override
		public String toString()
		{
			switch (this)
			{
			case HIGHCARD:
				return "HighCard";
			case ONEPAIR:
				return "OnePair";
			case TWOPAIR:
				return "TwoPair";
			case THREEOFAKIND:
				return "ThreeOfAKind";
			case STRAIGHT:
				return "Straight";
			case FLUSH:
				return "Flush";
			case FULLHOUSE:
				return "FullHouse";
			case FOUROFAKIND:
				return "FourOfAKind";
			case STRAIGHTFLUSH:
				return "StraightFlush";
			default:
				throw new InternalError();
			}
		}
	}

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	int id;

	HandRanking ranking;

	Vector<Card> rankCards = new Vector<Card>(); //Cartas involucradas en la jugada
	Vector<Card> kickerCards = new Vector<Card>(); //Resto de cartas, sirven para desempatar dos jugadas iguales

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public HandRanking getRanking()
	{
		return this.ranking;
	}

	public void setRanking(HandRanking ranking)
	{
		this.ranking = ranking;
	}

	public Vector<Card> getRankCards()
	{
		Vector<Card> cRank = new Vector<Card>();
		for (Card c : this.rankCards)
			cRank.add(c); //FIXME elimina c de rankCards? hay que clonarlo?

		return cRank;
	}

	public Vector<Card> getKickerCards()
	{
		Vector<Card> cKicker = new Vector<Card>();
		for (Card c : this.kickerCards)
			cKicker.add(c); //FIXME elimina c de rankCards? hay que clonarlo?

		return cKicker;
	}

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	/**
	 * Compara dos PokerHandStrength
	 * 
	 * Devuelve un negativo si este PokerHandStrength es menor al comparado, un cero si son iguales
	 * y un numero positivo si es mayor
	 * 
	 * @param o PokerHandStrength con el que queremos comparar
	 */
	public int compareTo(PokerHandStrength o)
	{
		int cret = this.ranking.compareTo(o.ranking);

		//Ambas manos tienen el mismo ranking
		if (cret == 0)
		{
			//Comparamos el valor de las rankCards
			for (int i = 0; i < this.rankCards.size(); ++i)
			{
				cret = (this.rankCards.get(0).mValue.compareTo(o.rankCards.get(0).mValue));

				if (cret != 0)
					break;
			}

			//Tienen las mismas rankCards
			if (cret == 0)
			{
				//Comparamos el valor de las kickerCards
				for (int i = 0; i < this.kickerCards.size(); ++i)
				{
					cret = (this.kickerCards.get(0).mValue.compareTo(o.kickerCards.get(0).mValue));

					if (cret != 0)
						break;
				}
			}
		}

		return cret;
	}

	@Override
	public String toString()
	{
		return "ranking=" + this.ranking + " rank=" + this.rankCards + " kicker=" + this.kickerCards;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
