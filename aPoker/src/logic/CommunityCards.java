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

	public void setFlop(Card c1, Card c2, Card c3)
	{
		cards.clear();
		cards.add(c1);
		cards.add(c2);
		cards.add(c3);
	}

	public void setTurn(Card c)
	{
		if(cards.size() == 3)
			cards.add(c);
	}

	public void setRiver(Card c)
	{
		if(cards.size() == 4)
			cards.add(c);
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

	public int size()
	{
		return cards.size();
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}