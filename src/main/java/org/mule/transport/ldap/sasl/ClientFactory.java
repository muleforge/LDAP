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

import java.util.Arrays;
import java.util.Map;

import javax.security.auth.callback.CallbackHandler;

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

    public SaslClient createSaslClient(final String[] mechanisms,
            final String authorizationID, final String protocol,
            final String serverName, final Map props, final CallbackHandler cbh)
            throws SaslException
    {

        logger.debug("try to get sun sasl client to wrap:" + authorizationID
                + "/" + Arrays.toString(mechanisms) + "/" + protocol + "/"
                + serverName + "/" + props + "/" + cbh);

        try
        {

            final javax.security.sasl.SaslClient sc = javax.security.sasl.Sasl
                    .createSaslClient(mechanisms, authorizationID, protocol,
                            serverName, props, cbh);

            logger.debug("sun sasl client to wrap:" + sc);

            return new SaslClient0(sc);

        }
        catch (final javax.security.sasl.SaslException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    public String[] getMechanismNames(final Map props)
    {
        // TODO further mechanisms

        return new String[]
        {"CRAM-MD5", "EXTERNAL", "GSSAPI", "DIGEST-MD5", "PLAIN"};
    }

    private static final class SaslClient0 implements SaslClient
    {
        private final javax.security.sasl.SaslClient sc;
        private final Log logger = LogFactory.getLog(getClass());

        private SaslClient0(final javax.security.sasl.SaslClient sc)
        {
            this.sc = sc;
        }

        public void dispose() throws SaslException
        {
            try
            {
                logger.debug("dispose");
                sc.dispose();
            }
            catch (final javax.security.sasl.SaslException e)
            {
                throw new SaslException(e.toString());
            }
        }

        public byte[] evaluateChallenge(final byte[] challenge)
                throws SaslException
        {
            try
            {
                logger.debug("evaluateChallenge");
                return sc.evaluateChallenge(challenge);
            }
            catch (final javax.security.sasl.SaslException e)
            {
                // TODO Auto-generated catch
                // block
                throw new SaslException(e.toString());
            }
        }

        public String getMechanismName()
        {
            logger.debug("sc.getMechanismName(): " + sc.getMechanismName());
            return sc.getMechanismName();
        }

        public Object getNegotiatedProperty(final String propName)
        {
            logger.debug("sc.getNegotiatedProperty(propName): "
                    + sc.getNegotiatedProperty(propName));
            return sc.getNegotiatedProperty(propName);
        }

        public boolean hasInitialResponse()
        {
            logger.debug("sc.hasInitialResponse(): " + sc.hasInitialResponse());
            return sc.hasInitialResponse();
        }

        public boolean isComplete()
        {
            logger.debug("sc.isComplete(): " + sc.isComplete());
            return sc.isComplete();
        }

        public byte[] unwrap(final byte[] incoming, final int offset,
                final int len) throws SaslException
        {
            try
            {
                logger.debug("unwrap");
                return sc.unwrap(incoming, offset, len);
            }
            catch (final javax.security.sasl.SaslException e)
            {
                // TODO Auto-generated catch
                // block
                throw new SaslException(e.toString());
            }
        }

        public byte[] wrap(final byte[] outgoing, final int offset,
                final int len) throws SaslException
        {
            try
            {
                logger.debug("wrap");
                return sc.wrap(outgoing, offset, len);
            }
            catch (final javax.security.sasl.SaslException e)
            {
                // TODO Auto-generated catch
                // block
                throw new SaslException(e.toString());
            }
        }
    }
}
