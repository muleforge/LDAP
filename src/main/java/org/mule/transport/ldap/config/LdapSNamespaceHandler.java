package org.mule.transport.ldap.config;

import org.mule.config.spring.handlers.AbstractMuleNamespaceHandler;
import org.mule.config.spring.parsers.MuleDefinitionParser;
import org.mule.config.spring.parsers.collection.ChildSingletonMapDefinitionParser;
import org.mule.config.spring.parsers.delegate.ParentContextDefinitionParser;
import org.mule.config.spring.parsers.specific.properties.NestedMapDefinitionParser;
import org.mule.endpoint.URIBuilder;
import org.mule.transport.ldap.LdapSConnector;

public class LdapSNamespaceHandler extends AbstractMuleNamespaceHandler
{

    public static final String QUERY_KEY = "queryKey";
    public static final String[] ADDRESS_ATTRIBUTES = new String[]
    {QUERY_KEY};

    public void init()
    {
        registerStandardTransportEndpoints("ldaps", ADDRESS_ATTRIBUTES)
                .addAlias(QUERY_KEY, URIBuilder.PATH);

        registerConnectorDefinitionParser(LdapSConnector.class);

        // children
        final MuleDefinitionParser connectorQuery = new ChildSingletonMapDefinitionParser(
                "query");

        //
        final MuleDefinitionParser endpointQuery = new NestedMapDefinitionParser(
                "properties", "queries");

        endpointQuery.addCollection("properties");

        registerMuleBeanDefinitionParser("query",
                new ParentContextDefinitionParser("connector", connectorQuery)
                        .otherwise(endpointQuery));

    }

}
