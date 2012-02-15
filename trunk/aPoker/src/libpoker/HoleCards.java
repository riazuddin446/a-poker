package libpoker;

import java.util.ArrayList;

public class HoleCards {

	ArrayList<Card> cards;

	public boolean setCards(Card c1, Card c2){
		cards.clear();
		cards.add(c1);
		cards.add(c2);
		return true;
	}

	public void clear(){
		cards.clear();
	}

}
