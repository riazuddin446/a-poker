package logic;

import java.util.ArrayList;
import java.util.Collections;


public class Deck {

	private ArrayList<Card> cards;

	//Fill the deck with cards and shuffle it
	void fill(){
		cards.clear();
		for(Card c : Card.values()){
			cards.add(c);
		}
		Collections.shuffle(cards);
	}

	//Clear the deck
	void empty(){
		cards.clear();
	}

	//Return the number of cards that actually are on the deck
	int count(){
		return cards.size();
	}

	//Add a card at the end of the deck
	boolean push(Card card){
		cards.add(card);
		return true;
	}

	//Return the last card of the deck and erase it
	Card pop(){
		if(count() == 0)
			return null;
		Card c = cards.get(cards.size()); //Get the last card
		cards.remove(cards.size()); //Remove it
		return c;
	}

}
