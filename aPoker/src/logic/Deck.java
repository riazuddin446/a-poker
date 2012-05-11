package logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EmptyStackException;

public class Deck {

	private ArrayList<Card> cards;

	public Deck()
	{
		cards = new ArrayList<Card>();
	}

	//Fill the deck with cards and shuffle it
	public void clearFillAndShuffle()
	{
		cards.clear();

		for(Card c : Card.values()){
			push(c);
		}

		Collections.shuffle(cards);
	}

	//Clear the deck
	public boolean clear()
	{
		cards.clear();
		return true;
	}

	//Return the number of cards that actually are on the deck
	public int count()
	{
		return cards.size();
	}

	//Add a card at the end of the deck
	public boolean push(Card card)
	{
		cards.add(card);
		return true;
	}

	//Return the last card of the deck and erase it
	public Card pop()
	{
		if(count() == 0)
			throw new EmptyStackException();

		Card c = cards.get(count()-1); //Get the last card
		cards.remove(count()-1); //Remove it
		return c;
	}
}
