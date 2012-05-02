package logic;

import java.util.ArrayList;

public class CommunityCards {

	// ===========================================================
	// Elements
	// ===========================================================

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	public ArrayList<Card> cards;

	// ===========================================================
	// Constructors
	// ===========================================================

	public CommunityCards()
	{
		cards = new ArrayList<Card>();
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public boolean setFlop(Card c1, Card c2, Card c3)
	{
		cards.clear();
		cards.add(c1);
		cards.add(c2);
		cards.add(c3);

		return true;
	}

	public boolean setTurn(Card c)
	{
		if(cards.size() != 3)
			return false;

		cards.add(c);

		return true;
	}

	public boolean setRiver(Card c)
	{
		if(cards.size() != 4)
			return false;

		cards.add(c);

		return true;
	}

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	public void clear()
	{
		cards.clear();
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
