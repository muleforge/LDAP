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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractTransformer;
import org.mule.transport.ldap.LdapConnector;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPModification;
import com.novell.ldap.LDAPModifyRequest;
import com.novell.ldap.LDAPSearchConstraints;
import com.novell.ldap.LDAPSearchResults;

public class JavaBeanToModifyRequest extends AbstractTransformer
{

    private String uniqueField;

    @Override
    protected Object doTransform(final Object src, final String encoding)
            throws TransformerException
    {

        logger.debug("UniqueField Value: '" + getUniqueField() + "'");

        if (src == null)
        {
            throw new TransformerException(this, new IllegalArgumentException(
                    "src must not be null"));
        }

        final Object bean = src;

        try
        {
            final Method[] fields = bean.getClass().getMethods();
            final List mods = new ArrayList(50);
            Object uniqueFieldValue = null;
            for (int i = 0; i < fields.length; i++)
            {
                final Method method = fields[i];
                final String name = method.getName();

                // logger.debug("found method '"+name+"'");

                if (!name.startsWith("get") || name.equals("getDn")
                        || name.equals("getClass"))
                {
                    continue;
                }

                String attributeName = name.substring(3);
                attributeName = org.apache.commons.lang.StringUtils
                        .uncapitalize(attributeName);

                logger.debug("going to invoke method '" + name + "'");

                final Object result = method.invoke(bean, null);

                if (!org.apache.commons.lang.StringUtils
                        .isEmpty(getUniqueField())
                        && getUniqueField().equals(attributeName))
                {
                    uniqueFieldValue = result;
                }

                logger.debug("result of type '"
                        + (result == null ? null : result.getClass())
                        + "' (attribute is '" + attributeName + "')");

                final LDAPAttribute attr = new LDAPAttribute(attributeName,
                        result == null ? null : result.toString());
                final LDAPModification modification = new LDAPModification(
                        LDAPModification.REPLACE, attr);
                mods.add(modification);
            }

            String dn = null;
            if (uniqueFieldValue == null)
            {
                dn = (String) bean.getClass().getMethod("getDn", null).invoke(
                        bean, null);
            }
            else
            {
                final LdapConnector connector = (LdapConnector) endpoint
                        .getConnector();

                final LDAPConnection lc = connector.getLdapConnection();
                final LDAPSearchConstraints cons = new LDAPSearchConstraints();
                cons.setBatchSize(0);
                final LDAPSearchResults res = lc.search(connector
                        .getSearchBase(), connector.getSearchScope(), "("
                        + getUniqueField() + "=" + uniqueFieldValue + ")",
                        null, false, cons);

                if (res.hasMore() && (res.getCount() > 0))
                {
                    if (res.getCount() > 1)
                    {
                        logger
                                .warn("UniqueField value is not unique, first entry will be modified");

                    }

                    dn = res.next().getDN();
                }
                else if (res.getCount() < 1)
                {
                    logger.error("UniqueField value didn't match any entry");
                }
            }

            logger.debug("dn is '" + dn + "'");

            final LDAPModification[] modifications = (LDAPModification[]) mods
                    .toArray(new LDAPModification[mods.size()]);

            // TODO LDAPCOntrols
            final LDAPModifyRequest request = new LDAPModifyRequest(dn,
                    modifications, null);

            return request;
        }
        catch (final SecurityException e)
        {
            throw new TransformerException(this, e);
        }
        catch (final IllegalArgumentException e)
        {
            throw new TransformerException(this, e);
        }
        catch (final IllegalAccessException e)
        {
            throw new TransformerException(this, e);
        }
        catch (final InvocationTargetException e)
        {
            throw new TransformerException(this, e);
        }
        catch (final NoSuchMethodException e)
        {
            throw new TransformerException(this, e);
        }
        catch (final LDAPException e)
        {
            throw new TransformerException(this, e);
        }

    }

    public String getUniqueField()
    {
        return uniqueField;
    }

    public void setUniqueField(String uniqueField)
    {
        if (uniqueField != null)
        {
            uniqueField = uniqueField.trim();
        }

        this.uniqueField = uniqueField;
    }

}
