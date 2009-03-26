/*
 * $Id: LDAPSearchResultToString.java 187 2008-10-23 13:12:25Z hsaly $
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.ldap.transformers;

import java.util.ArrayList;
import java.util.List;

import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractTransformer;

import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPSearchResults;

public class LDAPSearchResultsToList extends AbstractTransformer
{

    @Override
    protected Object doTransform(final Object src, final String encoding)
            throws TransformerException
    {

        if (src == null)
        {
            throw new TransformerException(this, new IllegalArgumentException(
                    "src must not be null"));
        }

        if (!(src instanceof LDAPSearchResults))
        {
            throw new TransformerException(this, new IllegalArgumentException(
                    "wrong type " + src.getClass()
                            + ", LDAPSearchResults expected"));
        }

        try
        {
            final LDAPSearchResults res = (LDAPSearchResults) src;
            final List < LDAPEntry > list = new ArrayList < LDAPEntry >();

            while (res.hasMore())
            {
                list.add(res.next());
            }

            return list;
        }
        catch (final LDAPException e)
        {
            throw new TransformerException(this, e);
        }

    }
}
