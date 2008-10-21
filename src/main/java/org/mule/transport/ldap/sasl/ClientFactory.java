/*
 * $Id: LdapConnector.java 172 2008-10-17 11:12:59Z hsaly $
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.ldap.sasl;

import java.util.Map;

import javax.security.auth.callback.CallbackHandler;
import javax.security.sasl.Sasl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.novell.security.sasl.SaslClient;
import com.novell.security.sasl.SaslException;

public class ClientFactory implements
        com.novell.security.sasl.SaslClientFactory
{

    protected final Log logger = LogFactory.getLog(getClass());

    public ClientFactory()
    {
        super();
        // System.out.println(this.getClass()+" inst.");
    }

    public SaslClient createSaslClient(String[] mechanisms,
            String authorizationID, String protocol, String serverName,
            Map props, CallbackHandler cbh) throws SaslException
    {

        logger.debug("try to get sun sasl client to wrap:" + authorizationID);

        try
        {

            final javax.security.sasl.SaslClient sc = Sasl.createSaslClient(
                    mechanisms, authorizationID, protocol, serverName, props,
                    cbh);

            logger.debug("sun sasl client to wrap:" + sc);

            return new SaslClient0(sc);

        }
        catch (javax.security.sasl.SaslException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    public String[] getMechanismNames(Map props)
    {
        // TODO further mechanisms
        return new String[]
        {"CRAM-MD5", "EXTERNAL", "GSSAPI", "DIGEST-MD5", "PLAIN"};
    }

    private static final class SaslClient0 implements SaslClient
    {
        private final javax.security.sasl.SaslClient sc;

        private SaslClient0(javax.security.sasl.SaslClient sc)
        {
            this.sc = sc;
        }

        public void dispose() throws SaslException
        {
            try
            {
                sc.dispose();
            }
            catch (javax.security.sasl.SaslException e)
            {
                throw new SaslException(e.toString());
            }
        }

        public byte[] evaluateChallenge(byte[] challenge) throws SaslException
        {
            try
            {
                return sc.evaluateChallenge(challenge);
            }
            catch (javax.security.sasl.SaslException e)
            {
                // TODO Auto-generated catch
                // block
                throw new SaslException(e.toString());
            }
        }

        public String getMechanismName()
        {
            return sc.getMechanismName();
        }

        public Object getNegotiatedProperty(String propName)
        {
            return sc.getNegotiatedProperty(propName);
        }

        public boolean hasInitialResponse()
        {
            return sc.hasInitialResponse();
        }

        public boolean isComplete()
        {
            return sc.isComplete();
        }

        public byte[] unwrap(byte[] incoming, int offset, int len)
                throws SaslException
        {
            try
            {
                return sc.unwrap(incoming, offset, len);
            }
            catch (javax.security.sasl.SaslException e)
            {
                // TODO Auto-generated catch
                // block
                throw new SaslException(e.toString());
            }
        }

        public byte[] wrap(byte[] outgoing, int offset, int len)
                throws SaslException
        {
            try
            {
                return sc.wrap(outgoing, offset, len);
            }
            catch (javax.security.sasl.SaslException e)
            {
                // TODO Auto-generated catch
                // block
                throw new SaslException(e.toString());
            }
        }
    }
}
