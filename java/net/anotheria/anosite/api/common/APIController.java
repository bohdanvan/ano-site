package net.anotheria.anosite.api.common;

import java.util.Collection;

import org.apache.log4j.Logger;

/**
 * APIController. Just help  to manage init && deInit on  app startUp or shutdown.
 */
public class APIController {

	/**
	 * Logger.
	 */
	private static Logger	log;
	/**
	 * Logger init.
	 */
	static {
		log = Logger.getLogger(APIController.class);
	}

	/**
	 * Public constructor.
	 */
	APIController() {
	}
	/**
	 * Called on application shutdown.
	 */
	public void notifyShutdown() {
		info("API is going down ... ");
		Collection<API> apis = APIFinder.getAPIs();
		for (API api : apis) {
			try {
				api.deInit();
				info("API " + api + " is down.");
			} catch (Throwable t) {
				log.error("notifyShutdown", t);
			}
		}

	}

	/*
	 * Called on application start.
	 */
	public void notifyStart() {
	}

	/**
	 * Simply adds message to log.
	 * @param message message itself
	 */
	private void info(String message) {
		if (log != null) {
			log.info(message);
		}
	}

}
