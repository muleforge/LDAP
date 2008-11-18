package org.mule.transport.ldap.config;

import org.mule.endpoint.URIBuilder;
import org.mule.transport.ldap.LdapSASLConnector;

public class LdapSASLNamespaceHandler extends LdapSNamespaceHandler
{

    @Override
    protected void initSpecific()
    {
        registerStandardTransportEndpoints("ldapsasl", ADDRESS_ATTRIBUTES)
                .addAlias(QUERY_KEY, URIBuilder.PATH);

        registerConnectorDefinitionParser(LdapSASLConnector.class);
    }

}
