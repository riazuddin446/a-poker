package libpoker;

import java.util.ArrayList;
import java.util.HashMap;

import org.anddev.andengine.opengl.texture.region.TextureRegion;


import com.zorrozua.asier.Color;
import com.zorrozua.asier.Value;
import com.zorrozua.asier.Card;

public class Deck {

	private ArrayList<Card> cards;

	void fill(){
		cards.clear();
		for(Card c : Card.values()){
			cards.add(c);
		}
	}

	void empty(){
		cards.clear();
	}

	int count(){
		return cards.size();
	}

	boolean push(Card card){
		cards.add(card);
		return true;
	}

	boolean pop(Card card){
		if(count() == 0)
			return false;
		cards.remove(card);
		return true;
	}

}
