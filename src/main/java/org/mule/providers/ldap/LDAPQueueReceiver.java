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

import java.util.Enumeration;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.impl.MuleMessage;
import org.mule.providers.ldap.util.LDAPUtils;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOImmutableEndpoint;
import org.mule.umo.provider.UMOMessageAdapter;

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

    private UMOImmutableEndpoint endpoint;
    private LDAPQueueListener listener;

    public LDAPQueueReceiver(LdapConnector connector,
            UMOImmutableEndpoint endpoint, LDAPQueueListener listener)
    {
        super();
        this.connector = connector;
        this.endpoint = endpoint;
        this.listener = listener;
    }

    public UMOMessage pollOnce()
    {
        return pollOnce(true);
    }

    /*
     * public UMOMessage pollOnce(long timeout) {
     * 
     * final class InnerThread implements Runnable { private UMOMessage msg =
     * null;
     * 
     * public void run() { logger.debug("awaiting msg"); msg = pollOnce(true);
     * logger.debug("got msg"); }
     * 
     * public UMOMessage getMsg() { return msg; }
     *  }
     * 
     * InnerThread inner = new InnerThread();
     * 
     * Thread t = new Thread(inner);
     * 
     * t.start(); try { t.join(timeout); } catch (InterruptedException e) {
     * e.printStackTrace(); }
     * 
     * logger.debug("return msg. Null? " + (inner.getMsg() == null));
     * 
     * return inner.getMsg();
     *  }
     */

    public void release()
    {

    }

    public void run()
    {
        pollOnce(false);
    }

    // make not synchronized
    protected UMOMessage pollOnce(boolean synchronus)
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

                    LDAPEntry entry = ((LDAPSearchResult) message).getEntry();

                    logger.debug("\n" + entry.getDN());

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

                UMOMessageAdapter adapter = connector
                        .getMessageAdapter(message);

                if (synchronus)
                {
                    return new MuleMessage(adapter);
                }

                listener.onMessage(new MuleMessage(adapter), endpoint);

            } // endif

        }
        catch (UMOException e)
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
