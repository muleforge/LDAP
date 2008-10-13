package org.mule.providers.ldap;

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleMessage;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.transport.MessageAdapter;
import org.mule.transport.AbstractMessageRequester;

import com.novell.ldap.LDAPMessage;

public class LdapMessageRequester extends AbstractMessageRequester {

	public LdapMessageRequester(InboundEndpoint endpoint) {
		super(endpoint);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected MuleMessage doRequest(long timeout) throws Exception {
		// TODO Auto-generated method stub

		logger.debug("doRequest(long timeout)");
		logger.debug(endpoint.toString());

		LdapConnector c = (LdapConnector) this.connector;

		LDAPMessage msg = null;

		final long start = System.currentTimeMillis();

		while (msg == null && (System.currentTimeMillis() - start) < timeout) {
			msg = c.pollQueue();
			Thread.yield();
			Thread.sleep(50);
		}

		if (msg == null) {
		
			logger.debug("timeout");
			return null;
		}

		MessageAdapter adapter = connector.getMessageAdapter(msg);
		return new DefaultMuleMessage(adapter);

		// throw new Exception("not implementd");
		// return new DefaultMuleMessage(null);
	}

	@Override
	protected void doConnect() throws Exception {
		((LdapConnector) this.connector).ensureConnected();

	}

	@Override
	protected void doDisconnect() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub

	}

}
