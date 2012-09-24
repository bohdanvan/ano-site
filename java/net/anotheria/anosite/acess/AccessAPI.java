package net.anotheria.anosite.acess;

import net.anotheria.anoplass.api.API;

/**
 * {@link API} for validate current user access to requested Page, Box, Action, Wizard, etc.
 * 
 * @author Alexandr Bolbat
 */
public interface AccessAPI extends API {

	/**
	 * Validate access for given page. If access operation not configured for given page this validation return <code>true</code>.
	 * 
	 * @param pageId
	 *            - given page it
	 * @return <code>true</code> if have access or <code>false</code>
	 * @throws AccessAPIException
	 */
	boolean isAllowedForPage(String pageId) throws AccessAPIException;

	/**
	 * Validate access for given box. If access operation not configured for given box this validation return <code>true</code>.
	 * 
	 * @param boxId
	 *            - given box it
	 * @return <code>true</code> if have access or <code>false</code>
	 * @throws AccessAPIException
	 */
	boolean isAllowedForBox(String boxId) throws AccessAPIException;

	/**
	 * Validate access for given navigation item. If access operation not configured for given navigation item this validation return <code>true</code>.
	 * 
	 * @param naviItemId
	 *            - given navigation item it
	 * @return <code>true</code> if have access or <code>false</code>
	 * @throws AccessAPIException
	 */
	boolean isAllowedForNaviItem(String naviItemId) throws AccessAPIException;

	/**
	 * Validate access for given action. If access operation not configured for given action this validation return <code>true</code>.
	 * 
	 * @param actionId
	 *            - given action it
	 * @return <code>true</code> if have access or <code>false</code>
	 * @throws AccessAPIException
	 */
	boolean isAllowedForAction(String actionId) throws AccessAPIException;

	/**
	 * Validate access for given wizard. If access operation not configured for given wizard this validation return <code>true</code>.
	 * 
	 * @param wizardId
	 *            - given wizard it
	 * @return <code>true</code> if have access or <code>false</code>
	 * @throws AccessAPIException
	 */
	boolean isAllowedForWizard(String wizardId) throws AccessAPIException;

}
