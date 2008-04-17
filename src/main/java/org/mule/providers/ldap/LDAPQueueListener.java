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
import org.mule.api.MuleMessage;
import org.mule.api.endpoint.ImmutableEndpoint;


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
    void onMessage(MuleMessage message, ImmutableEndpoint endpoint)
            throws MuleException;

}
