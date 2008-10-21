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

import java.util.Enumeration;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.DefaultMuleMessage;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.api.transport.MessageAdapter;
import org.mule.transport.ldap.util.LDAPUtils;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPAttributeSet;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPMessage;
import com.novell.ldap.LDAPResponse;
import com.novell.ldap.LDAPSearchResult;
import com.novell.ldap.LDAPSearchResultReference;

class LDAPQueueReceiver implements javax.resource.spi.work.Work
{

    /**
     * logger used by this class
     */
    private final Log logger = LogFactory.getLog(getClass());

    private LdapConnector connector;

    private ImmutableEndpoint endpoint;
    private LDAPQueueListener listener;

    public LDAPQueueReceiver(LdapConnector connector,
            ImmutableEndpoint endpoint, LDAPQueueListener listener)
    {
        super();
        this.connector = connector;
        this.endpoint = endpoint;
        this.listener = listener;
    }

    public MuleMessage pollOnce()
    {
        return pollOnce(true);
    }

    public void release()
    {

    }

    public void run()
    {
        pollOnce(false);
    }

    // make not synchronized
    protected MuleMessage pollOnce(boolean synchronus)
    {

        try
        {
            // logger.debug("entering pollOnce()");

            // may block
            LDAPMessage message = connector.pollQueue();

            if (message != null)
            {

                // the message is a search result reference

                if (message instanceof LDAPSearchResultReference)
                {
                    logger.debug("LDAPSearchResultReference:");

                    String urls[] =

                    ((LDAPSearchResultReference) message).getReferrals();

                    logger.debug("Search result references:");

                    for (int i = 0; i < urls.length; i++)
                    {

                        logger.debug(urls[i]);
                    }

                }
                else if (message instanceof LDAPSearchResult)
                {

                    logger.debug("LDAPSearchResult:");

                    LDAPEntry entry = ((LDAPSearchResult) message).getEntry();

                    logger.debug("DN" + entry.getDN());
                    logger.debug("entry" + entry);
                    logger.debug("\tAttributes: ");

                    LDAPAttributeSet attributeSet = entry.getAttributeSet();

                    Iterator allAttributes = attributeSet.iterator();

                    while (allAttributes.hasNext())
                    {

                        LDAPAttribute attribute =

                        (LDAPAttribute) allAttributes.next();

                        String attributeName = attribute.getName();

                        logger.debug("\t\t" + attributeName);

                        Enumeration allValues = attribute.getStringValues();

                        if (allValues != null)
                        {

                            while (allValues.hasMoreElements())
                            {

                                String value =

                                (String) allValues.nextElement();

                                logger.debug("\t\t\t" + value);

                            }

                        }

                    }

                }

                // the message is a search response

                else
                {

                    LDAPResponse response = (LDAPResponse) message;

                    logger.debug("LDAPResponse:");

                    int status = response.getResultCode();

                    // the return code is LDAP success

                    if (status == LDAPException.SUCCESS)
                    {

                        logger.debug(">>>> Asynchronous search succeeded.");

                    }

                    // the reutrn code is referral exception

                    else if (status == LDAPException.REFERRAL)
                    {

                        String urls[] = ((LDAPResponse) message).getReferrals();

                        logger.debug("Referrals:");

                        for (int i = 0; i < urls.length; i++)
                        {

                            logger.debug(urls[i]);
                        }

                    }

                    else
                    {

                        logger.debug("Asynchronous search failed.");

                        /*
                         * throw new LDAPException(response.getErrorMessage(),
                         * 
                         * status,
                         * 
                         * response.getMatchedDN());
                         */

                        logger.debug("errmsg: " + response.getErrorMessage());
                        logger.debug("status" + status);
                        logger.debug("res code: " + response.getResultCode());
                        logger.debug("dn: " + response.getMatchedDN());
                        logger.debug("msg was: " + message);

                    }

                }

                logger.debug("   >>>>  message id " + message.getMessageID());
                logger.debug("   >>>>  message tag " + message.getTag());
                logger.debug("   >>>>  message "
                        + LDAPUtils.dumpLDAPMessage(message));

                MessageAdapter adapter = connector.getMessageAdapter(message);

                if (synchronus)
                {
                    return new DefaultMuleMessage(adapter);
                }

                listener.onMessage(new DefaultMuleMessage(adapter), endpoint);

            } // endif

        }
        catch (MuleException e)
        {
            // e.printStackTrace();
            connector.handleException(e);
        }
        catch (LDAPException e)
        {
            // e.printStackTrace();
            connector.handleException(e);
        }

        return null;

    }

}
