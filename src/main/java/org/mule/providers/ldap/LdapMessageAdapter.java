/*
 * $Id: LdapMessageAdapter.java,v 1.2 2007/08/01 21:33:18 hsaly Exp $
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.ldap;

// import org.mule.impl.ThreadSafeAccess;
//import org.mule.impl.ThreadSafeAccess;
import org.mule.providers.AbstractMessageAdapter;
import org.mule.umo.MessagingException;
import org.mule.umo.provider.MessageTypeNotSupportedException;

import com.novell.ldap.LDAPMessage;
import com.novell.ldap.LDAPSearchResults;

/**
 * <code>LdapMessageAdapter</code> TODO document
 */
public class LdapMessageAdapter extends AbstractMessageAdapter
{

    private static final String CORRELATION_ID = "CORRELATION_ID";

    static final long serialVersionUID = 1L;

    private LDAPMessage ldapMessage = null;
    private LDAPSearchResults searchResults = null;

    public LdapMessageAdapter(Object message) throws MessagingException
    {
        super();

        if (message instanceof LDAPMessage)
        {
            this.ldapMessage = (LDAPMessage) message;

            int value = this.ldapMessage.getType();
            setIntProperty("TYPE", value);
            setBooleanProperty("IS_ASYNC", true);

            String id = this.ldapMessage.getTag();

            if (id != null)
                setProperty(CORRELATION_ID, id);

            setBooleanProperty("IS_REQUEST", this.ldapMessage.isRequest());

            String tag = this.ldapMessage.getTag();
            if (tag != null)
            {
                setStringProperty("TAG", tag);
            }

        } else if (message instanceof LDAPSearchResults)
        {
            this.searchResults = (LDAPSearchResults) message;

            setBooleanProperty("IS_REQUEST", false);
            setBooleanProperty("IS_ASYNC", false);

        } else
        {
            throw new MessageTypeNotSupportedException(message, getClass());
        }

    }

    // @Override
    public String getCorrelationId()
    {
        // System.out.println("getcor"
        // +String.valueOf(getProperty(CORRELATION_ID)));
        return (String) (getProperty(CORRELATION_ID));
    }

    public Object getPayload()
    {
        if (ldapMessage != null)
        {
            return ldapMessage;
        } else
        {
            return searchResults;
        }

    }

    public byte[] getPayloadAsBytes() throws Exception
    {
        if (ldapMessage != null)
        {
            return ldapMessage.toString().getBytes();
        } else
        {
            return searchResults.toString().getBytes();
        }

    }

    public String getPayloadAsString(String encoding) throws Exception
    {
        if (ldapMessage != null)
        {
            return ldapMessage.toString();
        } else
        {
            return searchResults.toString();
        }

    }

    // @Override
    public String getUniqueId()
    {
        // TODO Auto-generated method stub
        if (ldapMessage != null)
        {
            return super.getUniqueId() + "-ID-" + ldapMessage.getMessageID();
        } else
        {
            return super.getUniqueId();
        }

    }

    // TODO what is it?
    /*public ThreadSafeAccess newThreadCopy()
    {

        return null;
    }*/

    // @Override
    public void setCorrelationId(String correlationId)
    {
        // System.out.println("setcor "+ Integer.valueOf(correlationId));
        setProperty(CORRELATION_ID, (correlationId));
    }

    // @Override
    public String toString()
    {
        // TODO Auto-generated method stub
        return super.toString();
    }

}
