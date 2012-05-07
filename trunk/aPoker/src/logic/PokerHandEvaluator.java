package logic;

import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

import logic.PokerHandStrength.HandRanking;

public abstract class PokerHandEvaluator
{

	// ===========================================================
	// Elements
	// ===========================================================

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	final protected Vector<Card> all_cards = new Vector<Card>();

	protected PokerHandStrength strength = null;

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public PokerHandStrength getStrength()
	{
		return strength; //FIXME Clone!
	}

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	/**
	 * Busca la mejor jugada
	 */
	protected void evaluate()
	{
		//Ordenar la cartas de mayor a menos valor
		Collections.sort(this.all_cards, Collections.reverseOrder(new CardComparator()));

		strength.kickerCards.clear();
		strength.rankCards.clear();

		//Busca jugadas, de mayor a menor
		if (evalStraightFlush())
			strength.setRanking(HandRanking.STRAIGHTFLUSH);
		else if (evalXOfAKind(4, null))
			strength.setRanking(HandRanking.FOUROFAKIND);
		else if (evalFullHouse())
			strength.setRanking(HandRanking.FULLHOUSE);
		else if (evalFlush())
			strength.setRanking(HandRanking.FLUSH);
		else if (evalStraight(null))
			strength.setRanking(HandRanking.STRAIGHT);
		else if (evalXOfAKind(3, null))
			strength.setRanking(HandRanking.THREEOFAKIND);
		else if (evalTwoPair())
			strength.setRanking(HandRanking.TWOPAIR);
		else if (evalXOfAKind(2, null))
			strength.setRanking(HandRanking.ONEPAIR);
		else
		{
			evalHighCard();
			strength.setRanking(HandRanking.HIGHCARD);
		}
	}

	/**
	 * Comprueba si la mano de un jugador tiene escalera de color
	 * 
	 * @return True si tiene dicha jugada
	 */
	protected boolean evalStraightFlush()
	{
		boolean isStraightFlush = false;

		//Comprobar si tiene Color
		if (evalFlush())
		{
			//Recordar el palo del Color
			Color flushSuit = strength.rankCards.firstElement().getColor();

			//Vaciar las rankCards antes del siguiente test
			strength.rankCards.clear();

			//Comprobar si tiene escalera con ese Color
			if (evalStraight(flushSuit))
			{
				isStraightFlush = true;
			}
			else
			{
				//Vaciar las rankCards, ya que no tiene Escalera de Color
				strength.rankCards.clear();
			}
		}

		return isStraightFlush;
	}

	/**
	 * Comprueba si la mano de un jugador lleva Full (Trio y doble pareja)
	 * 
	 * @return True si tiene dicha jugada
	 */
	protected boolean evalFullHouse()
	{
		boolean isFullHouse = false;

		//Buscar si tiene un trio
		if (evalXOfAKind(3, null))
		{
			//Recordar la carta del trio (es el primer elemento de las rankCards)
			Card toakCard = strength.rankCards.firstElement();

			//Vaciar rankCards y kickerCards antes del siguiente test
			strength.kickerCards.clear();
			strength.rankCards.clear();

			//Buscar una pareja adicional pero con difirente valor (usando ignoreFace)
			if (evalXOfAKind(2, toakCard.getValue()))
			{
				//Recordar el valor de la pareja
				Card pCard = strength.rankCards.firstElement();

				//Vaciar las rankCards y kickerCars otra vez, ya que lo necesitamos para formar el Full
				strength.kickerCards.clear();
				strength.rankCards.clear();

				//Añadir el trio y la pareja como rankCards
				strength.rankCards.add(toakCard);
				strength.rankCards.add(pCard);

				isFullHouse = true;
			}
			else
			{
				//Vaciar rankCards y kickerCards ya que no tenemos Full
				strength.kickerCards.clear();
				strength.rankCards.clear();
			}

		}

		return isFullHouse;
	}

	/**
	 * Comprueba si la mano de un jugador tiene Color
	 * 
	 * @return True si tiene dicha jugada
	 */
	protected boolean evalFlush()
	{
		boolean isFlush = false;
		Color flushSuit = null;

		int[] suitCount = new int[Color.values().length];	// FIXME: better method

		//Contamos la cartas con el mismo palo
		for (Card c : this.all_cards)
		{
			int idx = c.getColor().ordinal();	// FIXME: avoid using ordinal

			//Tenemos ya 5 cartas con el mismo palo?
			if (++suitCount[idx] == 5)
			{
				flushSuit = c.getColor();
				isFlush = true;
				break;
			}
		}

		if (isFlush) //Guardamos en rankCards las cartas que componen el Color
		{
			Iterator<Card> citr = this.all_cards.listIterator();
			while (citr.hasNext() && strength.rankCards.size() < 5)
			{
				Card c = citr.next();

				//Añadir solo las cartas con el palo del color como rankCards
				if (c.getColor() == flushSuit)
					strength.rankCards.add(c);
			}
		}

		return isFlush;
	}

	/**
	 * Comprueba si la mano de un jugador lleva Escalera
	 * 
	 * @param respectSuit Utilizada si buscamos Escalera de Color
	 * @return True si tiene dicha jugada
	 */
	protected boolean evalStraight(Color respectSuit)
	{
		boolean isStraight = false;
		int count = 0;
		Value lastFace = null;
		Card high = null;

		for (Card c : this.all_cards)
		{
			//Nos saltamos las cartas de distinto palo (usado para buscar escalera de color) Puede ser nulo
			if (respectSuit != null && c.getColor() != respectSuit)
				continue;

			//Ignoramos las cartas que contengan el mismo valor en la secuencia
			if (c.getValue() == lastFace)
				continue;

			//Existe una secuencia descendiente de cartas?
			if (lastFace == null || !c.getValue().isOneLessThan(lastFace))
			{
				//Primera de 5
				count = 1;

				//Recordar la carta alta
				high = c;
			}
			else
			{
				if (++count == 5) //Si tenemos 5 cartas en la secuencia, si es escalera
				{
					isStraight = true;
					break;
				}
			}

			//Guardamos el valor de la carta para la posterios comprobacion
			lastFace = c.getValue();
		}


		//Test para escalera circular (A2345)
		if (count == 4 && (lastFace == Value.TWO && this.all_cards.firstElement().getValue() == Value.ACE))
		{
			//Comprobar palo cuando estemos buscando escalera de color
			if (respectSuit == null || this.all_cards.firstElement().getColor() == respectSuit)
				isStraight = true;
		}

		if (isStraight)
		{
			//Añadir como rankCard la carta mas alta de la escalera
			strength.rankCards.add(high);
		}

		return isStraight;
	}

	/**
	 * Comprueba si el jugador tiene doble pareja
	 * 
	 * @return True si tiene dicha jugada
	 */
	protected boolean evalTwoPair()
	{
		boolean isTwoPair = false;

		//Comprobar si al menos tiene una pareja
		if (evalXOfAKind(2, null))
		{
			//Recordar la primera carta de la pareja (el primer elemento de las rankCards)
			Card fpCard = strength.rankCards.firstElement();

			//Vaciar las kickerCards y las rankCards antes del siguiente test
			strength.kickerCards.clear();
			strength.rankCards.clear();

			//Buscar otra pareja, difirente a la ya encontrada (usando ignoreFace)
			if (evalXOfAKind(2, fpCard.getValue()))
			{
				//Recordar el valor de la segunda pareja
				Card spCard = strength.rankCards.firstElement();

				//Vaciar las rankCards y kickerCards otra vez ya que tenemos que establecer las dos parejas
				strength.kickerCards.clear();
				strength.rankCards.clear();

				//Establecer una carta de cada pareja como rankCards
				strength.rankCards.add(fpCard);
				strength.rankCards.add(spCard);

				//Establecer la carta restante como kickerCard
				Iterator<Card> citr = this.all_cards.listIterator();
				while (citr.hasNext() && strength.kickerCards.size() < 1)
				{
					Card c = citr.next();

					//Solo añadir la carta que no pertence a las dos parejas
					if (c.getValue() != fpCard.getValue() && c.getValue() != spCard.getValue())
						strength.kickerCards.add(c);
				}

				isTwoPair = true;
			}
			else
			{
				//Vaciar las rankCards y kickerCards ya que no tenemos doble pareja
				strength.kickerCards.clear();
				strength.rankCards.clear();
			}

		}

		return isTwoPair;
	}

	/**
	 * Comprueba si el jugador tiene un numero X de una carta (Sirve para pareja, trio y poker)
	 * 
	 * @param n Numero de cartas iguales
	 * @param ignoreFace Valor de carta a ignorar
	 * @return True si tiene alguna de las jugadas citadas
	 */
	protected boolean evalXOfAKind(int n, Value ignoreFace)
	{
		int count = 0;
		boolean isXOfAKind = false;
		Value lastFace = null;
		Card high = null;

		for (Card c : this.all_cards)
		{
			//Nos saltamos las cartas con el valor que queremos igonrar (Necesario para buscar doble pareja) Puede ser nulo
			if (c.getValue() == ignoreFace)
				continue;

			//Tenemos una secuencia de cartas iguales? do we have a sequence?
			if (c.getValue() != lastFace)
			{
				//Recordar el valor de la carta para la siguiente comparacion
				lastFace = c.getValue();

				//Primera de n
				count = 1;

				//Recordar la carta mas alta
				high = c;
			}
			else
			{
				if (++count == n) //Tenemos la secuencia que queremos, XOfAKind
				{
					isXOfAKind = true;
					break;
				}
			}
		}


		if (isXOfAKind)
		{
			//Establecer la carta XOfAKind como rankCard
			strength.rankCards.add(high);

			//Establecer las kicker cards (5-n)
			Iterator<Card> citr = this.all_cards.listIterator();
			while (citr.hasNext() && strength.kickerCards.size() < (5 - n))
			{
				Card c = citr.next();

				//Solo las cartas que no entren dentro de las cartas XOfAKind
				if (c.mValue != lastFace)
					strength.kickerCards.add(c);
			}
		}

		return isXOfAKind;
	}

	/**
	 * Establece la carta mas alta
	 */
	protected void evalHighCard()
	{
		//Establecemos la primera carta como rankCard
		strength.rankCards.add(this.all_cards.firstElement());

		//Establecemos el resto como kickerCards
		for (int i = 1; i < this.all_cards.size() && i < 5; ++i)
			strength.kickerCards.add(this.all_cards.firstElement());
	}

	/**
	 * Separa los PokerHandStrength (de hands) de mayor a menor y las añade en winList.
	 * winList es un vector de vectores de PokerHandStrengths por la posibilidad de que haya dos de ellos del mismo valor
	 * 
	 * @param hands
	 * @param winList Lista con las manos ganadoras ordenadas de mayor a menor.
	 */
	public Vector< Vector<PokerHandStrength>> getWinList(Vector<PokerHandStrength> hands)
	{
		Vector< Vector<PokerHandStrength> > winList = new Vector< Vector<PokerHandStrength> >();
		winList.clear();
		winList.add(hands);

		int index = 0;
		while(true)
		{
			Vector<PokerHandStrength> tw = winList.get(index);
			Vector<PokerHandStrength> tmp = new Vector<PokerHandStrength>();

			//Ordenamos tw en orden descendiente
			Collections.sort(tw, Collections.reverseOrder()); //FIXME Comprobar que los ordena bien

			for(int i = tw.size()-1; i > 0; i--)
			{
				if(tw.get(i).compareTo(tw.get(0)) < 0) //FIXME
				{
					tmp.add(tw.get(i));
					tw.remove(i);
				}
			}

			if(tmp.size() == 0)
				break;

			winList.add(tmp);
			index++;
		}

		return winList;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}