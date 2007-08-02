/*
 * $Id: LdapMessageDispatcherFactory.java,v 1.1 2007/07/30 22:34:33 hsaly Exp $
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.ldap;

import org.mule.providers.AbstractMessageDispatcherFactory;
import org.mule.umo.UMOException;
import org.mule.umo.endpoint.UMOImmutableEndpoint;
import org.mule.umo.provider.UMOMessageDispatcher;

/**
 * <code>LdapMessageDispatcherFactory</code> Todo document
 */

public class LdapMessageDispatcherFactory extends
        AbstractMessageDispatcherFactory
{

    /*
     * For general guidelines on writing transports see
     * http://mule.mulesource.org/display/MULE/Writing+Transports
     */

    public UMOMessageDispatcher create(UMOImmutableEndpoint endpoint)
            throws UMOException
    {
        return new LdapMessageDispatcher(endpoint);
    }

}
