package logic;

import java.util.Comparator;

public class CardComparator implements Comparator<Card>{

	@Override
	public int compare(Card c1, Card c2)
	{
		return c1.mValue.compareTo(c2.mValue);
	}

}
