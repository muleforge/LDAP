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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.mule.api.transformer.TransformerException;
import org.mule.providers.ldap.util.LDAPUtils;
import org.mule.transformer.AbstractTransformer;
import org.mule.util.IOUtils;

import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPMessage;
import com.novell.ldap.util.DSMLReader;
import com.novell.ldap.util.LDAPReader;
import com.novell.ldap.util.LDIFReader;

public class StringToLDAPMessage extends AbstractTransformer
{

    public static final String PROPERTY_LDAP_HOST = "format";

    private String format;

    // DSML(default), LDIF

    // @Override
    protected Object doTransform(Object src, String encoding)
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

        String msg = ((String) src);

        // TODO ldifcheck
        if ("ldif".equalsIgnoreCase(format))
        {
            List res = getOut(false, msg);
            if (res.size() == 1)
            {
                return res.get(0);
            }

            return res;
        }

        List res = getOut(true, msg);
        if (res.size() == 1)
        {
            return res.get(0);
        }

        return res;

    }

    private List getOut(boolean dsml, String msg) throws TransformerException
    {
        InputStream in = IOUtils.toInputStream(msg);

        try
        {
            LDAPReader reader = dsml ? (LDAPReader) new DSMLReader(in)
                    : (LDAPReader) new LDIFReader(in);

            LDAPMessage result = null;
            List res = new ArrayList();

            while ((result = reader.readMessage()) != null)
            {
                logger.debug(LDAPUtils.dumpLDAPMessage(result));
                res.add(result);
            }
            return res;

        }
        catch (IOException e)
        {
            throw new TransformerException(this, e);
        }
        catch (LDAPException e)
        {
            throw new TransformerException(this, e);
        }

    }

    public String getFormat()
    {
        return format;
    }

    public void setFormat(String format)
    {
        this.format = format;
    }
}
