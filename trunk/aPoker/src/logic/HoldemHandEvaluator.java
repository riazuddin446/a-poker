package logic;

import java.util.ArrayList;

public class HoldemHandEvaluator extends PokerHandEvaluator{

	final protected ArrayList<Card> all_cards = new ArrayList<Card>();
	protected PokerHandStrength strength = null;

	public void getStrength(HoleCards holeCards, CommunityCards communityCards)
	{

		if (holeCards.size() > 2 || communityCards.size() > 5)
			throw new IllegalArgumentException();

		//Copy all hole cards
		for (Card c : holeCards.cards)
			this.all_cards.add((Card) c);

		//Copy all community cards
		for (Card c : communityCards.cards)
			this.all_cards.add((Card) c);

		//Get the strength of the hole cards with community cards
		this.strength = new PokerHandStrength();
		evaluate();
	}

}
