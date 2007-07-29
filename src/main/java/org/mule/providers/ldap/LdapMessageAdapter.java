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

import java.util.List;

import org.mule.providers.AbstractMessageAdapter;
import org.mule.umo.MessagingException;
import org.mule.umo.provider.MessageTypeNotSupportedException;

import com.novell.ldap.LDAPEntry;

/**
 * <code>LdapMessageAdapter</code> TODO document
 */
public class LdapMessageAdapter extends AbstractMessageAdapter
{
	
	private List<LDAPEntry> ldapMessage = null;

	static final long serialVersionUID = 1L;

	public LdapMessageAdapter(Object message) throws MessagingException
    {
        super();
		
		
		if (message instanceof List) {
            this.ldapMessage = (List<LDAPEntry>) message;
        } else {
            throw new MessageTypeNotSupportedException(message, getClass());
        }
		
        
    }

    public String getPayloadAsString(String encoding) throws Exception
    {
        
        return ldapMessage.toString();
    }

    public byte[] getPayloadAsBytes() throws Exception
    {
        return ldapMessage.toString().getBytes();
    }

    public Object getPayload()
    {
        return ldapMessage;
    }

}
