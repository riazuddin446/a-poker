package logic;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Vector;

import logic.PokerHandStrength.HandRanking;

public abstract class PokerHandEvaluator
{
	final protected Vector<Card> all_cards = new Vector<Card>();
	protected PokerHandStrength strength = null;

	public PokerHandStrength getStrength()
	{
		return strength;
	}

	protected void evaluate()
	{
		//Sort cards descending
		Collections.sort(this.all_cards, Collections.reverseOrder(new CardComparator()));

		strength.kickerCards.clear();
		strength.rankCards.clear();

		// test combinations from highest to lowest
		if (evalStraightFlush())
			strength.ranking = HandRanking.STRAIGHTFLUSH;
		else if (evalXOfAKind(4, null))
			strength.ranking = HandRanking.FOUROFAKIND;
		else if (evalFullHouse())
			strength.ranking = HandRanking.FULLHOUSE;
		else if (evalFlush())
			strength.ranking = HandRanking.FLUSH;
		else if (evalStraight(null))
			strength.ranking = HandRanking.STRAIGHT;
		else if (evalXOfAKind(3, null))
			strength.ranking = HandRanking.THREEOFAKIND;
		else if (evalTwoPair())
			strength.ranking = HandRanking.TWOPAIR;
		else if (evalXOfAKind(2, null))
			strength.ranking = HandRanking.ONEPAIR;
		else
		{
			evalHighCard();
			strength.ranking = HandRanking.HIGHCARD;
		}
	}

	protected boolean evalFlush()
	{
		boolean isFlush = false;
		Color flushSuit = null;

		int[] suitCount = new int[Color.values().length];	// FIXME: better method

		for (Card c : this.all_cards)
		{
			int idx = c.getColor().ordinal();	// FIXME: avoid using ordinal

			// do we already have 5 cards with same suit?
			if (++suitCount[idx] == 5)
			{
				flushSuit = c.getColor();
				isFlush = true;
				break;
			}
		}


		if (isFlush)
		{
			Iterator<Card> citr = this.all_cards.listIterator();
			while (citr.hasNext() && strength.rankCards.size() < 5)
			{
				Card c = citr.next();

				// only set cards which have the flush suit
				if (c.getColor() == flushSuit)
					strength.rankCards.add(c);
			}
		}

		return isFlush;
	}

	protected boolean evalFullHouse()
	{
		boolean isFullHouse = false;

		// check for three-of-a-kind
		if (evalXOfAKind(3, null))
		{
			// remember three-of-a-kind card (is first element of rank cards)
			Card toakCard = (Card) strength.rankCards.firstElement();

			// clear rank and kicker cards before next test
			strength.kickerCards.clear();
			strength.rankCards.clear();

			// check for additional pair but different face (using ignoreFace)
			if (evalXOfAKind(2, toakCard.getValue()))
			{
				// remember pair card
				Card pCard = strength.rankCards.firstElement();

				// clear rank and kicker cards again, as we need to set them up for full house
				strength.kickerCards.clear();
				strength.rankCards.clear();

				// set three-of-a-kind and pair card as rank cards
				strength.rankCards.add(toakCard);
				strength.rankCards.add(pCard);

				isFullHouse = true;
			}
			else
			{
				// clear rank and kicker cards, as we don't have a full house
				strength.kickerCards.clear();
				strength.rankCards.clear();
			}

		}

		return isFullHouse;
	}

	protected void evalHighCard()
	{
		// set first card as rank card
		strength.rankCards.add(this.all_cards.firstElement());

		// set remaining cards as kicker
		for (int i = 1; i < this.all_cards.size() && i < 5; ++i)
			strength.kickerCards.add(this.all_cards.firstElement());
	}

	protected boolean evalStraight(Color respectSuit)
	{
		boolean isStraight = false;
		int count = 0;
		Value lastFace = null;
		Card high = null;

		for (Card c : this.all_cards)
		{
			// skip unfit suit (can be null); needed for straight-flush determination
			if (respectSuit != null && c.getColor() != respectSuit)
				continue;

			// ignore cards with same face in sequence (e.g. Qs and Qc)
			if (c.getValue() == lastFace)
				continue;

			// is there a descending sequence of cards?
			if (lastFace == null || !c.getValue().isOneLessThan(lastFace))
			{
				// first hit out of 5
				count = 1;

				// remember high card
				high = c;
			}
			else
			{
				// already 5 cards in a sequence, which is a straight?
				if (++count == 5)
				{
					isStraight = true;
					break;
				}
			}

			// remember the card's face for subsequent test
			lastFace = c.getValue();
		}


		// test for wheel-straight (A2345)
		if (count == 4 && (lastFace == Value.TWO && this.all_cards.firstElement().getValue() == Value.ACE))
		{
			// check suit when testing for StraightFlush
			if (respectSuit == null || this.all_cards.firstElement().getColor() == respectSuit)
				isStraight = true;
		}


		if (isStraight)
		{
			// set high card of straight as rank card
			strength.rankCards.add(high);
		}

		return isStraight;
	}

	protected boolean evalStraightFlush()
	{
		boolean isStraightFlush = false;

		// test for flush
		if (evalFlush())
		{
			// remember flush suit
			Color flushSuit = ((Card) strength.rankCards.firstElement()).getColor();

			// clear rank cards before next test
			strength.rankCards.clear();

			// test for straight with flush suit
			if (evalStraight(flushSuit))
			{
				isStraightFlush = true;
			}
			else
			{
				// clear rank cards, as we don't have straight flush
				strength.rankCards.clear();
			}
		}

		return isStraightFlush;
	}

	protected boolean evalTwoPair()
	{
		boolean isTwoPair = false;

		// check for at least one pair
		if (evalXOfAKind(2, null))
		{
			// remember first pair card (is first element of rank cards)
			Card fpCard = (Card) strength.rankCards.firstElement();

			// clear rank and kicker cards before next test
			strength.kickerCards.clear();
			strength.rankCards.clear();

			// check for another, different pair (using ignoreFace)
			if (evalXOfAKind(2, fpCard.getValue()))
			{
				// remember second pair card
				Card spCard = (Card) strength.rankCards.firstElement();

				// clear rank and kicker cards again, as we need to set them up for two pairs
				strength.kickerCards.clear();
				strength.rankCards.clear();

				// set first and second pair card as rank cards
				strength.rankCards.add(fpCard);
				strength.rankCards.add(spCard);

				// set the remaining one card as kicker
				Iterator<Card> citr = this.all_cards.listIterator();
				while (citr.hasNext() && strength.kickerCards.size() < 1)
				{
					Card c = citr.next();

					// only set the non-pair card
					if (c.getValue() != fpCard.getValue() && c.getValue() != spCard.getValue())
						strength.kickerCards.add(c);
				}

				isTwoPair = true;
			}
			else
			{
				// clear rank and kicker cards, as we don't have two pair
				strength.kickerCards.clear();
				strength.rankCards.clear();
			}

		}

		return isTwoPair;
	}

	protected boolean evalXOfAKind(int n, Value ignoreFace)
	{
		int count = 0;
		boolean isXOfAKind = false;
		Value lastFace = null;
		Card high = null;

		for (Card c : this.all_cards)
		{
			// skip face which should be ignored (can be null); needed for two pair determination
			if (c.getValue() == ignoreFace)
				continue;

			// do we have a sequence?
			if (c.getValue() != lastFace)
			{
				// remember face for subsequent comparison
				lastFace = c.getValue();

				// first hit out of n
				count = 1;

				// remember high card
				high = c;
			}
			else
			{
				// we've got the needed sequence, XOfAKind
				if (++count == n)
				{
					isXOfAKind = true;
					break;
				}
			}
		}


		if (isXOfAKind)
		{
			// set XOfAKind card as rank card
			strength.rankCards.add(high);

			// set max. (5-n) cards as kicker cards
			Iterator<Card> citr = this.all_cards.listIterator();
			while (citr.hasNext() && strength.kickerCards.size() < (5 - n))
			{
				Card c = citr.next();

				// only set non-XOfAKind cards
				if (c.mValue != lastFace)
					strength.kickerCards.add(c);
			}
		}

		return isXOfAKind;
	}

	/**
	 * 
	 * 
	 * @param hands
	 * @param winList
	 */
	protected void getWinList(Vector<PokerHandStrength> hands, Vector< Vector<PokerHandStrength> > winList)
	{
		winList.clear();
		winList.add(hands);

		int index = 0;
		while(true)
		{
			Vector<PokerHandStrength> tw = winList.get(index);
			Vector<PokerHandStrength> tmp = new Vector<PokerHandStrength>();

			//Sort tw in descending order
			Comparator comparator = Collections.reverseOrder();
			Collections.sort(tw, comparator);

			for(int i = tw.size()-1; i > 0; i--)
			{
				if(tw.get(i).compareTo(tw.get(0)) < 0) //FIXME
				{
					tmp.add(tw.get(i));
					tw.remove(tw.size());
				}
			}
			
			if(tmp.size() == 0)
				break;
			
			winList.add(tmp);
			index++;
		}
	}
}