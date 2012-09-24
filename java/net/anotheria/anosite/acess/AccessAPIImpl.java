package net.anotheria.anosite.acess;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.anotheria.access.AccessService;
import net.anotheria.access.AccessServiceException;
import net.anotheria.access.SOAttribute;
import net.anotheria.access.SecurityObject;
import net.anotheria.access.impl.PermissionCollection;
import net.anotheria.access.impl.PermissionImpl;
import net.anotheria.access.impl.StaticRole;
import net.anotheria.anoplass.api.APIException;
import net.anotheria.anoplass.api.APIFinder;
import net.anotheria.anoplass.api.APIInitException;
import net.anotheria.anoplass.api.generic.login.LoginAPI;
import net.anotheria.anoprise.metafactory.MetaFactory;
import net.anotheria.anoprise.metafactory.MetaFactoryException;
import net.anotheria.anosite.acess.constraint.ParametrizedConstraint;
import net.anotheria.anosite.acess.context.SecurityContextInitializer;
import net.anotheria.anosite.gen.anoaccessapplicationdata.data.UserData;
import net.anotheria.anosite.gen.anoaccessapplicationdata.service.IAnoAccessApplicationDataService;
import net.anotheria.anosite.gen.anoaccessconfiguration.data.AccessOperation;
import net.anotheria.anosite.gen.anoaccessconfiguration.data.Constraint;
import net.anotheria.anosite.gen.anoaccessconfiguration.data.ContextInitializer;
import net.anotheria.anosite.gen.anoaccessconfiguration.data.Permission;
import net.anotheria.anosite.gen.anoaccessconfiguration.data.PermissionSortType;
import net.anotheria.anosite.gen.anoaccessconfiguration.data.Role;
import net.anotheria.anosite.gen.anoaccessconfiguration.data.RoleSortType;
import net.anotheria.anosite.gen.anoaccessconfiguration.service.AccessOperationNotFoundInAnoAccessConfigurationServiceException;
import net.anotheria.anosite.gen.anoaccessconfiguration.service.AnoAccessConfigurationServiceException;
import net.anotheria.anosite.gen.anoaccessconfiguration.service.IAnoAccessConfigurationService;
import net.anotheria.anosite.gen.ascustomaction.data.CustomActionDef;
import net.anotheria.anosite.gen.ascustomaction.service.ASCustomActionServiceException;
import net.anotheria.anosite.gen.ascustomaction.service.IASCustomActionService;
import net.anotheria.anosite.gen.assitedata.data.NaviItem;
import net.anotheria.anosite.gen.assitedata.service.ASSiteDataServiceException;
import net.anotheria.anosite.gen.assitedata.service.IASSiteDataService;
import net.anotheria.anosite.gen.aswebdata.data.Box;
import net.anotheria.anosite.gen.aswebdata.data.Pagex;
import net.anotheria.anosite.gen.aswebdata.service.ASWebDataServiceException;
import net.anotheria.anosite.gen.aswebdata.service.IASWebDataService;
import net.anotheria.anosite.gen.aswizarddata.data.WizardDef;
import net.anotheria.anosite.gen.aswizarddata.service.ASWizardDataServiceException;
import net.anotheria.anosite.gen.aswizarddata.service.IASWizardDataService;
import net.anotheria.asg.data.DataObject;
import net.anotheria.asg.util.listener.IServiceListener;
import net.anotheria.util.StringUtils;
import net.anotheria.util.log.LogMessageUtil;
import net.anotheria.util.sorter.SortType;

import org.apache.log4j.Logger;

/**
 * {@link AccessAPI} implementation.
 * 
 * @author Alexandr Bolbat
 */
public class AccessAPIImpl implements AccessAPI {

	/**
	 * {@link Logger} instance.
	 */
	private static final Logger LOGGER = Logger.getLogger(AccessAPIImpl.class);

	/**
	 * {@link AccessService} instance.
	 */
	private AccessService accessService;

	/**
	 * Ano-site configuration for {@link AccessService}.
	 */
	private IAnoAccessConfigurationService accessConfigurationService;

	/**
	 * Ano-site persistence for access user2roles data.
	 */
	private IAnoAccessApplicationDataService accessApplicationDataService;

	/**
	 * Pages and boxes configuration.
	 */
	private IASWebDataService pagesConfigurationPersistence;

	/**
	 * Navigation items configuration.
	 */
	private IASSiteDataService siteDataService;

	/**
	 * Custom actions configuration.
	 */
	private IASCustomActionService customActionsConfigurationPersistence;

	/**
	 * Wizards configuration.
	 */
	private IASWizardDataService wizardConfigurationPersistence;

	/**
	 * {@link LoginAPI} instance.
	 */
	private LoginAPI loginAPI;

	@Override
	public void init() throws APIInitException {
		try {
			accessService = MetaFactory.get(AccessService.class);
			accessConfigurationService = MetaFactory.get(IAnoAccessConfigurationService.class);
			accessApplicationDataService = MetaFactory.get(IAnoAccessApplicationDataService.class);
			pagesConfigurationPersistence = MetaFactory.get(IASWebDataService.class);
			siteDataService = MetaFactory.get(IASSiteDataService.class);
			customActionsConfigurationPersistence = MetaFactory.get(IASCustomActionService.class);
			wizardConfigurationPersistence = MetaFactory.get(IASWizardDataService.class);
		} catch (MetaFactoryException e) {
			String message = LogMessageUtil.failMsg(e) + " Can't initialize required services.";
			LOGGER.fatal(message, e);
			throw new APIInitException(message, e);
		}

		loginAPI = APIFinder.findAPI(LoginAPI.class);

		try {
			configureAccessService();
		} catch (AccessAPIException e) {
			String message = LogMessageUtil.failMsg(e) + " Can't initialize access service with current configuration.";
			LOGGER.fatal(message, e);
			throw new APIInitException(message, e);
		}

		accessConfigurationService.addServiceListener(new AccessConfigurationChangeListener());
		accessApplicationDataService.addServiceListener(new AccessUserDataChangeListener());
	}

	@Override
	public void deInit() {
	}

	@Override
	public boolean isAllowedForPage(final String pageId) throws AccessAPIException {
		if (!AccessAPIConfig.getInstance().isEnabled()) // allowing access if access control disabled by configuration
			return true;

		try {
			Pagex page = pagesConfigurationPersistence.getPagex(pageId);
			return isAllowed(page.getAccessOperation());
		} catch (ASWebDataServiceException e) {
			String message = LogMessageUtil.failMsg(e, pageId);
			LOGGER.error(message, e);
			throw new AccessAPIException(message, e);
		}
	}

	@Override
	public boolean isAllowedForBox(final String boxId) throws AccessAPIException {
		if (!AccessAPIConfig.getInstance().isEnabled()) // allowing access if access control disabled by configuration
			return true;

		try {
			Box box = pagesConfigurationPersistence.getBox(boxId);
			return isAllowed(box.getAccessOperation());
		} catch (ASWebDataServiceException e) {
			String message = LogMessageUtil.failMsg(e, boxId);
			LOGGER.error(message, e);
			throw new AccessAPIException(message, e);
		}
	}

	@Override
	public boolean isAllowedForNaviItem(String naviItemId) throws AccessAPIException {
		if (!AccessAPIConfig.getInstance().isEnabled()) // allowing access if access control disabled by configuration
			return true;

		try {
			NaviItem naviItem = siteDataService.getNaviItem(naviItemId);
			return isAllowed(naviItem.getAccessOperation());
		} catch (ASSiteDataServiceException e) {
			String message = LogMessageUtil.failMsg(e, naviItemId);
			LOGGER.error(message, e);
			throw new AccessAPIException(message, e);
		}
	}

	@Override
	public boolean isAllowedForAction(final String actionId) throws AccessAPIException {
		if (!AccessAPIConfig.getInstance().isEnabled()) // allowing access if access control disabled by configuration
			return true;

		try {
			CustomActionDef action = customActionsConfigurationPersistence.getCustomActionDef(actionId);
			return isAllowed(action.getAccessOperation());
		} catch (ASCustomActionServiceException e) {
			String message = LogMessageUtil.failMsg(e, actionId);
			LOGGER.error(message, e);
			throw new AccessAPIException(message, e);
		}
	}

	@Override
	public boolean isAllowedForWizard(final String wizardId) throws AccessAPIException {
		if (!AccessAPIConfig.getInstance().isEnabled()) // allowing access if access control disabled by configuration
			return true;

		try {
			WizardDef wizard = wizardConfigurationPersistence.getWizardDef(wizardId);
			return isAllowed(wizard.getAccessOperation());
		} catch (ASWizardDataServiceException e) {
			String message = LogMessageUtil.failMsg(e, wizardId);
			LOGGER.error(message, e);
			throw new AccessAPIException(message, e);
		}
	}

	/**
	 * Main API logic.
	 * 
	 * @param accessOperationId
	 *            - access operation id
	 * @return <code>true</code> if operation allowed or <code>false</code>
	 * @throws AccessAPIException
	 */
	private boolean isAllowed(final String accessOperationId) throws AccessAPIException {
		// if access operation not configured (not required)
		if (StringUtils.isEmpty(accessOperationId))
			return true;

		// if no logged user
		if (!isLoggedIn())
			return false;

		// loading context initializers required for current access operation
		Set<String> initializers = new HashSet<String>();
		for (Permission permission : getPermissions(accessOperationId))
			for (Constraint constraint : getConstraint(permission.getConstraints()))
				for (ContextInitializer contextInitializer : getContextInitializers(constraint.getContextInitializers()))
					if (contextInitializer != null && !StringUtils.isEmpty(contextInitializer.getClassName()))
						initializers.add(contextInitializer.getClassName());

		// prepare access security context
		SecurityObject securityObject = new SecurityObject(getCurrentUserId());

		for (String clazz : initializers) {
			try {
				Class<?> undefinedClass = Class.forName(clazz); // loading class by name
				if (!SecurityContextInitializer.class.isAssignableFrom(undefinedClass)) { // validating class type
					String message = LogMessageUtil.failMsg(new RuntimeException(), accessOperationId) + " Wrong security context class[" + clazz + "] type.";
					LOGGER.warn(message);
					throw new AccessAPIException(message);
				}

				@SuppressWarnings("unchecked")
				Class<SecurityContextInitializer> initializerClass = (Class<SecurityContextInitializer>) undefinedClass;

				// instantiating initializer
				SecurityContextInitializer initializer = initializerClass.newInstance();

				// retrieving security context values
				Map<String, String> initializedValues = initializer.initialize();

				// filling security object attributes with obtained values
				for (String key : initializedValues.keySet()) {
					SOAttribute attr = new SOAttribute(key, initializedValues.get(key));
					securityObject.addAttribute(attr);
				}
			} catch (ClassNotFoundException e) {
				String message = LogMessageUtil.failMsg(e, accessOperationId) + " Wrong security context class[" + clazz + "].";
				LOGGER.warn(message, e);
				throw new AccessAPIException(message, e);
			} catch (InstantiationException e) {
				String message = LogMessageUtil.failMsg(e, accessOperationId) + " Can't instantiate security context class[" + clazz + "].";
				LOGGER.warn(message, e);
				throw new AccessAPIException(message, e);
			} catch (IllegalAccessException e) {
				String message = LogMessageUtil.failMsg(e, accessOperationId) + " Can't instantiate security context class[" + clazz + "].";
				LOGGER.warn(message, e);
				throw new AccessAPIException(message, e);
			}
		}

		// executing main security logic
		try {
			return accessService.isAllowed(getAccessOperation(accessOperationId).getId(), securityObject, null).isAllowed();
		} catch (AccessServiceException e) {
			LOGGER.warn(LogMessageUtil.failMsg(e, accessOperationId) + " Skipping exception and don't allow execution.", e);
			return false;
		}
	}

	/**
	 * Check is some user logged in.
	 * 
	 * @return <code>true</code> if logged or <code>false</code>
	 */
	private boolean isLoggedIn() {
		return loginAPI.isLogedIn();
	}

	/**
	 * Return currently logged user id.
	 * 
	 * @return {@link String} id of currently logged user
	 * @throws AccessAPIException
	 */
	private String getCurrentUserId() throws AccessAPIException {
		try {
			return loginAPI.getLogedUserId();
		} catch (APIException e) {
			throw new AccessAPIException("No logged user.");
		}
	}

	/**
	 * Return {@link AccessOperation} by given id.
	 * 
	 * @param id
	 *            - given access operation id
	 * @return {@link AccessOperation}, can't be <code>null</code>
	 * @throws AccessAPIException
	 */
	private AccessOperation getAccessOperation(final String id) throws AccessAPIException {
		try {
			return accessConfigurationService.getAccessOperation(id);
		} catch (AccessOperationNotFoundInAnoAccessConfigurationServiceException e) {
			throw new AccessAPIException("Access operation with given id[" + id + "] not found.", e);
		} catch (AnoAccessConfigurationServiceException e) {
			throw new AccessAPIException(LogMessageUtil.failMsg(e, id), e);
		}
	}

	/**
	 * Get {@link List} of {@link Role} by given id's.
	 * 
	 * @return {@link List} of {@link Role}, can be empty but not <code>null</code>
	 * @throws AccessAPIException
	 */
	private List<Role> getRoles() throws AccessAPIException {
		List<Role> result = new ArrayList<Role>();

		try {
			SortType sorting = new SortType(RoleSortType.SORT_BY_ID, RoleSortType.ASC);
			List<Role> loaded = accessConfigurationService.getRoles(sorting);
			if (loaded != null && !loaded.isEmpty())
				result.addAll(loaded);

			return result;
		} catch (AnoAccessConfigurationServiceException e) {
			String message = LogMessageUtil.failMsg(e) + " Can't load roles.";
			LOGGER.error(message, e);
			throw new AccessAPIException(message, e);
		}
	}

	/**
	 * Get {@link List} of {@link Permission} who have given access operation.
	 * 
	 * @param accessOperationId
	 *            - access operation id
	 * @return {@link List} of {@link Permission}, can be empty but not <code>null</code>
	 * @throws AccessAPIException
	 */
	private List<Permission> getPermissions(final String accessOperationId) throws AccessAPIException {
		List<Permission> result = new ArrayList<Permission>();

		try {
			SortType sorting = new SortType(PermissionSortType.SORT_BY_PRIORITY, PermissionSortType.DESC);
			List<Permission> loaded = accessConfigurationService.getPermissionsByProperty(Permission.LINK_PROP_ACCESS_OPERATION, accessOperationId, sorting);
			if (loaded != null && !loaded.isEmpty())
				result.addAll(loaded);

			return result;
		} catch (AnoAccessConfigurationServiceException e) {
			String message = LogMessageUtil.failMsg(e, accessOperationId) + " Can't load permissions.";
			LOGGER.error(message, e);
			throw new AccessAPIException(message, e);
		}
	}

	/**
	 * Get {@link List} of {@link Permission} by given id's.
	 * 
	 * @param ids
	 *            - id's
	 * @return {@link List} of {@link Permission}, can be empty but not <code>null</code>
	 * @throws AccessAPIException
	 */
	private List<Permission> getPermissions(final List<String> ids) throws AccessAPIException {
		List<Permission> result = new ArrayList<Permission>();

		if (ids == null || ids.isEmpty())
			return result;

		try {
			for (String id : ids)
				result.add(accessConfigurationService.getPermission(id));

			return result;
		} catch (AnoAccessConfigurationServiceException e) {
			String message = LogMessageUtil.failMsg(e, ids) + " Can't load permissions.";
			LOGGER.error(message, e);
			throw new AccessAPIException(message, e);
		}
	}

	/**
	 * Get {@link List} of {@link Constraint} by given id's.
	 * 
	 * @param ids
	 *            - id's
	 * @return {@link List} of {@link Constraint}, can be empty but not <code>null</code>
	 * @throws AccessAPIException
	 */
	private List<Constraint> getConstraint(final List<String> ids) throws AccessAPIException {
		List<Constraint> result = new ArrayList<Constraint>();

		if (ids == null || ids.isEmpty())
			return result;

		try {
			for (String id : ids)
				result.add(accessConfigurationService.getConstraint(id));

			return result;
		} catch (AnoAccessConfigurationServiceException e) {
			String message = LogMessageUtil.failMsg(e, ids) + " Can't load constraints.";
			LOGGER.error(message, e);
			throw new AccessAPIException(message, e);
		}
	}

	/**
	 * Get {@link List} of {@link ContextInitializer} by given id's.
	 * 
	 * @param ids
	 *            - id's
	 * @return {@link List} of {@link ContextInitializer}, can be empty but not <code>null</code>
	 * @throws AccessAPIException
	 */
	private List<ContextInitializer> getContextInitializers(final List<String> ids) throws AccessAPIException {
		List<ContextInitializer> result = new ArrayList<ContextInitializer>();

		if (ids == null || ids.isEmpty())
			return result;

		try {
			for (String id : ids)
				result.add(accessConfigurationService.getContextInitializer(id));

			return result;
		} catch (AnoAccessConfigurationServiceException e) {
			String message = LogMessageUtil.failMsg(e, ids) + " Can't load context initializers.";
			LOGGER.error(message, e);
			throw new AccessAPIException(message, e);
		}
	}

	/**
	 * Configuring {@link AccessService} instance with current configuration.
	 * 
	 * @throws AccessAPIException
	 */
	private synchronized void configureAccessService() throws AccessAPIException {
		for (Role role : getRoles()) {
			StaticRole configuredRole = new StaticRole(role.getId());

			PermissionCollection permissionCollection = new PermissionCollection(configuredRole.getName());
			for (Permission permission : getPermissions(role.getPermissions())) {
				PermissionImpl configuredPermission = new PermissionImpl();
				configuredPermission.setName(permission.getId());
				configuredPermission.setAction(permission.getAccessOperation());
				configuredPermission.setAllow(!permission.getDeny());

				for (Constraint constraint : getConstraint(permission.getConstraints())) {
					if (StringUtils.isEmpty(constraint.getClassName())) // skipping constraint if its class not configured
						continue;

					String clazz = constraint.getClassName();
					try {
						Class<?> undefinedClass = Class.forName(clazz); // loading class by name
						if (!net.anotheria.access.impl.Constraint.class.isAssignableFrom(undefinedClass)) { // validating class type
							String message = LogMessageUtil.failMsg(new RuntimeException()) + " Wrong constraint class[" + clazz + "] type.";
							LOGGER.warn(message);
							throw new AccessAPIException(message);
						}

						if (ParametrizedConstraint.class.isAssignableFrom(undefinedClass)) {
							@SuppressWarnings("unchecked")
							Class<ParametrizedConstraint> constraintClass = (Class<ParametrizedConstraint>) undefinedClass;
							ParametrizedConstraint instance = constraintClass.newInstance();
							instance.setParameter1(constraint.getParameter1());
							instance.setParameter2(constraint.getParameter2());
							instance.setParameter3(constraint.getParameter3());
							instance.setParameter4(constraint.getParameter4());
							instance.setParameter5(constraint.getParameter5());
							// instantiating constraint and adding it to permission
							configuredPermission.addConstraint(constraintClass.newInstance());
						} else {
							@SuppressWarnings("unchecked")
							Class<net.anotheria.access.impl.Constraint> constraintClass = (Class<net.anotheria.access.impl.Constraint>) undefinedClass;
							// instantiating constraint and adding it to permission
							configuredPermission.addConstraint(constraintClass.newInstance());
						}
					} catch (ClassNotFoundException e) {
						String message = LogMessageUtil.failMsg(e) + " Wrong constraint class[" + clazz + "].";
						LOGGER.warn(message, e);
						throw new AccessAPIException(message, e);
					} catch (InstantiationException e) {
						String message = LogMessageUtil.failMsg(e) + " Can't instantiate constraint class[" + clazz + "].";
						LOGGER.warn(message, e);
						throw new AccessAPIException(message, e);
					} catch (IllegalAccessException e) {
						String message = LogMessageUtil.failMsg(e) + " Can't instantiate constraint class[" + clazz + "].";
						LOGGER.warn(message, e);
						throw new AccessAPIException(message, e);
					}
				}

				// adding permission to role permission collection
				permissionCollection.add(configuredPermission);
			}

			// configuring access service with role and its permission collection
			accessService.reset();
			accessService.addPermissionCollection(permissionCollection);
			accessService.addRole(configuredRole);
		}
	}

	/**
	 * Listener for updating {@link AccessService} configuration if it's changed in CMS.
	 * 
	 * @author Alexandr Bolbat
	 */
	public final class AccessConfigurationChangeListener implements IServiceListener {

		/**
		 * {@link Logger} instance.
		 */
		private final Logger LOGGER = Logger.getLogger(AccessUserDataChangeListener.class);

		@Override
		public void documentUpdated(final DataObject oldVersion, final DataObject newVersion) {
			updateConfiguration();
		}

		@Override
		public void documentDeleted(final DataObject doc) {
			updateConfiguration();
		}

		@Override
		public void documentCreated(final DataObject doc) {
			updateConfiguration();
		}

		@Override
		public void documentImported(final DataObject doc) {
			updateConfiguration();
		}

		@Override
		public void persistenceChanged() {
			updateConfiguration();
		}

		/**
		 * Update configuration in {@link AccessService}.
		 */
		private void updateConfiguration() {
			try {
				LOGGER.debug("Access configuration changed. Re-Configuring AccessService...");
				configureAccessService();
				LOGGER.debug("Re-Configuration of AccessService finished.");
			} catch (AccessAPIException e) {
				LOGGER.warn(LogMessageUtil.failMsg(e), e);
			}
		}

	}

	/**
	 * Listener for clearing {@link AccessService} cache if user data (roles2user mapping) changed trough CMS.
	 * 
	 * @author Alexandr Bolbat
	 */
	public final class AccessUserDataChangeListener implements IServiceListener {

		/**
		 * {@link Logger} instance.
		 */
		private final Logger LOGGER = Logger.getLogger(AccessUserDataChangeListener.class);

		@Override
		public void documentUpdated(final DataObject oldVersion, final DataObject newVersion) {
			update(oldVersion); // clearing for old userId
			update(newVersion); // clearing for new userId
		}

		@Override
		public void documentDeleted(final DataObject doc) {
			update(doc);
		}

		@Override
		public void documentCreated(final DataObject doc) {
			update(doc);
		}

		@Override
		public void documentImported(final DataObject doc) {
		}

		@Override
		public void persistenceChanged() {
		}

		/**
		 * Update configuration in {@link AccessService}.
		 */
		private void update(final DataObject doc) {
			if (!(doc instanceof UserData))
				return;

			UserData userData = UserData.class.cast(doc);
			LOGGER.debug("Access user data changed. Clearing cached user[" + userData.getUserId() + "] data in AccessService...");
			accessService.reset(userData.getUserId());
			LOGGER.debug("Clearingfinished.");
		}

	}

}
