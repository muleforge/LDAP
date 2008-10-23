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

import com.novell.ldap.LDAPDeleteRequest;
import com.novell.ldap.LDAPException;

public class StringToDeleteRequest extends AbstractTransformer
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

        final String content = src.toString();

        if (org.apache.commons.lang.StringUtils.isEmpty(content))
        {
            throw new TransformerException(this, new IllegalArgumentException(
                    "srctoString() must not be null or empty"));
        }

        try
        {

            // TODO LDAPCOntrols
            final LDAPDeleteRequest request = new LDAPDeleteRequest(content,
                    null);

            // logger.debug("tranf: id: " + request.getMessageID());

            return request;
        }
        catch (final LDAPException e)
        {
            throw new TransformerException(this, e);
        }

    }
}
