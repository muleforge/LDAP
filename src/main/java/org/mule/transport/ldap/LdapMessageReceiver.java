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

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Map;

import org.mule.DefaultMuleMessage;
import org.mule.api.DefaultMuleException;
import org.mule.api.MuleException;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.lifecycle.CreateException;
import org.mule.api.service.Service;
import org.mule.api.transaction.Transaction;
import org.mule.api.transaction.TransactionException;
import org.mule.api.transport.Connector;
import org.mule.api.transport.MessageAdapter;
import org.mule.transport.AbstractMessageReceiver;
import org.mule.transport.AbstractPollingMessageReceiver;
import org.mule.transport.AbstractReceiverWorker;
import org.mule.transport.ConnectException;
import org.mule.transport.DefaultMessageAdapter;

import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPExtendedResponse;
import com.novell.ldap.LDAPMessage;
import com.novell.ldap.LDAPUnsolicitedNotificationListener;
import com.novell.ldap.events.LDAPEvent;
import com.novell.ldap.events.LDAPExceptionEvent;
import com.novell.ldap.events.PSearchEventListener;
import com.novell.ldap.events.SearchReferralEvent;
import com.novell.ldap.events.SearchResultEvent;

/**
 * <code>LdapMessageReceiver</code> TODO document
 */
public class LdapMessageReceiver extends AbstractPollingMessageReceiver
        implements LDAPUnsolicitedNotificationListener, PSearchEventListener
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
            getWorkManager().scheduleWork(new LdapWorker(msg, this));
        }
        catch (final Exception e)
        {
            handleException(e);
        }
        /*
         * try { final MessageAdapter adapter =
         * connector.getMessageAdapter(msg); routeMessage(new
         * DefaultMuleMessage(adapter, (Map) null), endpoint .isSynchronous()); }
         * catch (final MessagingException e) { throw new RuntimeException(e); }
         * catch (final MuleException e) { throw new RuntimeException(e); }
         */

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

        try
        {
            ((LdapConnector) this.connector).registerforEvent(this);
        }
        catch (final LDAPException e)
        {
            throw new DefaultMuleException(e);
        }

        super.doStart();

        logger.debug("started");

    }

    @Override
    public synchronized void poll() throws Exception
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

    public void searchReferalEvent(final SearchReferralEvent referalevent)
    {

        logger.debug("searchReferalEvent: " + referalevent);

        try
        {
            getWorkManager().scheduleWork(new LdapWorker(referalevent, this));
        }
        catch (final Exception e)
        {
            handleException(e);
        }
    }

    public void searchResultEvent(final SearchResultEvent event)
    {

        logger.debug("searchResultEvent: " + event);
        try
        {
            getWorkManager().scheduleWork(new LdapWorker(event, this));
        }
        catch (final Exception e)
        {
            handleException(e);
        }
    }

    public void ldapEventNotification(final LDAPEvent evt)
    {

        logger.debug("ldapEventNotification: " + evt);
        try
        {
            getWorkManager().scheduleWork(new LdapWorker(evt, this));
        }
        catch (final Exception e)
        {
            handleException(e);
        }

    }

    public void ldapExceptionNotification(final LDAPExceptionEvent ldapevt)
    {

        logger.debug("ldapExceptionNotification: " + ldapevt);
        try
        {
            getWorkManager().scheduleWork(new LdapWorker(ldapevt, this));
        }
        catch (final Exception e)
        {
            handleException(e);
        }

    }

    protected class LdapWorker extends AbstractReceiverWorker
    {

        public LdapWorker(final LDAPMessage message,
                final AbstractMessageReceiver receiver)
        {
            super(new ArrayList(1), receiver);
            messages.add(message);
        }

        public LdapWorker(final EventObject message,
                final AbstractMessageReceiver receiver)
        {
            super(new ArrayList(1), receiver);
            messages.add(new DefaultMessageAdapter(message));
        }

        public LdapWorker(final List messages,
                final AbstractMessageReceiver receiver, final OutputStream out)
        {
            super(messages, receiver, out);
            // TODO Auto-generated constructor stub
        }

        public LdapWorker(final List messages,
                final AbstractMessageReceiver receiver)
        {
            super(messages, receiver);
            // TODO Auto-generated constructor stub
        }

        @Override
        protected void bindTransaction(final Transaction tx)
                throws TransactionException
        {
            // no impl. because LDAP is not transactional

        }

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
            // ignore;throw new DefaultMuleException(e);
        }
    }

}
