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

import java.io.UnsupportedEncodingException;

import org.mule.api.transformer.TransformerException;
import org.mule.config.i18n.MessageFactory;
import org.mule.transformer.AbstractTransformer;

public class HttpRequestToString extends AbstractTransformer
{

    public HttpRequestToString()
    {
        super();
        this.registerSourceType(String.class);
        this.registerSourceType(byte[].class);
    }

    @Override
    public Object doTransform(final Object src, final String encoding)
            throws TransformerException
    {

        logger.debug("Type: " + src.getClass().toString());
        logger.debug("Content: " + src.toString());

        String param;

        if (src instanceof byte[])
        {
            if (encoding != null)
            {
                try
                {
                    param = new String((byte[]) src, encoding);
                }
                catch (final UnsupportedEncodingException ex)
                {
                    param = new String((byte[]) src);
                }
            }
            else
            {
                param = new String((byte[]) src);
            }
        }
        else
        {
            param = src.toString();
        }

        final int equals = param.indexOf('=');
        if (equals > -1)
        {
            return param.substring(equals + 1);
        }
        else
        {
            throw new org.mule.api.transformer.TransformerException(
                    MessageFactory
                            .createStaticMessage("Failed to parse param string: "
                                    + param), this);
        }
    }
}
