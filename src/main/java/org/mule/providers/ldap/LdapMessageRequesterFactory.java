package org.mule.providers.ldap;

import org.mule.api.MuleException;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.transport.MessageRequester;
import org.mule.transport.AbstractMessageRequesterFactory;

public class LdapMessageRequesterFactory extends
AbstractMessageRequesterFactory{

	@Override
	public MessageRequester create(InboundEndpoint endpoint)
			throws MuleException {
		// TODO Auto-generated method stub
		return new LdapMessageRequester(endpoint);
	}

}
