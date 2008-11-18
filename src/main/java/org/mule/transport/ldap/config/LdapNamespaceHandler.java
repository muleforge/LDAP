package org.mule.transport.ldap.config;

import org.mule.config.spring.handlers.AbstractMuleNamespaceHandler;
import org.mule.config.spring.parsers.MuleDefinitionParser;
import org.mule.config.spring.parsers.collection.ChildSingletonMapDefinitionParser;
import org.mule.config.spring.parsers.delegate.ParentContextDefinitionParser;
import org.mule.config.spring.parsers.specific.TransformerDefinitionParser;
import org.mule.config.spring.parsers.specific.properties.NestedMapDefinitionParser;
import org.mule.endpoint.URIBuilder;
import org.mule.transport.ldap.LdapConnector;
import org.mule.transport.ldap.transformers.LDAPEntryToAddRequest;
import org.mule.transport.ldap.transformers.LDAPMessageToString;

public class LdapNamespaceHandler extends AbstractMuleNamespaceHandler
{

    public static final String QUERY_KEY = "queryKey";
    public static final String[] ADDRESS_ATTRIBUTES = new String[]
    {QUERY_KEY};

    public final void init()
    {
        initSpecific();
        initGlobals();

    }

    protected void initSpecific()
    {
        registerStandardTransportEndpoints("ldap", ADDRESS_ATTRIBUTES)
                .addAlias(QUERY_KEY, URIBuilder.PATH);

        registerConnectorDefinitionParser(LdapConnector.class);
    }

    private void initGlobals()
    {

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

        registerBeanDefinitionParser("ldapentry-to-ldapaddrequest-transformer",
                new TransformerDefinitionParser(LDAPEntryToAddRequest.class));
        registerBeanDefinitionParser("ldapmessage-to-string-transformer",
                new TransformerDefinitionParser(LDAPMessageToString.class));

    }

}
