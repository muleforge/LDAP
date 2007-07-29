/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.ldap;

import java.util.List;

import org.mule.impl.MuleMessage;
import org.mule.providers.AbstractMessageDispatcher;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOImmutableEndpoint;
import org.mule.umo.provider.UMOMessageAdapter;

import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPSearchResults;

/**
 * <code>LdapMessageDispatcher</code> TODO document
 */
public class LdapMessageDispatcher extends AbstractMessageDispatcher {

	private LdapConnector connector;

	public LdapMessageDispatcher(UMOImmutableEndpoint endpoint) {
		super(endpoint);

		connector = (LdapConnector) endpoint.getConnector();
	}

	public void doConnect() throws Exception {

	}

	public void doDisconnect() throws Exception {

	}

	public void doDispatch(UMOEvent event) throws Exception {

		if (logger.isDebugEnabled()) {
			logger.debug("doDispatch(UMOEvent event)");
		}

		
			throw new UnsupportedOperationException(
					"asynchronus dispatching not yet supported");

		/*
		 * IMPLEMENTATION NOTE: This is invoked when the endpoint is
		 * asynchronous. It should invoke the transport but not return any
		 * result. If a result is returned it should be ignorred, but if the
		 * underlying transport does have a notion of asynchronous processing,
		 * that should be invoked. This method is executed in a different thread
		 * to the request thread.
		 */

		// TODO Write the client code here to dispatch the event over this
		// transport

	}

	public UMOMessage doSend(UMOEvent event) throws Exception {

		if (logger.isDebugEnabled()) {
			logger.debug("entering doSend(UMOEvent event)");
		}

		/*
		 * IMPLEMENTATION NOTE: Should send the event payload over the
		 * transport. If there is a response from the transport it shuold be
		 * returned from this method. The sendEvent method is called when the
		 * endpoint is running synchronously and any response returned will
		 * ultimately be passed back to the callee. This method is executed in
		 * the same thread as the request thread.
		 */

		// TODO Write the client code here to send the event over this
		// transport (or to dispatch the event to a store or repository)
		// TODO Once the event has been sent, return the result (if any)
		// wrapped in a MuleMessage object
		String searchStr = LDAPUtils.getSearchStringFromEndpoint(endpoint,
				event.getTransformedMessage());

		LdapConnector connector = (LdapConnector) this.connector;

		LDAPSearchResults results = connector.doSearch(searchStr);
		List<LDAPEntry> entries = connector.extractEntriesFromResults(results);

		logger.debug("entryCount " + entries.size());

		logger.debug("entries " + entries);

		UMOMessageAdapter msgAdapter = this.connector
				.getMessageAdapter(entries);
		UMOMessage message = new MuleMessage(msgAdapter);

		return message;
	}

	public UMOMessage doReceive(long timeout) throws Exception {
		/*
		 * IMPLEMENTATION NOTE: Can be used to make arbitary requests to a
		 * transport resource. if the timeout is 0 the method should block until
		 * an event on the endpoint is received.
		 */

		// TODO Write the client code here to perform a request over the
		// transport
		// UMOImmutableEndpoint endpoint = event.getEndpoint();
		if (logger.isDebugEnabled()) {
			logger.debug("entering doReceive(long timeout)");
		}

		
			throw new UnsupportedOperationException(
					"asynchronus dispatching not yet supported");

		
		
	}

	public void doDispose() {
		
	}

}
