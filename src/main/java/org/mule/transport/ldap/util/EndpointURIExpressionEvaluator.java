/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.ldap.util;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.MuleServer;
import org.mule.RegistryContext;
import org.mule.api.endpoint.EndpointURI;
import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.config.i18n.CoreMessages;
import org.mule.endpoint.AbstractEndpointBuilder;
import org.mule.util.expression.ExpressionEvaluator;

public class EndpointURIExpressionEvaluator implements ExpressionEvaluator
{
    public static final String NAME = "endpointuri";

    /**
     * logger used by this class
     */
    protected final Log logger = LogFactory.getLog(getClass());

    public Object evaluate(final String expression, final Object message)
    {
        logger.debug(expression + " on " + message);

        final int i = expression.indexOf(".");
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

        Object tmp = null;

        final Collection < ImmutableEndpoint > endpoints = MuleServer
                .getMuleContext().getRegistry().getEndpoints();
        for (final Iterator iterator = endpoints.iterator(); iterator.hasNext();)
        {

            final ImmutableEndpoint ep = (ImmutableEndpoint) iterator.next();

            logger.debug("found endpoint: " + ep.getName());

            if (ep.getName().equals(endpointName))
            {
                tmp = ep;
                break;
            }

        }

        if (tmp == null)
        {
            tmp = MuleServer.getMuleContext().getRegistry().lookupObject(
                    endpointName);
        }

        logger.debug(tmp);

        if (tmp != null)
        {
            logger.debug(tmp.getClass());
        }

        if ((tmp != null) && (tmp instanceof ImmutableEndpoint))
        {
            final ImmutableEndpoint ep = (ImmutableEndpoint) tmp;
            uri = ep.getEndpointURI();
        }
        else if (((tmp = RegistryContext.getRegistry().lookupObject(
                endpointName)) != null)
                && (tmp instanceof ImmutableEndpoint))
        {
            final ImmutableEndpoint ep = (ImmutableEndpoint) tmp;
            uri = ep.getEndpointURI();
        }
        else
        {
            logger.warn("There is no endpoint registered with name: "
                    + endpointName + " Will look for an global one ...");

            final AbstractEndpointBuilder eb = (AbstractEndpointBuilder) MuleServer
                    .getMuleContext().getRegistry().lookupEndpointBuilder(
                            endpointName);

            final AbstractEndpointBuilder eb2 = (AbstractEndpointBuilder) RegistryContext
                    .getRegistry().lookupEndpointBuilder(endpointName);

            if (eb != null)
            {
                uri = eb.getEndpointBuilder().getEndpoint();
            }
            else if (eb2 != null)
            {
                uri = eb2.getEndpointBuilder().getEndpoint();
            }
            else
            {
                logger
                        .error("There is no endpointbuilder or endpoint registered with name: "
                                + endpointName);
            }

        }

        if (uri != null)
        {

            // ${endpointuri:testendpoint.params:xxx}
            if (property.toLowerCase().startsWith("params:"))
            {
                final String[] sa = property.split(":");

                return uri.getParams().getProperty(sa[1]);
            }

            // ${endpointuri:testendpoint.params:xxx}
            if (property.toLowerCase().startsWith("userparams:"))
            {
                final String[] sa = property.split(":");

                return uri.getUserParams().getProperty(sa[1]);
            }

            // resovles dynamically to getXXX Method

            try
            {
                return uri.getClass().getMethod(
                        "get"
                                + org.apache.commons.lang.StringUtils
                                        .capitalize(property.toLowerCase()),
                        new Class[0]).invoke(uri, (Object[]) null);
            }
            catch (final IllegalArgumentException e)
            {
                logger.error(e.toString(), e);
            }
            catch (final SecurityException e)
            {
                logger.error(e.toString(), e);
            }
            catch (final IllegalAccessException e)
            {
                logger.error(e.toString(), e);
            }
            catch (final InvocationTargetException e)
            {
                logger.error(e.toString(), e);
            }
            catch (final NoSuchMethodException e)
            {
                logger.error(e.toString(), e);
            }

            throw new IllegalArgumentException(CoreMessages
                    .expressionInvalidForProperty(property, expression)
                    .getMessage());

        }
        else
        {

            return null;
        }
    }

    /** {@inheritDoc} */
    public String getName()
    {
        return NAME;
    }

    /** {@inheritDoc} */
    public void setName(final String name)
    {
        throw new UnsupportedOperationException("setName");
    }
}
