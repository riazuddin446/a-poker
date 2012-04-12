package logic;

import java.util.ArrayList;
import java.util.List;

public class HoldemHandEvaluator extends PokerHandEvaluator{

	public HoldemHandEvaluator(List<Card> hole_cards, List<Card> community_cards)
	{
		super();

		if (hole_cards.size() > 2 || community_cards.size() > 5)
			throw new IllegalArgumentException();

		//Copy all hole cards
		for (Card c : hole_cards)
			this.all_cards.add((Card) c);

		//Copy all community cards
		for (Card c : community_cards)
			this.all_cards.add((Card) c);

		//For Texas Hold'em we can use every card from hole and community
		this.strength = new PokerHandStrength();
		evaluate();
	}
}
