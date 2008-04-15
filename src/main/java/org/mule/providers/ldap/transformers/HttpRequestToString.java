/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.ldap.transformers;

import java.io.UnsupportedEncodingException;

import org.mule.config.i18n.MessageFactory;
import org.mule.transformers.AbstractTransformer;
import org.mule.umo.transformer.TransformerException;

public class HttpRequestToString extends AbstractTransformer
{

    public HttpRequestToString()
    {
        super();
        this.registerSourceType(String.class);
        this.registerSourceType(byte[].class);
    }

    public Object doTransform(Object src, String encoding)
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
                catch (UnsupportedEncodingException ex)
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

        int equals = param.indexOf('=');
        if (equals > -1)
        {
            return param.substring(equals + 1);
        }
        else
        {
            throw new TransformerException(MessageFactory
                    .createStaticMessage("Failed to parse param string: "
                            + param), this);
        }
    }
}
