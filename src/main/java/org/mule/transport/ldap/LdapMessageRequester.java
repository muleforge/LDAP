/*
 * $Id: LdapConnector.java 172 2008-10-17 11:12:59Z hsaly $
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.ldap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EventObject;
import java.util.List;
import java.util.Map;

import org.mule.DefaultMuleMessage;
import org.mule.api.DefaultMuleException;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.transport.MessageAdapter;
import org.mule.transport.AbstractMessageRequester;

import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPMessage;
import com.novell.ldap.events.LDAPEvent;
import com.novell.ldap.events.LDAPExceptionEvent;
import com.novell.ldap.events.SearchReferralEvent;
import com.novell.ldap.events.SearchResultEvent;

public class LdapMessageRequester extends AbstractMessageRequester implements
        com.novell.ldap.events.PSearchEventListener
{
    private List < EventObject > events = Collections
            .synchronizedList(new ArrayList < EventObject >());

    public LdapMessageRequester(final InboundEndpoint endpoint)
    {
        super(endpoint);

        try
        {
            ((LdapConnector) this.connector).registerforEvent(this);
        }
        catch (final LDAPException e)
        {
            handleException(e);
        }

        // TODO Auto-generated constructor stub
    }

    /*
     * public MuleMessage doReceive(long timeout) throws Exception {
     * logger.debug("entering doReceive(long timeout)");
     * 
     * LDAPQueueReceiver receiver = new LDAPQueueReceiver(connector, endpoint,
     * null);
     * 
     * long t0 = System.currentTimeMillis(); if (timeout < 0) { timeout =
     * Long.MAX_VALUE; }
     * 
     * do { // logger.debug("try to poll"); MuleMessage msg =
     * receiver.pollOnce();
     * 
     * if (msg != null) { return msg; }
     * 
     * long sleep = Math.min(DEFAULT_MINIMAL_TIMEOUT, timeout -
     * (System.currentTimeMillis() - t0));
     * 
     * if (sleep > 0) { if (logger.isDebugEnabled()) { logger.debug("No results,
     * sleeping for " + sleep); } Thread.sleep(sleep); } else {
     * 
     * logger.debug("Timeout"); return null; } } while (true); } // end method
     * 
     * 
     */

    @Override
    protected MuleMessage doRequest(final long timeout) throws Exception
    {
        // TODO Auto-generated method stub

        logger.debug("doRequest(long timeout)");
        logger.debug(endpoint.toString());

        final LdapConnector c = (LdapConnector) this.connector;

        Object msg = null;

        final long start = System.currentTimeMillis();

        while ((msg == null)
                && ((System.currentTimeMillis() - start) < timeout))
        {
            msg = c.pollQueue();

            if (msg == null)
            {
                if (events.size() > 0)
                {
                    msg = events.get(0);
                    logger.error("Null message queued");

                }
                else
                {
                    logger.debug("No event message queued");
                }
            }

            Thread.sleep(50);
        }

        if (msg == null)
        {

            logger.debug("timeout after " + timeout + " ms");
            return null;
        }

        if (msg instanceof LDAPMessage)
        {
            final MessageAdapter adapter = connector.getMessageAdapter(msg);
            return new DefaultMuleMessage(adapter, (Map) null);
        }
        else
        {
            return new DefaultMuleMessage(msg, (Map) null);
        }
    }

    @Override
    protected void doConnect() throws Exception
    {
        ((LdapConnector) this.connector).ensureConnected();

    }

    @Override
    protected void doDisconnect() throws Exception
    {
        // TODO Auto-generated method stub

    }

    @Override
    protected void doDispose()
    {
        events.clear();
        events = null;

    }

    public void ldapEventNotification(final LDAPEvent evt)
    {

        events.add(evt);

    }

    public void ldapExceptionNotification(final LDAPExceptionEvent ldapevt)
    {
        events.add(ldapevt);

    }

    public void searchReferalEvent(final SearchReferralEvent referalevent)
    {
        events.add(referalevent);

    }

    public void searchResultEvent(final SearchResultEvent event)
    {
        events.add(event);

    }

    @Override
    protected void doStart() throws MuleException
    {

        super.doStart();
    }

    @Override
    protected void doStop() throws MuleException
    {
        // TODO Auto-generated method stub
        super.doStop();

        try
        {
            ((LdapConnector) this.connector).removeListener(this);
        }
        catch (final LDAPException e)
        {
            throw new DefaultMuleException(e);
        }

    }

}
