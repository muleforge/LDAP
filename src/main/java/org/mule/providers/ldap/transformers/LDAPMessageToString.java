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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractTransformer;

import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPMessage;
import com.novell.ldap.util.DSMLWriter;
import com.novell.ldap.util.LDAPWriter;
import com.novell.ldap.util.LDIFWriter;
public class LDAPMessageToString extends AbstractTransformer
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

        if (!(src instanceof LDAPMessage) && !(src instanceof List))
        {
            throw new TransformerException(this, new IllegalArgumentException(
                    "wrong type " + src.getClass()
                            + ", LDAPMessage or List excpeted"));
        }

        List msgs = null;

        if (src instanceof List)
        {
            msgs = (List) src;
        }
        else
        {
            msgs = new ArrayList();
            msgs.add(src);
        }

        if ("tostring".equalsIgnoreCase(format))
        {

            return msgs.toString();

        }
        else if ("ldif".equalsIgnoreCase(format))
        {

            // TODO ldifcheck
            // if(dsml || msg.getType() != LDAPMessage.SEARCH_RESULT);

            return getOut(false, msgs);
        }
        else
        {
            return getOut(true, msgs);
        }

    }

    private String getOut(boolean dsml, List msgList)
            throws TransformerException
    {
        try
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            LDAPWriter writer = dsml ? (LDAPWriter) new DSMLWriter(out)
                    : (LDAPWriter) new LDIFWriter(out);

            for (Iterator iterator = msgList.iterator(); iterator.hasNext();)
            {
                LDAPMessage ldapMsg = (LDAPMessage) iterator.next();

                try
                {
                    writer.writeMessage(ldapMsg);

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

            writer.finish();
            out.close();
            return out.toString();
        }
        catch (IOException e)
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
