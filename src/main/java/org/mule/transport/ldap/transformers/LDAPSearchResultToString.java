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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractTransformer;

import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPMessage;
import com.novell.ldap.LDAPSearchResults;
import com.novell.ldap.util.DSMLWriter;
import com.novell.ldap.util.LDAPWriter;
import com.novell.ldap.util.LDIFWriter;

public class LDAPSearchResultToString extends AbstractTransformer
{

    public static final String PROPERTY_LDAP_HOST = "format";

    private String format;

    // DSML(default), LDIF, toString

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

        if (!(src instanceof List) && !(src instanceof LDAPSearchResults))
        {
            throw new TransformerException(
                    this,
                    new IllegalArgumentException(
                            "wrong type "
                                    + src.getClass()
                                    + ", List<LDAPMessage> or LDAPSearchResults excpeted"));
        }

        // List msg = (List) src;
        // LDAPSearchResults msg = ((LDAPSearchResults) src);

        if ("tostring".equalsIgnoreCase(format))
        {
            return src.toString();

        }
        else if ("ldif".equalsIgnoreCase(format))
        {
            if (src instanceof List)
            {
                return getOut(false, (List) src);
            }
            else
            {
                return getOut(false, (LDAPSearchResults) src);
            }

        }
        else
        {

            if (src instanceof List)
            {
                return getOut(true, (List) src);
            }
            else
            {
                return getOut(true, (LDAPSearchResults) src);
            }
        }

    }

    private String getOut(final boolean dsml, final List list)
            throws TransformerException
    {
        final StringBuffer sb = new StringBuffer();
        for (final Iterator iterator = list.iterator(); iterator.hasNext();)
        {
            final LDAPMessage msg = (LDAPMessage) iterator.next();

            if (dsml
                    || ((msg.getType() != LDAPMessage.SEARCH_RESULT)
                            && (msg.getType() != LDAPMessage.DEL_RESPONSE)
                            && (msg.getType() != LDAPMessage.ADD_RESPONSE)
                            && (msg.getType() != LDAPMessage.MODIFY_RESPONSE)
                            && (msg.getType() != LDAPMessage.MODIFY_RDN_RESPONSE)
                            && (msg.getType() != LDAPMessage.BIND_RESPONSE)
                            && (msg.getType() != LDAPMessage.COMPARE_RESPONSE)
                            && (msg.getType() != LDAPMessage.EXTENDED_RESPONSE) && (msg
                            .getType() != LDAPMessage.INTERMEDIATE_RESPONSE)))
            {
                sb.append(getOut(dsml, msg));
            }
            else
            {
                sb
                        .append("# Messagetype "
                                + msg.getType()
                                + " not supported by LDIF specification. Use DSML instead.");
            }
        }

        return sb.toString();
    }

    private String getOut(final boolean dsml, final LDAPMessage msg)
            throws TransformerException
    {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        try
        {
            final LDAPWriter writer = dsml ? (LDAPWriter) new DSMLWriter(out)
                    : (LDAPWriter) new LDIFWriter(out);
            writer.writeMessage(msg);
            writer.finish();
            out.close();
            return out.toString();
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

    private String getOut(final boolean dsml, final LDAPSearchResults msg)
            throws TransformerException
    {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        try
        {
            final LDAPWriter writer = dsml ? (LDAPWriter) new DSMLWriter(out)
                    : (LDAPWriter) new LDIFWriter(out);

            while (msg.hasMore())
            {
                writer.writeEntry(msg.next());
            }

            writer.finish();
            out.close();
            return out.toString();
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
