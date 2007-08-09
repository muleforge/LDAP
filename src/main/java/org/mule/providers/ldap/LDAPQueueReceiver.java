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

import org.mule.impl.MuleMessage;
import org.mule.providers.ldap.util.LDAPUtils;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOImmutableEndpoint;
import org.mule.umo.provider.UMOMessageAdapter;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPAttributeSet;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPMessage;
import com.novell.ldap.LDAPMessageQueue;
import com.novell.ldap.LDAPResponse;
import com.novell.ldap.LDAPSearchResult;
import com.novell.ldap.LDAPSearchResultReference;

class LDAPQueueReceiver implements javax.resource.spi.work.Work
{

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

    public synchronized UMOMessage pollOnce()
    {
        return pollOnce(true);
    }

    public void release()
    {
        // TODO Auto-generated method stub

    }

    public synchronized void run()
    {

        pollOnce(false);
    }

    protected synchronized UMOMessage pollOnce(boolean synchronus)
    {

        try
        {

            LDAPMessageQueue queue = connector.getMessageQueue();
            LDAPMessage message = null;

            if (queue != null && (message = queue.getResponse()) != null)
            {

                // the message is a search result reference

                if (message instanceof LDAPSearchResultReference)
                {

                    String urls[] =

                    ((LDAPSearchResultReference) message).getReferrals();

                    System.out.println("Search result references:");

                    for (int i = 0; i < urls.length; i++)
                    {

                        System.out.println(urls[i]);
                    }

                }
                else if (message instanceof LDAPSearchResult)
                {

                    LDAPEntry entry = ((LDAPSearchResult) message).getEntry();

                    System.out.println("\n" + entry.getDN());

                    System.out.println("\tAttributes: ");

                    LDAPAttributeSet attributeSet = entry.getAttributeSet();

                    Iterator allAttributes = attributeSet.iterator();

                    while (allAttributes.hasNext())
                    {

                        LDAPAttribute attribute =

                        (LDAPAttribute) allAttributes.next();

                        String attributeName = attribute.getName();

                        System.out.println("\t\t" + attributeName);

                        Enumeration allValues = attribute.getStringValues();

                        if (allValues != null)
                        {

                            while (allValues.hasMoreElements())
                            {

                                String Value =

                                (String) allValues.nextElement();

                                System.out.println("\t\t\t" + Value);

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

                        System.out
                                .println(">>>> Asynchronous search succeeded.");

                    }

                    // the reutrn code is referral exception

                    else if (status == LDAPException.REFERRAL)
                    {

                        String urls[] = ((LDAPResponse) message).getReferrals();

                        System.out.println("Referrals:");

                        for (int i = 0; i < urls.length; i++)
                        {

                            System.out.println(urls[i]);
                        }

                    }

                    else
                    {

                        System.out.println("Asynchronous search failed.");

                        /*
                         * throw new LDAPException(response.getErrorMessage(),
                         * 
                         * status,
                         * 
                         * response.getMatchedDN());
                         */

                        System.err.println("errmsg: "
                                + response.getErrorMessage());
                        System.err.println("status" + status);
                        System.err.println("res code: "
                                + response.getResultCode());
                        System.err.println("dn: " + response.getMatchedDN());
                        System.err.println("msg was: " + message);

                    }

                }

                System.out.println("   >>>>  message id "
                        + message.getMessageID());
                System.out.println("   >>>>  message tag " + message.getTag());
                System.out.println("   >>>>  message "
                        + LDAPUtils.dumpLDAPMessage(message));

                UMOMessageAdapter adapter = connector
                        .getMessageAdapter(message);

                if (synchronus)
                {
                    return new MuleMessage(adapter);
                }

                listener.onMessage(new MuleMessage(adapter), endpoint);

            }// endif

        }
        catch (Exception e)
        {
            // e.printStackTrace();
            connector.handleException(e);
        }

        return null;

    }

}
