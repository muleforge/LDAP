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

import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOImmutableEndpoint;

/**
 * 
 * @author Administrator
 * 
 */
public interface LDAPQueueListener
{
    /**
     * 
     * @param message
     * @param endpoint
     * @throws UMOException
     */
    void onMessage(UMOMessage message, UMOImmutableEndpoint endpoint)
            throws UMOException;

}
