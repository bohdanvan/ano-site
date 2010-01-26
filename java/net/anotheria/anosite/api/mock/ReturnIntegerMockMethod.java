package net.anotheria.anosite.api.mock;

/**
 * A mock method which always return a predefined integer value.
 * @author another
 *
 */
public class ReturnIntegerMockMethod extends ReturnObjectMockMethod{
	/**
	 * Constructor.
	 *
	 * @param aValue int param
	 */
	public ReturnIntegerMockMethod(int aValue){
		super(aValue);
	}
}
