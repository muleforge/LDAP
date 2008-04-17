/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.ldap;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;

import javax.net.ssl.SSLContext;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.providers.ldap.util.TrustAllCertsManager;
import org.mule.util.StringUtils;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPJSSESecureSocketFactory;
import com.novell.security.sasl.RealmCallback;
import com.novell.security.sasl.RealmChoiceCallback;

public class LdapSASLConnector extends LdapConnector
{

    private static final String MECHANISM_DIGEST_MD5 = "DIGEST-MD5";
    private static final String MECHANISM_DIGEST_EXTERNAL = "EXTERNAL";
    private boolean trustAll = false;
    private String trustStore = null;
    private final boolean forceJDK14 = true;
    private String realm = null;

    private String mechanism = MECHANISM_DIGEST_MD5;

    private LDAPJSSESecureSocketFactory ssf;

    public LdapSASLConnector()
    {
        super();

        if (MECHANISM_DIGEST_EXTERNAL.equals(mechanism))
        {
            setLdapPort(LDAPConnection.DEFAULT_SSL_PORT);
        }
        else
        {

            setLdapPort(LDAPConnection.DEFAULT_PORT);
        }

    }

    // @Override
    protected void doInitialise() throws InitialisationException
    {

        // if (isForceJDK14())
        // {
        // logger.debug("forcing JDK 1.4 SASL mode");
        Security.addProvider(new com.novell.sasl.client.SaslProvider());
        // }
        /*
         * else { Provider sunSASL = Security.getProvider("SunSASL");
         * 
         * if (sunSASL != null) { logger .debug("SunSASL implementation (JDK >=
         * 1.5) detected. Use it."); try { Sasl.setSaslClientFactory(new
         * SaslBridgeClientFactory()); } catch (RuntimeException e) {
         * logger.warn(e.toString()); } } else { logger .debug("No SunSASL
         * implementation (JDK >= 1.5 detected. Fall back to JDK 1.4 mode");
         * Security.addProvider(new com.novell.sasl.client.SaslProvider()); } }
         */

        if (logger.isDebugEnabled())
        {

            Provider[] ps = Security.getProviders();
            for (int i = 0; i < ps.length; i++)
            {
                Provider provider = ps[i];
                logger.debug(provider.getClass() + "/" + provider.getName()
                        + "/" + provider.getVersion() + "/"
                        + provider.getInfo());

            }
        }

        if (MECHANISM_DIGEST_EXTERNAL.equals(mechanism))
        {

            try
            {
                if (trustAll)
                {
                    SSLContext context = SSLContext.getInstance("TLS");
                    context.init(null, trustAll ? TrustAllCertsManager
                            .getTrustAllCertsManager() : null, null);

                    // certificate_unknown
                    ssf = new LDAPJSSESecureSocketFactory(context
                            .getSocketFactory());
                }
                else
                {
                    if (StringUtils.isEmpty(trustStore))
                    {
                        throw new InitialisationException(
                                new IllegalArgumentException(
                                        "Either trustAll value must be true or the trustStore parameter must be set"),
                                this);
                    }

                    File trustStoreFile = new File(trustStore);

                    if (!trustStoreFile.exists() || !trustStoreFile.canRead())
                    {
                        throw new InitialisationException(
                                new IllegalArgumentException("truststore file "
                                        + trustStoreFile.getAbsolutePath()
                                        + " do not exist or is not readable"),
                                this);
                    }

                    System.setProperty("javax.net.ssl.trustStore", trustStore);

                    logger.debug("truststore set to "
                            + trustStoreFile.getAbsolutePath());

                    ssf = new LDAPJSSESecureSocketFactory();
                }
                // pix path
                // ssf = new LDAPJSSESecureSocketFactory((SSLSocketFactory)
                // SSLSocketFactory.getDefault());

                // TODO SSL<->TLS (TLS maybe require startTLS() call on lc
                // ssf = new LDAPJSSEStartTLSFactory();
            }
            catch (KeyManagementException e)
            {
                throw new InitialisationException(e, this);
            }
            catch (NoSuchAlgorithmException e)
            {
                throw new InitialisationException(e, this);
            }

        }

        super.doInitialise();
    }

    // @Override
    protected void doBind() throws Exception
    {
        logger.debug("bind with mechanism " + mechanism);

        if (MECHANISM_DIGEST_EXTERNAL.equals(mechanism))
        {
            getLdapConnection().bind((String) null, (String) null, new String[]
            {mechanism}, null, (Object) null);
        }
        else
        {

            getLdapConnection().bind(getLoginDN(), getLoginDN(), new String[]
            {mechanism}, null, new BindCallbackHandler(getPassword()));
        }
    }

    public String getProtocol()
    {
        return "ldap";
    }

    // @Override
    protected boolean isAnonymousBindSupported()
    {

        return false;
    }

    public String getMechanism()
    {
        return mechanism;
    }

    public void setMechanism(String mechanism)
    {
        this.mechanism = mechanism;
    }

    // @Override
    protected void setLDAPConnection()
    {

        if (MECHANISM_DIGEST_EXTERNAL.equals(mechanism))
        {
            super.setLdapConnection(new LDAPConnection(ssf));
        }
        else
        {
            super.setLdapConnection(new LDAPConnection());
        }

    }

    public boolean isTrustAll()
    {
        return trustAll;
    }

    public void setTrustAll(boolean trustAll)
    {
        this.trustAll = trustAll;
    }

    public String getRealm()
    {
        return realm;
    }

    public void setRealm(String realm)
    {
        this.realm = realm;
    }

    public boolean isForceJDK14()
    {
        return forceJDK14;
    }

    private class BindCallbackHandler implements CallbackHandler
    {
        private final Log logger = LogFactory.getLog(getClass());
        private char[] passwordChars;

        BindCallbackHandler(String password)
        {

            passwordChars = new char[password.length()];
            password.getChars(0, password.length(), passwordChars, 0);
        }

        public void handle(Callback[] callbacks) throws IOException,
                UnsupportedCallbackException
        {

            for (int i = 0; i < callbacks.length; i++)
            {
                logger.debug(callbacks[i].getClass());

                if (callbacks[i] instanceof PasswordCallback)
                {

                    logger.debug("******");

                    ((PasswordCallback) callbacks[i])
                            .setPassword(passwordChars);

                }
                else if (callbacks[i] instanceof NameCallback)
                {
                    logger
                            .debug(((NameCallback) callbacks[i])
                                    .getDefaultName());

                    ((NameCallback) callbacks[i])
                            .setName(((NameCallback) callbacks[i])
                                    .getDefaultName());

                }
                else if (callbacks[i] instanceof RealmCallback)
                {

                    String result = ((RealmCallback) callbacks[i])
                            .getDefaultText();

                    if (realm != null)
                    {
                        result = realm;
                    }

                    ((RealmCallback) callbacks[i]).setText(result);

                    logger.debug(result);

                }
                else if (callbacks[i] instanceof RealmCallback)
                {

                    String result = ((RealmCallback) callbacks[i])
                            .getDefaultText();

                    if (realm != null)
                    {
                        result = realm;
                    }

                    ((RealmCallback) callbacks[i]).setText(result);

                    logger.debug(result);

                }
                else if (callbacks[i] instanceof RealmChoiceCallback)
                {

                    ((RealmChoiceCallback) callbacks[i]).setSelectedIndex(0);

                }
                else if (callbacks[i] instanceof RealmChoiceCallback)
                {

                    ((RealmChoiceCallback) callbacks[i]).setSelectedIndex(0);

                }

            }

        }

    }

}
