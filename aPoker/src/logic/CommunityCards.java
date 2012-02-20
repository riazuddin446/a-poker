package logic;

import java.util.ArrayList;

public class CommunityCards {

	ArrayList<Card> cards;

	public CommunityCards(){

	}

	boolean setFlop(Card c1, Card c2, Card c3){
		cards.clear();
		cards.add(c1);
		cards.add(c2);
		cards.add(c3);
		return true;
	}

	boolean setTurn(Card c){
		if(cards.size() != 3)
			return false;
		cards.add(c);
		return true;
	}

	boolean setRiver(Card c){
		if(cards.size() != 4)
			return false;
		cards.add(c);
		return true;
	}

	void clear(){
		cards.clear();
	}

	ArrayList<Card> copyCards(){
		return cards;
	}
	
	int size(){
		return cards.size();
	}

	enum Round{
		None,
		Flop,
		Turn,
		River;
	}

}
