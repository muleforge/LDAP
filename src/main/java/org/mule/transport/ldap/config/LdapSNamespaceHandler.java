package org.mule.transport.ldap.config;

import org.mule.endpoint.URIBuilder;
import org.mule.transport.ldap.LdapSConnector;

public class LdapSNamespaceHandler extends LdapNamespaceHandler
{

    @Override
    protected void initSpecific()
    {
        registerStandardTransportEndpoints("ldaps", ADDRESS_ATTRIBUTES)
                .addAlias(QUERY_KEY, URIBuilder.PATH);

        registerConnectorDefinitionParser(LdapSConnector.class);

    }

}
