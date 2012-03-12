package logic;

import java.util.ArrayList;

public class CommunityCards {

	ArrayList<Card> cards;

	public CommunityCards(){

	}

	public boolean setFlop(Card c1, Card c2, Card c3){
		cards.clear();
		cards.add(c1);
		cards.add(c2);
		cards.add(c3);
		return true;
	}

	public boolean setTurn(Card c){
		if(cards.size() != 3)
			return false;
		cards.add(c);
		return true;
	}

	public boolean setRiver(Card c){
		if(cards.size() != 4)
			return false;
		cards.add(c);
		return true;
	}

	public void clear(){
		cards.clear();
	}

	public ArrayList<Card> copyCards(){
		return cards;
	}

	public int size(){
		return cards.size();
	}

	public enum Round{
		None,
		Flop,
		Turn,
		River;
	}

}
