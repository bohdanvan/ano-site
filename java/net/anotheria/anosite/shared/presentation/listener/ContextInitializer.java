package net.anotheria.anosite.shared.presentation.listener;

import net.anotheria.anodoc.service.LockHolder;
import net.anotheria.anoplass.api.APIFinder;
import net.anotheria.anosite.blog.api.BlogAPI;
import net.anotheria.anosite.blog.api.BlogAPIFactory;
import net.anotheria.anosite.cms.helper.BoxHelperUtility;
import net.anotheria.anosite.config.Config;
import net.anotheria.anosite.wizard.api.WizardAPI;
import net.anotheria.anosite.wizard.api.WizardAPIFactory;
import net.anotheria.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;



/**
 * This listener performs the webapp initialization upon webserver start (or webapp hot-re-deploy).
 * @author lrosenberg
 * @created Feb 16, 2007
 */
public class ContextInitializer implements ServletContextListener{

	/**
	 * {@link Logger} instance.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ContextInitializer.class);

	public void contextDestroyed(ServletContextEvent event) {
		LOGGER.info("CONTEXT DESTROYED @ " + Date.currentDate());
		
	}

	public void contextInitialized(ServletContextEvent event) {
		
		String myname = event.getServletContext().getContextPath()+" context ";
		
		LOGGER.info(myname + "CONTEXT INITIALIZED @ " + Date.currentDate());
		CMSSelfTest.performSelfTest();
		BoxHelperUtility.setup();
		
		Config cfg = Config.getInstance();
		LOGGER.info(myname + "configured as " + cfg.getSystemName());

		//configure API!
		LOGGER.info(myname + "Configure api");
		APIFinder.addAPIFactory(WizardAPI.class, new WizardAPIFactory());
		APIFinder.addAPIFactory(BlogAPI.class,new BlogAPIFactory());
		LOGGER.info(myname + "API configured");
		LockHolder.addShutdownHook();
		LOGGER.info(myname + "added shutdown hook");

	}
	
}
