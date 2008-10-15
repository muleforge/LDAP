package com.novell.security.sasl.client.pkgs;

import java.util.Map;

import javax.security.auth.callback.CallbackHandler;
import javax.security.sasl.Sasl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.novell.security.sasl.SaslClient;
import com.novell.security.sasl.SaslException;

public class ClientFactory implements com.novell.security.sasl.SaslClientFactory {

    protected final Log logger = LogFactory.getLog(getClass());

	
	public ClientFactory() {
		super();
		//System.out.println(this.getClass()+" inst.");
	}

	public SaslClient createSaslClient(
			String[] mechanisms,
			String authorizationID,
			String protocol, String serverName,
			Map props, CallbackHandler cbh)
			throws SaslException {
		
		logger.debug("try to get sun sasl client to wrap:"+authorizationID);

		try {
			
			
			
			final javax.security.sasl.SaslClient sc = Sasl
					.createSaslClient(mechanisms,
							authorizationID,
							protocol, serverName,
							props, cbh);
			
			logger.debug("sun sasl client to wrap:"+sc);

			return new SaslClient() {

				public void dispose()
						throws SaslException {
					try {
						sc.dispose();
					} catch (javax.security.sasl.SaslException e) {
						throw new SaslException(e
								.toString());
					}
				}

				public byte[] evaluateChallenge(
						byte[] challenge)
						throws SaslException {
					try {
						return sc
								.evaluateChallenge(challenge);
					} catch (javax.security.sasl.SaslException e) {
						// TODO Auto-generated catch
						// block
						throw new SaslException(e
								.toString());
					}
				}

				public String getMechanismName() {
					return sc.getMechanismName();
				}

				public Object getNegotiatedProperty(
						String propName) {
					return sc
							.getNegotiatedProperty(propName);
				}

				public boolean hasInitialResponse() {
					return sc.hasInitialResponse();
				}

				public boolean isComplete() {
					return sc.isComplete();
				}

				public byte[] unwrap(
						byte[] incoming,
						int offset, int len)
						throws SaslException {
					try {
						return sc.unwrap(incoming,
								offset, len);
					} catch (javax.security.sasl.SaslException e) {
						// TODO Auto-generated catch
						// block
						throw new SaslException(e
								.toString());
					}
				}

				public byte[] wrap(byte[] outgoing,
						int offset, int len)
						throws SaslException {
					try {
						return sc.wrap(outgoing,
								offset, len);
					} catch (javax.security.sasl.SaslException e) {
						// TODO Auto-generated catch
						// block
						throw new SaslException(e
								.toString());
					}
				}

			};

		} catch (javax.security.sasl.SaslException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public String[] getMechanismNames(Map props) {
		// TODO further mechanisms
		return new String[]{"CRAM-MD5","EXTERNAL","GSSAPI","DIGEST-MD5","PLAIN"};
	}

}
