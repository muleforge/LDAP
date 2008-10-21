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

import org.mule.DefaultMuleMessage;
import org.mule.api.MessagingException;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.lifecycle.CreateException;
import org.mule.api.service.Service;
import org.mule.api.transport.Connector;
import org.mule.api.transport.MessageAdapter;
import org.mule.transport.ConnectException;

import com.novell.ldap.LDAPExtendedResponse;
import com.novell.ldap.LDAPUnsolicitedNotificationListener;

/**
 * <code>LdapMessageReceiver</code> TODO document
 */
public class LdapMessageReceiver extends
        org.mule.transport.AbstractPollingMessageReceiver implements
        LDAPUnsolicitedNotificationListener, LDAPQueueListener
{

    /*
     * For general guidelines on writing transports see
     * http://mule.mulesource.org/display/MULE/Writing+Transports
     */

    private LDAPQueueReceiver queueReceiver;

    public LdapMessageReceiver(Connector connector, Service service,
            InboundEndpoint endpoint) throws CreateException
    {

        super(connector, service, endpoint);

    }

    public void messageReceived(LDAPExtendedResponse msg)
    {
        logger.debug("Received notification of unsolicited notification");

        // Print the OID

        logger.debug("The OID in the notification was ==>" + msg.getID());

        // byte[] data = msg.getValue();

        try
        {
            MessageAdapter adapter = connector.getMessageAdapter(msg);
            routeMessage(new DefaultMuleMessage(adapter), endpoint
                    .isSynchronous());
        }
        catch (MessagingException e)
        {
            throw new RuntimeException(e);
        }
        catch (MuleException e)
        {

            throw new RuntimeException(e);
        }

    }

    public void onMessage(MuleMessage message, ImmutableEndpoint endpoint)
            throws MuleException
    {
        routeMessage(message, endpoint.isSynchronous());

    }

    public void poll() throws Exception
    {

        LdapConnector connector = (LdapConnector) this.connector;

        try
        {
            connector.ensureConnected();
        }
        catch (org.mule.transport.ConnectException e)
        {

            handleException(e);
        }
        getWorkManager().doWork(queueReceiver);
        // getWorkManager().scheduleWork(queueReceiver);

    }

    protected void doConnect() throws ConnectException
    {
        ((LdapConnector) this.connector).ensureConnected();
        queueReceiver = new LDAPQueueReceiver(((LdapConnector) this.connector),
                endpoint, this);

    }

    protected void doDisconnect() throws ConnectException
    {

    }

    protected void doDispose()
    {

    }

    // @Override
    protected void doStart() throws MuleException
    {

        ((LdapConnector) this.connector).ensureConnected();

        super.doStart();

        ((LdapConnector) connector).addLDAPListener(this);

    }

}
