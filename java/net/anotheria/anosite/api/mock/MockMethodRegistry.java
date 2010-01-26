package net.anotheria.anosite.api.mock;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.anotheria.anosite.api.common.API;

import org.apache.log4j.Logger;
/**
 * A registry for mocking methods.
 * @author another
 *
 */
public class MockMethodRegistry {
	/**
	 * Logger.
	 */
	private static Logger log = Logger.getLogger(MockMethodRegistry.class);
	/**
	 * MockMethodRegistry 'methods'. Actually holder for Mocked methods.
	 */
	private static Map<Method, APIMockMethod> methods = new ConcurrentHashMap<Method, APIMockMethod>();
	/**
	 * init.
	 */
	static{
		reset();
	}

	/**
	 * Adding mock method to the registry.
	 * @param m method
	 * @param mock mocked implementation
	 */
	public static void addMockMethod(Method m, APIMockMethod mock){
		methods.put(m, mock);
	}

	/**
	 * Return mocked method.
	 * @param m method itself
	 * @return mocked implementation from registry
	 */
	public static APIMockMethod getMockMethod(Method m){
		return methods.get(m);
	}

	/**
	 * Reset.
	 */
	public static void reset(){
		try{
			Method init = API.class.getMethod("init");
			addMockMethod(init, new NoopMockMethod());
		}catch(NoSuchMethodException e){
			log.fatal("Someone changed the api signature!",e );
		}

	}
}
