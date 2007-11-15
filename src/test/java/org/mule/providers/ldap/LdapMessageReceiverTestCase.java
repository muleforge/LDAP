package org.mule.providers.ldap;

import org.mule.impl.endpoint.MuleEndpoint;
import org.mule.providers.AbstractConnector;
import org.mule.tck.providers.AbstractMessageReceiverTestCase;
import org.mule.umo.UMOComponent;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.provider.UMOMessageReceiver;

import com.mockobjects.dynamic.Mock;

public class LdapMessageReceiverTestCase extends
        AbstractMessageReceiverTestCase
{

    public UMOEndpoint getEndpoint() throws Exception
    {

        endpoint = new MuleEndpoint("ldap://ldap.in", true);
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
                endpoint, 0);
    }

}
