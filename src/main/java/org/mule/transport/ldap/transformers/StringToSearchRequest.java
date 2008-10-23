/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.ldap.transformers;

import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractTransformer;
import org.mule.transport.ldap.LdapConnector;
import org.mule.transport.ldap.util.LDAPUtils;

import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPSearchRequest;

public class StringToSearchRequest extends AbstractTransformer
{

    // @Override
    @Override
    protected Object doTransform(final Object src, final String encoding)
            throws TransformerException
    {

        if (src == null)
        {
            throw new TransformerException(this, new IllegalArgumentException(
                    "src must not be null"));
        }

        final LdapConnector ldapConnector = (LdapConnector) this.endpoint
                .getConnector();

        try
        {

            final LDAPSearchRequest request = LDAPUtils.createSearchRequest(
                    ldapConnector, src.toString());

            return request;
        }
        catch (final LDAPException e)
        {
            throw new TransformerException(this, e);
        }

    }
}
