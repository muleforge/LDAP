/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.ldap;

import java.net.URI;
import java.util.Properties;

import org.mule.api.endpoint.MalformedEndpointException;
import org.mule.endpoint.ResourceNameEndpointURIBuilder;

public class LdapEndpointBuilder extends ResourceNameEndpointURIBuilder
{

    @Override
    protected void setEndpoint(final URI uri, final Properties props)
            throws MalformedEndpointException
    {

        super.setEndpoint(uri, props);

        // validate
        if (!"ldap.in".equals(this.address)
                && !this.address.startsWith("ldap.out"))
        {
            throw new MalformedEndpointException(
                    this.address
                            + " (only 'ldap[s]://ldap.in' or 'ldap[s]://ldap.out[/query]' supported.)");
        }

        if (!org.apache.commons.lang.StringUtils.isEmpty(this.userInfo))
        {
            throw new MalformedEndpointException(
                    this.address
                            + " (only 'ldap[s]://ldap.in' or 'ldap[s]://ldap.out[/query]' without userinfo supported.)");
        }

    }

}
