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

import org.mule.api.MessagingException;
import org.mule.api.ThreadSafeAccess;
import org.mule.api.config.MuleProperties;
import org.mule.api.transport.MessageTypeNotSupportedException;

import com.novell.ldap.LDAPMessage;
import com.novell.ldap.LDAPSearchResults;

/**
 * <code>LdapMessageAdapter</code> TODO document
 */
public class LdapMessageAdapterOLD {/*extends MuleMessageAdapter
{

    private static final String CORRELATION_ID = MuleProperties.MULE_CORRELATION_ID_PROPERTY;

    static final long serialVersionUID = 1L;

    private LDAPMessage ldapMessage = null;
    private LDAPSearchResults searchResults = null;

    protected LdapMessageAdapterOLD(final LdapMessageAdapter template)
    {
        super(template);
        ldapMessage = template.ldapMessage;
        searchResults = template.searchResults;
    }

    public LdapMessageAdapterOLD(final Object message) throws MessagingException
    {
        super();

        if (message instanceof LDAPMessage)
        {
            this.ldapMessage = (LDAPMessage) message;

            final int value = this.ldapMessage.getType();
            setIntProperty("TYPE", value);
            setBooleanProperty("IS_ASYNC", true);

            final String id = this.ldapMessage.getTag();

            if (id != null)
            {
                setProperty(CORRELATION_ID, id);
            }

            setBooleanProperty("IS_REQUEST", this.ldapMessage.isRequest());

            final String tag = this.ldapMessage.getTag();
            if (tag != null)
            {
                setStringProperty("TAG", tag);
            }

        }
        else if (message instanceof LDAPSearchResults)
        {
            this.searchResults = (LDAPSearchResults) message;

            setBooleanProperty("IS_REQUEST", false);
            setBooleanProperty("IS_ASYNC", false);

        }
        else
        {
            throw new MessageTypeNotSupportedException(message, getClass());
        }

    }

    @Override
    public String getCorrelationId()
    {
        return (String) (getProperty(CORRELATION_ID));
    }

    public Object getPayload()
    {
        if (ldapMessage != null)
        {
            return ldapMessage;
        }
        else
        {
            return searchResults;
        }

    }

    public byte[] getPayloadAsBytes() throws Exception
    {
        if (ldapMessage != null)
        {
            return ldapMessage.toString().getBytes();
        }
        else
        {
            return searchResults.toString().getBytes();
        }

    }

    public String getPayloadAsString(final String encoding) throws Exception
    {
        if (ldapMessage != null)
        {
            return ldapMessage.toString();
        }
        else
        {
            return searchResults.toString();
        }

    }

    @Override
    public String getUniqueId()
    {
        if (ldapMessage != null)
        {
            return super.getUniqueId() + "-UNIQUE-ID-"
                    + ldapMessage.getMessageID();
        }
        else
        {
            return super.getUniqueId();
        }

    }

    @Override
    public ThreadSafeAccess newThreadCopy()
    {
        // TODO test this
        return new LdapMessageAdapter(this);

    }

    @Override
    public void setCorrelationId(final String correlationId)
    {
        setProperty(CORRELATION_ID, (correlationId));
    }
*/
}
