package org.mule.providers.ldap;

import java.net.URI;
import java.util.Properties;

import org.mule.impl.endpoint.ResourceNameEndpointBuilder;
import org.mule.umo.endpoint.MalformedEndpointException;
import org.mule.util.StringUtils;

public class LdapEndpointBuilder extends ResourceNameEndpointBuilder
{

    // @Override
    protected void setEndpoint(URI uri, Properties props)
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

        if (!StringUtils.isEmpty(this.userInfo))
        {
            throw new MalformedEndpointException(
                    this.address
                            + " (only 'ldap[s]://ldap.in' or 'ldap[s]://ldap.out[/query]' without userinfo supported.)");
        }

    }

}
