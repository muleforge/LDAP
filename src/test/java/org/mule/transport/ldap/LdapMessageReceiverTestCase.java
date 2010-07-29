package org.mule.transport.ldap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.mule.api.MuleContext;
import org.mule.api.endpoint.EndpointMessageProcessorChainFactory;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.processor.MessageProcessor;
import org.mule.api.retry.RetryPolicyTemplate;
import org.mule.api.routing.filter.Filter;
import org.mule.api.security.EndpointSecurityFilter;
import org.mule.api.service.Service;
import org.mule.api.transaction.TransactionConfig;
import org.mule.api.transport.Connector;
import org.mule.api.transport.MessageReceiver;
import org.mule.endpoint.DefaultInboundEndpoint;
import org.mule.endpoint.MuleEndpointURI;
import org.mule.transport.AbstractConnector;
import org.mule.transport.AbstractMessageReceiverTestCase;

import com.mockobjects.dynamic.Mock;

public class LdapMessageReceiverTestCase extends
        AbstractMessageReceiverTestCase
{

    @Override
    public InboundEndpoint getEndpoint() throws Exception
    {
        final MuleEndpointURI url = new MuleEndpointURI("ldap://ldap.in",muleContext);
        final Connector con = getConnector();
        return new DefaultInboundEndpoint(con, url, (List) null, (List) null,
                "testendpoint", new Properties(), (TransactionConfig) null,
                (Filter) null, false, (EndpointSecurityFilter) null, false, 0,
                (String) null, (String) null, (String) null,
                (MuleContext) null, (RetryPolicyTemplate) null, (EndpointMessageProcessorChainFactory) null,(List<MessageProcessor>) null,(List<MessageProcessor>) null);
    }

    @Override
    public MessageReceiver getMessageReceiver() throws Exception
    {
        final Mock mockComponent = new Mock(Service.class);
        mockComponent.expectAndReturn("getResponseRouter", null);
        // Mock mockDescriptor = new Mock(UMODescriptor.class);
        // mockComponent.expectAndReturn("getDescriptor",
        // mockDescriptor.proxy());
        // mockDescriptor.expectAndReturn("getResponseTransformer", null);

        return new LdapMessageReceiver((AbstractConnector) endpoint
                .getConnector(), (Service) mockComponent.proxy(), endpoint);
    }

    public Connector getConnector() throws Exception
    {

        final LdapConnector c = new LdapConnector(muleContext);
        c.setLdapHost("localhost");
        c.setLdapPort(10389);
        c.setName("ldapTestConnector");

        c.setLoginDN("uid=admin,ou=system");
        c.setPassword("secret");

        c.setSearchBase("o=sevenSeas");

        final Map map = new HashMap();
        map.put("payload.cn", "(cn=${payload})");

        c.setQueries(map);

        // c.initialise();

        return c;
    }

}
