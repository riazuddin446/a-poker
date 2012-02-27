package logic;

/**
 * (c) 2010 Nicolas Gramlich 
 * (c) 2011 Zynga Inc.
 * 
 * @author Nicolas Gramlich
 * @since 18:59:46 - 18.06.2010
 */
public enum Value {
	// ===========================================================
	// Elements
	// ===========================================================
	
	TWO,
	THREE,
	FOUR,
	FIVE,
	SIX,
	SEVEN,
	EIGHT,
	NINE,
	TEN,
	JACK,
	QUEEN,
	KING,
	ACE;

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	/**
	 * Test if enum's ordinal is exactly one less than the given enum f
	 * 
	 * <p>This test is needed for game-logic (e.g. HandEvaluator for determining a straight)
	 * 
	 * @param f Value to compare to
	 * @return true if enum's ordinal is exactly one less than f
	 */
	public boolean isOneLessThan(Value f)
	{
		return (this.ordinal() + 1 == f.ordinal());
	}
	
	public String toString()
	{
		if (this.ordinal() <= TEN.ordinal())
		{
			return Integer.toString(this.ordinal() + 2);
		}
		else
		{
			switch (this)
			{
			case JACK:
				return "J";
			case QUEEN:
				return "Q";
			case KING:
				return "K";
			case ACE:
				return "A";
			default:
				throw new InternalError();
			}
		}
	}
	
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
