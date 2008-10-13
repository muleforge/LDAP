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

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.api.endpoint.OutboundEndpoint;
import org.mule.api.transport.MessageAdapter;
import org.mule.providers.ldap.util.LDAPUtils;
import org.mule.transport.AbstractMessageDispatcher;

import com.novell.ldap.LDAPAddRequest;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPDeleteRequest;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPMessage;
import com.novell.ldap.LDAPModifyRequest;
import com.novell.ldap.LDAPSearchConstraints;
import com.novell.ldap.LDAPSearchRequest;
import com.novell.ldap.LDAPSearchResults;
import com.novell.ldap.util.DN;

/**
 * <code>LdapMessageDispatcher</code> TODO document
 */
public class LdapMessageDispatcher extends AbstractMessageDispatcher
{

   
    
    private static final int DEFAULT_MINIMAL_TIMEOUT = 2000;
    private LdapConnector connector;

    public LdapMessageDispatcher(OutboundEndpoint endpoint)
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

    public void doDispatch(MuleEvent event) throws Exception
    {

        if (logger.isDebugEnabled())
        {
            logger.debug("doDispatch(MuleEvent event)");
        }

        Object transformed = event.transformMessage();
        

        if (transformed instanceof LDAPMessage)
        {
            LDAPMessage tranformed = (LDAPMessage) transformed;
          

            if (event.getMessage().getCorrelationId() != null)
            {
                tranformed.setTag(event.getMessage().getCorrelationId());
            }

            connector.doAsyncRequest(tranformed);

        }
        else
        // not an instance of LDAPMessage
        {
            Object unknownMsg = event.transformMessage();

            if (unknownMsg == null)
            {
                return;
            }

            logger.debug("unknown is of type: " + unknownMsg.getClass());

            String query = LDAPUtils.getSearchStringFromEndpoint(endpoint,
                    unknownMsg);
            
            logger.debug("query: "+query);

            LDAPSearchRequest request = LDAPUtils.createSearchRequest(
                    connector, query);

            if (event.getMessage().getCorrelationId() != null)
            {
                request.setTag(event.getMessage().getCorrelationId());
            }

            connector.doAsyncRequest(request);

        }

    }

    @Override
    protected MuleMessage doSend(MuleEvent event) throws Exception{

        if (logger.isDebugEnabled())
        {
            logger.debug("entering doSend(MuleEvent event)");
        }

        LDAPConnection lc = connector.getLdapConnection();
        Object transformed = event.transformMessage();

        if (transformed instanceof LDAPMessage)
        {
            LDAPMessage tranformed = (LDAPMessage) transformed;

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

                MessageAdapter adapter = connector.getMessageAdapter(res);

               
       
                
                return new DefaultMuleMessage(adapter);
            }
            else
            {
                throw new IllegalArgumentException("type "
                        + tranformed.getClass() + " cannot be send");
            }

            // return event.getMessage();

        }
        else if (transformed instanceof DN)
        {

            DN dn = (DN) transformed;
            LDAPEntry entry = lc.read(dn.toString());

            // TODO
            // UMOMessageAdapter adapter = connector.getMessageAdapter(entry);

            return new DefaultMuleMessage(entry);
        }
        else
        // not an instance of LDAPMessage
        {
            Object unknownMsg = event.transformMessage();

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

            MessageAdapter adapter = connector.getMessageAdapter(res);

            return new DefaultMuleMessage(adapter);

        }

        // doDispatch(event);
        return event.getMessage();
    }

    public MuleMessage doReceive(long timeout) throws Exception
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
            // logger.debug("try to poll");
            MuleMessage msg = receiver.pollOnce();

            if (msg != null)
            {
                return msg;
            }

            long sleep = Math.min(DEFAULT_MINIMAL_TIMEOUT, timeout
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

    } // end method

    public void doDispose()
    {

    }

}
