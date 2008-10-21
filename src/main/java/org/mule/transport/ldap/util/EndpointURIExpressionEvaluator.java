/*
 * $Id: LdapConnector.java 172 2008-10-17 11:12:59Z hsaly $
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.ldap.util;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.MuleServer;
import org.mule.api.endpoint.EndpointURI;
import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.config.i18n.CoreMessages;
import org.mule.endpoint.AbstractEndpointBuilder;
import org.mule.util.StringUtils;
import org.mule.util.expression.ExpressionEvaluator;

public class EndpointURIExpressionEvaluator implements ExpressionEvaluator
{
    public static final String NAME = "endpointuri";

    /**
     * logger used by this class
     */
    protected final Log logger = LogFactory.getLog(getClass());

    public Object evaluate(String expression, Object message)
    {
        int i = expression.indexOf(".");
        String endpointName;
        String property;
        if (i > 0)
        {
            endpointName = expression.substring(0, i);
            property = expression.substring(i + 1);
        }
        else
        {
            throw new IllegalArgumentException(CoreMessages
                    .expressionMalformed(expression, getName()).getMessage());
        }

        EndpointURI uri = null;

        // looking for endpoint in the registry, if not look for an
        // EndpointBuilder
        Object tmp = MuleServer.getMuleContext().getRegistry().lookupObject(
                endpointName);

        if (tmp != null && tmp instanceof ImmutableEndpoint)
        {
            ImmutableEndpoint ep = (ImmutableEndpoint) tmp;
            uri = ep.getEndpointURI();
        }
        else
        {
            logger.info("There is no endpoint registered with name: "
                    + endpointName + " Will look for an global one ...");

            AbstractEndpointBuilder eb = (AbstractEndpointBuilder) MuleServer
                    .getMuleContext().getRegistry().lookupEndpointBuilder(
                            endpointName);

            if (eb != null)
            {
                uri = eb.getEndpointBuilder().getEndpoint();
            }
            else
            {
                logger
                        .warn("There is no endpointbuilder registered with name: "
                                + endpointName);
            }

        }

        if (uri != null)
        {

            // ${endpointuri:testendpoint.params:xxx}
            if (property.toLowerCase().startsWith("params:"))
            {
                String[] sa = property.split(":");

                return uri.getParams().getProperty(sa[1]);
            }

            // ${endpointuri:testendpoint.params:xxx}
            if (property.toLowerCase().startsWith("userparams:"))
            {
                String[] sa = property.split(":");

                return uri.getUserParams().getProperty(sa[1]);
            }

            // resovles dynamically to getXXX Method

            try
            {
                return uri.getClass().getMethod(
                        "get" + StringUtils.capitalize(property.toLowerCase()),
                        new Class[0]).invoke(uri, (Object[]) null);
            }
            catch (IllegalArgumentException e)
            {
                logger.error(e.toString(), e);
            }
            catch (SecurityException e)
            {
                logger.error(e.toString(), e);
            }
            catch (IllegalAccessException e)
            {
                logger.error(e.toString(), e);
            }
            catch (InvocationTargetException e)
            {
                logger.error(e.toString(), e);
            }
            catch (NoSuchMethodException e)
            {
                logger.error(e.toString(), e);
            }

            throw new IllegalArgumentException(CoreMessages
                    .expressionInvalidForProperty(property, expression)
                    .getMessage());

        }
        else
        {
            logger.warn("There is no endpoint registered with name: "
                    + endpointName);
            return null;
        }
    }

    /** {@inheritDoc} */
    public String getName()
    {
        return NAME;
    }

    /** {@inheritDoc} */
    public void setName(String name)
    {
        throw new UnsupportedOperationException("setName");
    }
}
