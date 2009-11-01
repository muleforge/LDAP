/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.ldap;

import java.io.File;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

import org.mule.api.lifecycle.InitialisationException;
import org.mule.transport.ldap.util.TrustAllCertsManager;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPJSSESecureSocketFactory;
import com.novell.ldap.LDAPJSSEStartTLSFactory;
import com.novell.ldap.LDAPSocketFactory;
import com.novell.ldap.SaslLDAPConnection;

public class LdapSConnector extends LdapConnector
{

    private LDAPSocketFactory ssf = null;

    private boolean trustAll = false;
    private String trustStore = null;
    private boolean startTLS = false;

    public String getTrustStore()
    {
        return trustStore;
    }

    public void setTrustStore(final String trustStore)
    {
        this.trustStore = trustStore;
    }

    public LdapSConnector()
    {
        super();
        setLdapPort(LDAPConnection.DEFAULT_SSL_PORT);

    }

    @Override
    public String getProtocol()
    {
        return "ldaps";
    }

    protected void setupSSL() throws InitialisationException
    {
        try
        {
            logger.debug("trustAll: " + trustAll);
            logger.debug("trustStore: " + trustStore);
            if (trustAll)
            {
                final SSLContext context = SSLContext.getInstance("TLS");
                context.init(null, trustAll ? TrustAllCertsManager
                        .getTrustAllCertsManager() : null, null);

                // certificate_unknown

                if (startTLS)
                {
                    ssf = new LDAPJSSEStartTLSFactory(context
                            .getSocketFactory());
                }
                else
                {
                    ssf = new LDAPJSSESecureSocketFactory(context
                            .getSocketFactory());
                }

            }
            else
            {
                if (org.apache.commons.lang.StringUtils.isEmpty(trustStore))
                {
                    throw new InitialisationException(
                            new IllegalArgumentException(
                                    "Either trustAll value must be true or the trustStore parameter must be set"),
                            this);
                }

                final File trustStoreFile = new File(trustStore);

                if (!trustStoreFile.exists() || !trustStoreFile.canRead())
                {
                    throw new InitialisationException(
                            new IllegalArgumentException("truststore file "
                                    + trustStoreFile.getAbsolutePath()
                                    + " do not exist or is not readable"), this);
                }

                System.setProperty("javax.net.ssl.trustStore", trustStoreFile
                        .getAbsolutePath());
                // System.setProperty (
                // "javax.net.ssl.keyStore",trustStoreFile.getAbsolutePath() );
                // System.setProperty ( "javax.net.ssl.keyStorePassword",
                // "changeit" );

                logger.debug("truststore set to "
                        + trustStoreFile.getAbsolutePath());
                if (startTLS)
                {
                    ssf = new LDAPJSSEStartTLSFactory();
                }
                else
                {
                    ssf = new LDAPJSSESecureSocketFactory();
                }
            }

        }
        catch (final KeyManagementException e)
        {
            throw new InitialisationException(e, this);
        }
        catch (final NoSuchAlgorithmException e)
        {
            throw new InitialisationException(e, this);
        }

        // super.setSsf(ssf);
    }

    protected boolean initSSL()
    {
        return true;
    }

    @Override
    protected void doInitialise() throws InitialisationException
    {

        if (initSSL())
        {
            setupSSL();
        }

        super.doInitialise();
    }

    public boolean isTrustAll()
    {
        return trustAll;
    }

    public void setTrustAll(final boolean trustAll)
    {
        this.trustAll = trustAll;
    }

    @Override
    protected boolean isAnonymousBindSupported()
    {

        return false;
    }

    @Override
    protected LDAPConnection createLDAPConnection()
    {
        

        if (initSSL())
        {
            return new SaslLDAPConnection(ssf);

        }
        else
        {
            return new SaslLDAPConnection();
        }

        //FIXME
        //setLdapConnection(c);
    }

    @Override
    protected void doBind(LDAPConnection lc) throws Exception
    {

        if (initSSL() && startTLS)
        {
            lc.startTLS();

        }
        super.doBind(lc);
    }

    public boolean isStartTLS()
    {
        return startTLS;
    }

    public void setStartTLS(final boolean startTLS)
    {
        this.startTLS = startTLS;
    }

}
