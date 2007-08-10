/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.ldap.transformers;

import org.mule.transformers.AbstractTransformer;
import org.mule.umo.transformer.TransformerException;
import org.mule.util.StringUtils;

import com.novell.ldap.LDAPDeleteRequest;
import com.novell.ldap.LDAPException;

public class StringToDeleteRequest extends AbstractTransformer
{

    // @Override
    protected Object doTransform(Object src, String encoding)
            throws TransformerException
    {

        if (src == null)
        {
            throw new TransformerException(this, new IllegalArgumentException(
                    "src must not be null"));
        }

        String content = src.toString();

        if (StringUtils.isEmpty(content))
        {
            throw new TransformerException(this, new IllegalArgumentException(
                    "srctoString() must not be null or empty"));
        }

        try
        {

            // TODO LDAPCOntrols
            LDAPDeleteRequest request = new LDAPDeleteRequest(content, null);

            // logger.debug("tranf: id: " + request.getMessageID());

            return request;
        }
        catch (LDAPException e)
        {
            throw new TransformerException(this, e);
        }

    }
}
