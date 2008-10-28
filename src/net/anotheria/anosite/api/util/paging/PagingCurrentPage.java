/* ------------------------------------------------------------------------- *
$Source$
$Author$
$Date$
$Revision$


Copyright 2004-2005 by FriendScout24 GmbH, Munich, Germany.
All rights reserved.

This software is the confidential and proprietary information
of FriendScout24 GmbH. ("Confidential Information").  You
shall not disclose such Confidential Information and shall use
it only in accordance with the terms of the license agreement
you entered into with FriendScout24 GmbH.
See www.friendscout24.de for details.
** ------------------------------------------------------------------------- */
package net.anotheria.anosite.api.util.paging;

public class PagingCurrentPage extends PagingElement{
	private String caption;
	
	public PagingCurrentPage(String aCaption){
		caption = aCaption;
	}

	@Override
	public String getCaption() {
		return caption;
	}

	@Override
	public String getPagingParameter() {
		return null;
	}

	@Override
	public boolean isActive() {
		return true;
	}

	@Override
	public boolean isLinked() {
		return false;
	}

	@Override
	public boolean isSeparator() {
		return false;
	}
	
	@Override
	public String toString(){
		return "caption:"+caption;
	}
}

/* ------------------------------------------------------------------------- *
 * $Log$
 * Revision 1.1  2006/09/20 08:09:20  lrosenberg
 * *** empty log message ***
 *
 * Revision 1.1  2006/08/04 15:47:49  lrosenberg
 * *** empty log message ***
 *
 */