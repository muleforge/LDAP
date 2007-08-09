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

import org.mule.impl.MuleMessage;
import org.mule.providers.AbstractMessageDispatcher;
import org.mule.providers.ldap.util.LDAPUtils;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOImmutableEndpoint;
import org.mule.umo.provider.UMOMessageAdapter;

import com.novell.ldap.LDAPAddRequest;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPDeleteRequest;
import com.novell.ldap.LDAPMessage;
import com.novell.ldap.LDAPModifyRequest;
import com.novell.ldap.LDAPSearchConstraints;
import com.novell.ldap.LDAPSearchRequest;
import com.novell.ldap.LDAPSearchResults;

/**
 * <code>LdapMessageDispatcher</code> TODO document
 */
public class LdapMessageDispatcher extends AbstractMessageDispatcher
{

    private LdapConnector connector;

    public LdapMessageDispatcher(UMOImmutableEndpoint endpoint)
    {
        super(endpoint);

        connector = (LdapConnector) endpoint.getConnector();
    }

    public void doConnect() throws Exception
    {

        this.connector.ensureConnected();
    }

    public void doDisconnect() throws Exception
    {

    }

    public void doDispatch(UMOEvent event) throws Exception
    {

        if (logger.isDebugEnabled())
        {
            logger.debug("doDispatch(UMOEvent event)");
        }

        if (event.getTransformedMessage() instanceof LDAPMessage)
        {
            LDAPMessage tranformed = (LDAPMessage) event
                    .getTransformedMessage();

            if (event.getMessage().getCorrelationId() != null)
            {
                tranformed.setTag(event.getMessage().getCorrelationId());
            }

            connector.doAsyncRequest(tranformed);

        }
        else
        // not an instance of LDAPMessage
        {
            Object unknownMsg = event.getTransformedMessage();

            if (unknownMsg == null)
            {
                return;
            }

            logger.debug("unknown is of type: " + unknownMsg.getClass());

            String query = LDAPUtils.getSearchStringFromEndpoint(endpoint,
                    unknownMsg);

            LDAPSearchRequest request = LDAPUtils.createSearchRequest(
                    connector, query);

            if (event.getMessage().getCorrelationId() != null)
            {
                request.setTag(event.getMessage().getCorrelationId());
            }

            connector.doAsyncRequest(request);

        }

    }

    public UMOMessage doSend(UMOEvent event) throws Exception
    {

        if (logger.isDebugEnabled())
        {
            logger.debug("entering doSend(UMOEvent event)");
        }

        LDAPConnection lc = connector.getLdapConnection();

        if (event.getTransformedMessage() instanceof LDAPMessage)
        {
            LDAPMessage tranformed = (LDAPMessage) event
                    .getTransformedMessage();

            if (event.getMessage().getCorrelationId() != null)
            {
                tranformed.setTag(event.getMessage().getCorrelationId());
            }

            if (tranformed instanceof LDAPAddRequest)
            {
                lc.add(((LDAPAddRequest) tranformed).getEntry());
            }
            else if (tranformed instanceof LDAPDeleteRequest)
            {
                lc.delete(((LDAPDeleteRequest) tranformed).getDN());
            }
            else if (tranformed instanceof LDAPModifyRequest)
            {
                lc.modify(((LDAPModifyRequest) tranformed).getDN(),
                        ((LDAPModifyRequest) tranformed).getModifications());
            }
            else if (tranformed instanceof LDAPSearchRequest)
            {

                LDAPSearchRequest sr = ((LDAPSearchRequest) tranformed);

                LDAPSearchResults res = lc.search(sr.getDN(), sr.getScope(), sr
                        .getStringFilter(), sr.getAttributes(), sr
                        .isTypesOnly(), (LDAPSearchConstraints) null);

                UMOMessageAdapter adapter = connector.getMessageAdapter(res);

                return new MuleMessage(adapter);
            }
            else
            {
                throw new IllegalArgumentException("type "
                        + tranformed.getClass() + " cannot be send");
            }

            // return event.getMessage();

        }
        else
        // not an instance of LDAPMessage
        {
            Object unknownMsg = event.getTransformedMessage();

            if (unknownMsg == null)
            {
                return null;
            }

            logger.debug("unknown is of type: " + unknownMsg.getClass());

            String query = LDAPUtils.getSearchStringFromEndpoint(endpoint,
                    unknownMsg);

            LDAPSearchRequest sr = LDAPUtils.createSearchRequest(connector,
                    query);

            if (event.getMessage().getCorrelationId() != null)
            {
                sr.setTag(event.getMessage().getCorrelationId());
            }

            LDAPSearchResults res = lc.search(sr.getDN(), sr.getScope(), sr
                    .getStringFilter(), sr.getAttributes(), sr.isTypesOnly(),
                    (LDAPSearchConstraints) null);

            UMOMessageAdapter adapter = connector.getMessageAdapter(res);

            return new MuleMessage(adapter);

        }

        // doDispatch(event);
        return event.getMessage();
    }

    public UMOMessage doReceive(long timeout) throws Exception
    {
        logger.debug("entering doReceive(long timeout)");

        LDAPQueueReceiver receiver = new LDAPQueueReceiver(connector, endpoint,
                null);

        long t0 = System.currentTimeMillis();
        if (timeout < 0)
        {
            timeout = Long.MAX_VALUE;
        }

        do
        {

            UMOMessage msg = receiver.pollOnce();

            if (msg != null)
            {
                return msg;
            }

            long sleep = Math.min(2000, timeout
                    - (System.currentTimeMillis() - t0));

            if (sleep > 0)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("No results, sleeping for " + sleep);
                }
                Thread.sleep(sleep);
            }
            else
            {

                logger.debug("Timeout");
                return null;
            }

        }
        while (true);

    }// end method

    public void doDispose()
    {

    }

}
