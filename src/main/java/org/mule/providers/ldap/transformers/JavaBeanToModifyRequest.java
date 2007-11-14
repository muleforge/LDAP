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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.mule.providers.ldap.LdapConnector;
import org.mule.transformers.AbstractTransformer;
import org.mule.umo.transformer.TransformerException;
import org.mule.util.StringUtils;

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

    protected Object doTransform(Object src, String encoding)
            throws TransformerException
    {

        logger.debug("UniqueField Value: '" + getUniqueField() + "'");

        if (src == null)
        {
            throw new TransformerException(this, new IllegalArgumentException(
                    "src must not be null"));
        }

        Object bean = src;

        try
        {
            Method[] fields = bean.getClass().getMethods();
            List mods = new ArrayList(50);
            Object uniqueFieldValue = null;
            for (int i = 0; i < fields.length; i++)
            {
                Method method = fields[i];
                String name = method.getName();

                // logger.debug("found method '"+name+"'");

                if (!name.startsWith("get") || name.equals("getDn")
                        || name.equals("getClass"))
                {
                    continue;
                }

                String attributeName = name.substring(3);
                attributeName = StringUtils.uncapitalize(attributeName);

                logger.debug("going to invoke method '" + name + "'");

                Object result = method.invoke(bean, null);

                if (!StringUtils.isEmpty(getUniqueField())
                        && getUniqueField().equals(attributeName))
                {
                    uniqueFieldValue = result;
                }

                logger.debug("result of type '"
                        + (result == null ? null : result.getClass())
                        + "' (attribute is '" + attributeName + "')");

                LDAPAttribute attr = new LDAPAttribute(attributeName,
                        result == null ? null : result.toString());
                LDAPModification modification = new LDAPModification(
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
                LdapConnector connector = (LdapConnector) endpoint
                        .getConnector();

                LDAPConnection lc = connector.getLdapConnection();
                LDAPSearchConstraints cons = new LDAPSearchConstraints();
                cons.setBatchSize(0);
                LDAPSearchResults res = lc.search(connector.getSearchBase(),
                        connector.getSearchScope(), "(" + getUniqueField()
                                + "=" + uniqueFieldValue + ")", null, false,
                        cons);

                if (res.hasMore() && res.getCount() > 0)
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

            LDAPModification[] modifications = (LDAPModification[]) mods
                    .toArray(new LDAPModification[mods.size()]);

            // TODO LDAPCOntrols
            LDAPModifyRequest request = new LDAPModifyRequest(dn,
                    modifications, null);

            return request;
        }
        catch (SecurityException e)
        {
            throw new TransformerException(this, e);
        }
        catch (IllegalArgumentException e)
        {
            throw new TransformerException(this, e);
        }
        catch (IllegalAccessException e)
        {
            throw new TransformerException(this, e);
        }
        catch (InvocationTargetException e)
        {
            throw new TransformerException(this, e);
        }
        catch (NoSuchMethodException e)
        {
            throw new TransformerException(this, e);
        }
        catch (LDAPException e)
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
