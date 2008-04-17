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
    protected Object doTransform(Object src, String encoding)
            throws TransformerException
    {

        if (src == null)
        {
            throw new TransformerException(this, new IllegalArgumentException(
                    "src must not be null"));
        }

        if (!(src instanceof List) && !(src instanceof LDAPSearchResults))
        {
            throw new TransformerException(this, new IllegalArgumentException(
                    "wrong type " + src.getClass()
                            + ", LDAPMessage or LDAPSearchResults excpeted"));
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

    private String getOut(boolean dsml, List list) throws TransformerException
    {
        StringBuffer sb = new StringBuffer();
        for (Iterator iterator = list.iterator(); iterator.hasNext();)
        {
            LDAPMessage msg = (LDAPMessage) iterator.next();

            if (dsml
                    || (msg.getType() != LDAPMessage.SEARCH_RESULT
                            && msg.getType() != LDAPMessage.DEL_RESPONSE
                            && msg.getType() != LDAPMessage.ADD_RESPONSE
                            && msg.getType() != LDAPMessage.MODIFY_RESPONSE
                            && msg.getType() != LDAPMessage.MODIFY_RDN_RESPONSE
                            && msg.getType() != LDAPMessage.BIND_RESPONSE
                            && msg.getType() != LDAPMessage.COMPARE_RESPONSE
                            && msg.getType() != LDAPMessage.EXTENDED_RESPONSE && msg
                            .getType() != LDAPMessage.INTERMEDIATE_RESPONSE))
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

    private String getOut(boolean dsml, LDAPMessage msg)
            throws TransformerException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try
        {
            LDAPWriter writer = dsml ? (LDAPWriter) new DSMLWriter(out)
                    : (LDAPWriter) new LDIFWriter(out);
            writer.writeMessage(msg);
            writer.finish();
            out.close();
            return out.toString();
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

    private String getOut(boolean dsml, LDAPSearchResults msg)
            throws TransformerException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try
        {
            LDAPWriter writer = dsml ? (LDAPWriter) new DSMLWriter(out)
                    : (LDAPWriter) new LDIFWriter(out);

            while (msg.hasMore())
            {
                writer.writeEntry(msg.next());
            }

            writer.finish();
            out.close();
            return out.toString();
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
