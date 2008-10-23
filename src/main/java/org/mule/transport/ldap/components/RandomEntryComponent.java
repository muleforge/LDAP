/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.ldap.components;

import java.util.Random;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;

import com.novell.ldap.LDAPAddRequest;
import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPAttributeSet;
import com.novell.ldap.LDAPEntry;

public class RandomEntryComponent implements Callable
{

    private final Random rand = new Random(System.currentTimeMillis());

    public Object onCall(final MuleEventContext eventContext) throws Exception
    {

        final String cn = "hsaly-" + rand.nextInt(Integer.MAX_VALUE);
        final String sn = "sn-" + rand.nextInt(Integer.MAX_VALUE);
        final LDAPAttributeSet attr = new LDAPAttributeSet();
        attr.add(new LDAPAttribute("cn", cn));
        attr.add(new LDAPAttribute("sn", sn));
        attr.add(new LDAPAttribute("objectClass", "inetOrgPerson"));

        final LDAPEntry entry = new LDAPEntry("cn=" + cn + ",o=sevenseas", attr);

        return new LDAPAddRequest(entry, null);

    }

}
