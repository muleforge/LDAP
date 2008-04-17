package org.mule.providers.ldap;

import org.mule.endpoint.MuleEndpointURI;
import org.mule.transport.AbstractConnector;
import org.mule.transport.AbstractMessageReceiverTestCase;

import com.mockobjects.dynamic.Mock;

public class LdapMessageReceiverTestCase extends
        AbstractMessageReceiverTestCase
{

    public UMOEndpoint getEndpoint() throws Exception
    {

        endpoint = new MuleEndpoin("ldap://ldap.in", true);
        return endpoint;
    }

    public UMOMessageReceiver getMessageReceiver() throws Exception
    {

        Mock mockComponent = new Mock(UMOComponent.class);
        // Mock mockDescriptor = new Mock(UMODescriptor.class);
        // mockComponent.expectAndReturn("getDescriptor",
        // mockDescriptor.proxy());
        // mockDescriptor.expectAndReturn("getResponseTransformer", null);

        return new LdapMessageReceiver((AbstractConnector) endpoint
                .getConnector(), (UMOComponent) mockComponent.proxy(),
                endpoint);
    }

}
