/*
 * $Id$
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
import org.mule.api.MessagingException;
import org.mule.api.MuleException;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.lifecycle.CreateException;
import org.mule.api.service.Service;
import org.mule.api.transport.Connector;
import org.mule.api.transport.MessageAdapter;
import org.mule.transport.AbstractPollingMessageReceiver;
import org.mule.transport.ConnectException;

import com.novell.ldap.LDAPExtendedResponse;
import com.novell.ldap.LDAPMessage;
import com.novell.ldap.LDAPUnsolicitedNotificationListener;

/**
 * <code>LdapMessageReceiver</code> TODO document
 */
public class LdapMessageReceiver extends AbstractPollingMessageReceiver
        implements LDAPUnsolicitedNotificationListener
{

    /*
     * For general guidelines on writing transports see
     * http://mule.mulesource.org/display/MULE/Writing+Transports
     */

    // private LDAPQueueReceiver queueReceiver;
    public LdapMessageReceiver(final Connector connector,
            final Service service, final InboundEndpoint endpoint)
            throws CreateException
    {

        super(connector, service, endpoint);
        this.setFrequency(500);
    }

    // unsolicited notification, impl. of LDAPUnsolicitedNotificationListener
    public void messageReceived(final LDAPExtendedResponse msg)
    {
        logger.debug("Received notification of unsolicited notification");
        logger.debug("The OID in the notification was ==>" + msg.getID());

        try
        {
            final MessageAdapter adapter = connector.getMessageAdapter(msg);
            routeMessage(new DefaultMuleMessage(adapter, (Map) null), endpoint
                    .isSynchronous());

        }
        catch (final MessagingException e)
        {
            throw new RuntimeException(e);
        }
        catch (final MuleException e)
        {
            throw new RuntimeException(e);
        }

    }

    // impl. of LDAPQueueListener
    /*
     * public void onMessage(final MuleMessage message, final ImmutableEndpoint
     * endpoint) throws MuleException { logger.debug("Received async. incoming
     * ldap message (onMessage())"); routeMessage(message,
     * endpoint.isSynchronous()); }
     */

    /*
     * @Override public void poll() throws Exception {
     * 
     * final LdapConnector connector = (LdapConnector) this.connector;
     * 
     * try { connector.ensureConnected(); } catch (final
     * org.mule.transport.ConnectException e) {
     * 
     * handleException(e); } getWorkManager().doWork(queueReceiver); //
     * getWorkManager().scheduleWork(queueReceiver); }
     */

    @Override
    protected void doConnect() throws ConnectException
    {
        ((LdapConnector) this.connector).ensureConnected();
        // queueReceiver = new LDAPQueueReceiver(((LdapConnector)
        // this.connector),
        // endpoint, this);

    }

    @Override
    protected void doDisconnect() throws ConnectException
    {

    }

    @Override
    protected void doDispose()
    {

    }

    @Override
    protected void doStart() throws MuleException
    {

        ((LdapConnector) this.connector).ensureConnected();
        ((LdapConnector) this.connector)
                .addLDAPUnsolicitedNotificationListener(this);
        super.doStart();

    }

    @Override
    public void poll() throws Exception
    {
        final LDAPMessage msg = ((LdapConnector) this.connector).pollQueue();

        if (msg == null)
        {
            return;
        }

        final MessageAdapter adapter = connector.getMessageAdapter(msg);
        routeMessage(new DefaultMuleMessage(adapter, (Map) null), endpoint
                .isSynchronous());
    }

}
