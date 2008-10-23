/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.ldap.routers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.api.routing.RoutingException;
import org.mule.api.transformer.TransformerException;
import org.mule.routing.EventCorrelatorCallback;
import org.mule.routing.inbound.EventGroup;
import org.mule.routing.response.ResponseCorrelationAggregator;
import org.mule.transport.ldap.util.LDAPUtils;

import com.novell.ldap.LDAPMessage;
import com.novell.ldap.LDAPResponse;

public class LdapResponseCorrelationAggregator extends
        ResponseCorrelationAggregator
{

    @Override
    protected EventCorrelatorCallback getCorrelatorCallback()
    {
        // TODO Auto-generated method stub
        return new LdapResponseEventCorrelatorCallback();
    }

    public class LdapResponseEventCorrelatorCallback extends
            DefaultEventCorrelatorCallback
    {

        @Override
        public boolean shouldAggregateEvents(final EventGroup events)
        {

            synchronized (events)
            {

                final Iterator it = events.iterator();
                while (it.hasNext())
                {
                    final MuleEvent event = (MuleEvent) it.next();

                    try
                    {
                        logger.debug(">--- aggregator should ---> "
                                + LDAPUtils.dumpLDAPMessage(event
                                        .transformMessage()));

                        if (event.transformMessage() instanceof LDAPResponse)
                        {
                            // last message is a LDapResponse
                            logger
                                    .debug(">--- aggregator should ---> shouldAggregateEvents return true");
                            return true;
                        }
                    }
                    catch (final TransformerException e)
                    {
                        throw new RuntimeException(e);
                    }
                }

                return false;
            }
        }

        @Override
        public MuleMessage aggregateEvents(final EventGroup events)
                throws RoutingException
        {

            logger.debug(">--- aggregator enter aggregateEvents ---> ");

            LDAPMessage msg = null;
            MuleEvent event = null;
            final List results = new ArrayList();

            try
            {
                for (final Iterator iterator = events.iterator(); iterator
                        .hasNext();)
                {
                    event = (MuleEvent) iterator.next();
                    msg = (LDAPMessage) event.transformMessage();

                    results.add(msg);

                }
            }
            catch (final TransformerException e)
            {
                logger.error(e);

                if (event != null)
                {
                    throw new RoutingException(event.getMessage(), event
                            .getEndpoint());
                }

                throw new RuntimeException(e);
            }

            if (event != null)
            {

                logger.debug(">--- aggregator leave  aggregateEvents with "
                        + results.size() + " msgs ---> ");
                return new DefaultMuleMessage(results, event.getMessage());
            }

            throw new RuntimeException("event is null");
        }

    }

}
