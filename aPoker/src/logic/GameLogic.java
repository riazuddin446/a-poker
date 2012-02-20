package logic;

import java.util.ArrayList;

public class GameLogic {

	final protected ArrayList<Card> all_cards = new ArrayList<Card>();
	protected PokerHandStrength strength = null;
	
	public void getStrength(HoleCards holeCards, CommunityCards communityCards){

		if (holeCards.size() > 2 || communityCards.size() > 5)
			throw new IllegalArgumentException();


		// copy all hole cards
		for (Card c : holeCards)
			this.all_cards.add((AngloAmericanCard) c);

		// copy all community cards
		for (Card c : community_cards)
			this.all_cards.add((AngloAmericanCard) c);

		// for Texas Hold'em we can use every card from hole and community
		this.strength = new PokerHandStrength();
		evaluate();
	}

}
