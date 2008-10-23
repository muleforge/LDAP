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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractTransformer;
import org.mule.transport.ldap.util.LDAPUtils;

import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPMessage;
import com.novell.ldap.util.DSMLReader;
import com.novell.ldap.util.LDAPReader;
import com.novell.ldap.util.LDIFReader;

public class StringToLDAPMessage extends AbstractTransformer
{

    // public static final String PROPERTY_LDAP_HOST = "format";

    private String format;

    // DSML(default), LDIF

    @Override
    protected Object doTransform(final Object src, final String encoding)
            throws TransformerException
    {

        if (src == null)
        {
            throw new TransformerException(this, new IllegalArgumentException(
                    "src must not be null"));
        }

        if (!(src instanceof String))
        {
            throw new TransformerException(this, new IllegalArgumentException(
                    "wrong type " + src.getClass()
                            + ", java.lang.String excpeted"));
        }

        final String msg = ((String) src);

        // TODO ldifcheck
        if ("ldif".equalsIgnoreCase(format))
        {
            final List res = getOut(false, msg);
            if (res.size() == 1)
            {
                return res.get(0);
            }

            return res;
        }

        final List res = getOut(true, msg);
        if (res.size() == 1)
        {
            return res.get(0);
        }

        return res;

    }

    private List getOut(final boolean dsml, final String msg)
            throws TransformerException
    {
        final InputStream in = org.apache.commons.io.IOUtils.toInputStream(msg);

        try
        {
            final LDAPReader reader = dsml ? (LDAPReader) new DSMLReader(in)
                    : (LDAPReader) new LDIFReader(in);

            LDAPMessage result = null;
            final List res = new ArrayList();

            while ((result = reader.readMessage()) != null)
            {
                logger.debug(LDAPUtils.dumpLDAPMessage(result));
                res.add(result);
            }
            return res;

        }
        catch (final IOException e)
        {
            throw new TransformerException(this, e);
        }
        catch (final LDAPException e)
        {
            throw new TransformerException(this, e);
        }

    }

    public String getFormat()
    {
        return format;
    }

    public void setFormat(final String format)
    {
        this.format = format;
    }
}
