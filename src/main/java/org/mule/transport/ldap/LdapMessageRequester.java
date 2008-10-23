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

import java.util.Map;

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleMessage;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.transport.MessageAdapter;
import org.mule.transport.AbstractMessageRequester;

import com.novell.ldap.LDAPMessage;

public class LdapMessageRequester extends AbstractMessageRequester
{

    public LdapMessageRequester(final InboundEndpoint endpoint)
    {
        super(endpoint);
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

        LDAPMessage msg = null;

        final long start = System.currentTimeMillis();

        while ((msg == null)
                && ((System.currentTimeMillis() - start) < timeout))
        {
            msg = c.pollQueue();
            // Thread.yield();
            Thread.sleep(50);
        }

        if (msg == null)
        {

            logger.debug("timeout after " + timeout + " ms");
            return null;
        }

        final MessageAdapter adapter = connector.getMessageAdapter(msg);
        return new DefaultMuleMessage(adapter, (Map) null);

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
        // TODO Auto-generated method stub

    }

}
