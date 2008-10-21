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

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleMessage;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.transport.MessageAdapter;
import org.mule.transport.AbstractMessageRequester;

import com.novell.ldap.LDAPMessage;

public class LdapMessageRequester extends AbstractMessageRequester
{

    public LdapMessageRequester(InboundEndpoint endpoint)
    {
        super(endpoint);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected MuleMessage doRequest(long timeout) throws Exception
    {
        // TODO Auto-generated method stub

        logger.debug("doRequest(long timeout)");
        logger.debug(endpoint.toString());

        LdapConnector c = (LdapConnector) this.connector;

        LDAPMessage msg = null;

        final long start = System.currentTimeMillis();

        while (msg == null && (System.currentTimeMillis() - start) < timeout)
        {
            msg = c.pollQueue();
            Thread.yield();
            Thread.sleep(50);
        }

        if (msg == null)
        {

            logger.debug("timeout");
            return null;
        }

        MessageAdapter adapter = connector.getMessageAdapter(msg);
        return new DefaultMuleMessage(adapter);

        // throw new Exception("not implementd");
        // return new DefaultMuleMessage(null);
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
