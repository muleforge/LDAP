package org.mule.transport.ldap.config;

import org.mule.config.spring.handlers.AbstractMuleNamespaceHandler;
import org.mule.config.spring.parsers.MuleDefinitionParser;
import org.mule.config.spring.parsers.collection.ChildSingletonMapDefinitionParser;
import org.mule.config.spring.parsers.delegate.ParentContextDefinitionParser;
import org.mule.config.spring.parsers.generic.ParentDefinitionParser;
import org.mule.config.spring.parsers.specific.ObjectFactoryWrapper;
import org.mule.config.spring.parsers.specific.TransactionDefinitionParser;
import org.mule.config.spring.parsers.specific.properties.NestedMapDefinitionParser;
import org.mule.endpoint.URIBuilder;
import org.mule.transport.ldap.LdapConnector;

public class LdapNamespaceHandler extends AbstractMuleNamespaceHandler
{ 

    public static final String QUERY_KEY = "queryKey";
    public static final String[] ADDRESS_ATTRIBUTES = new String[]{QUERY_KEY};

    public void init()
    {
        registerStandardTransportEndpoints("ldap", ADDRESS_ATTRIBUTES).addAlias(QUERY_KEY, URIBuilder.PATH);
        registerConnectorDefinitionParser(LdapConnector.class);
        registerBeanDefinitionParser("dataSource", new ObjectFactoryWrapper("dataSourceFactory"));
        MuleDefinitionParser connectorQuery = new ChildSingletonMapDefinitionParser("query");
        MuleDefinitionParser endpointQuery = new NestedMapDefinitionParser("properties", "queries");
        endpointQuery.addCollection("properties");
        registerMuleBeanDefinitionParser("query", new ParentContextDefinitionParser("connector", connectorQuery).otherwise(endpointQuery));
        registerBeanDefinitionParser("extractors", new ParentDefinitionParser());
        registerBeanDefinitionParser("transaction", new TransactionDefinitionParser(LdapConnector.class));
    }

}


