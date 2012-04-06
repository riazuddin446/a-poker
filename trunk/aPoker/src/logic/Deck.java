package logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EmptyStackException;


public class Deck {

	private ArrayList<Card> cards;

	//Fill the deck with cards and shuffle it
	public void clearFillAndShuffle()
	{
		cards.clear();

		for(Card c : Card.values()){
			cards.add(c);
		}

		Collections.shuffle(cards);
	}

	//Clear the deck
	public void clear(){
		cards.clear();
	}

	//Return the number of cards that actually are on the deck
	public int count(){
		return cards.size();
	}

	//Add a card at the end of the deck
	public boolean push(Card card){
		cards.add(card);
		return true;
	}

	//Return the last card of the deck and erase it
	public Card pop(){
		if(count() == 0)
			throw new EmptyStackException();
		
		Card c = cards.get(cards.size()); //Get the last card
		cards.remove(cards.size()); //Remove it
		return c;
	}
}
