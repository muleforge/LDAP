package org.mule.transport.ldap;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.service.Service;
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
        MuleEndpointURI url = new MuleEndpointURI("ldap://ldap.in");
        Connector con = getConnector();
        return new DefaultInboundEndpoint(con, url, null, null, "testendpoint",
                new Properties(), null, null, true, null, false, false, 0,
                null, null, null, null);
    }

    @Override
    public MessageReceiver getMessageReceiver() throws Exception
    {
        Mock mockComponent = new Mock(Service.class);
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

        LdapConnector c = new LdapConnector();
        c.setLdapHost("localhost");
        c.setLdapPort(10389);
        c.setName("ldapTestConnector");

        c.setLoginDN("uid=admin,ou=system");
        c.setPassword("secret");

        c.setSearchBase("o=sevenSeas");

        Map map = new HashMap();
        map.put("payload.cn", "(cn=${payload})");

        c.setQueries(map);

        // c.initialise();

        return c;
    }

}
