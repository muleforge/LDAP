/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.ldap;

import org.mule.api.MuleException;
import org.mule.api.endpoint.OutboundEndpoint;
import org.mule.transport.AbstractMessageDispatcherFactory;



/**
 * <code>LdapMessageDispatcherFactory</code> Todo document
 */

public class LdapMessageDispatcherFactory extends
        AbstractMessageDispatcherFactory
{

    @Override
    public org.mule.api.transport.MessageDispatcher create(OutboundEndpoint endpoint)
            throws MuleException
    {
       
    return new LdapMessageDispatcher(endpoint);
    }

    /*
     * For general guidelines on writing transports see
     * http://mule.mulesource.org/display/MULE/Writing+Transports
     */

 



}
