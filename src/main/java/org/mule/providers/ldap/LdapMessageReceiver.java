/*
 * $Id: LdapMessageReceiver.java,v 1.1 2007/07/30 22:34:33 hsaly Exp $
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.ldap;

import org.mule.impl.MuleMessage;
import org.mule.providers.AbstractPollingMessageReceiver;
import org.mule.providers.ConnectException;
import org.mule.umo.MessagingException;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.endpoint.UMOImmutableEndpoint;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.provider.UMOConnector;
import org.mule.umo.provider.UMOMessageAdapter;

import com.novell.ldap.LDAPExtendedResponse;
import com.novell.ldap.LDAPUnsolicitedNotificationListener;

/**
 * <code>LdapMessageReceiver</code> TODO document
 */
public class LdapMessageReceiver extends AbstractPollingMessageReceiver
        implements LDAPUnsolicitedNotificationListener, LDAPQueueListener
{

    /*
     * For general guidelines on writing transports see
     * http://mule.mulesource.org/display/MULE/Writing+Transports
     */

    protected LDAPQueueReceiver queueReceiver;

    public LdapMessageReceiver(UMOConnector connector, UMOComponent component,
            UMOEndpoint endpoint, long pollingFrequency)
            throws InitialisationException
    {
        // TODO
        // ,pollingFrequency
        super(connector, component, endpoint);

    }

    public void messageReceived(LDAPExtendedResponse msg)
    {
        logger.debug("Received notification of unsolicited notification");

        // Print the OID

        logger.debug("The OID in the notification was ==>" + msg.getID());

        byte[] data = msg.getValue();

        if (data == null)

            logger.debug("The DATA in the notification was <null>");

        else

            logger.debug("The DATA in the notification was ==> " + data);

        try
        {
            UMOMessageAdapter adapter = connector.getMessageAdapter(msg);
            routeMessage(new MuleMessage(adapter), endpoint.isSynchronous());
        } catch (MessagingException e)
        {
            throw new RuntimeException(e);
        } catch (UMOException e)
        {
            // TODO Auto-generated catch block
            throw new RuntimeException(e);
        }

    }

    public void onMessage(UMOMessage message, UMOImmutableEndpoint endpoint)
            throws UMOException
    {
        routeMessage(message, endpoint.isSynchronous());

    }

    public void poll() throws Exception
    {

        LdapConnector connector = (LdapConnector) this.connector;

        try
        {
            connector.ensureConnected();
        } catch (ConnectException e)
        {
            // TODO Auto-generated catch block
            handleException(e);
        }

        getWorkManager().scheduleWork(queueReceiver);

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
    protected void doStart() throws UMOException
    {

        ((LdapConnector) this.connector).ensureConnected();
        // TODO Auto-generated method stub
        super.doStart();

        ((LdapConnector) connector).addLDAPListener(this);

    }

}
