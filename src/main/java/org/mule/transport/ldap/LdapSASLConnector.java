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

import java.io.IOException;
import java.security.Provider;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.mule.api.MuleContext;
import org.mule.api.lifecycle.InitialisationException;

import com.novell.ldap.LDAPConnection;
import com.novell.security.sasl.RealmCallback;
import com.novell.security.sasl.RealmChoiceCallback;

public class LdapSASLConnector extends LdapSConnector
{

    private boolean useSSL = false;
    private boolean forceJDK14 = false;
    private String alternativeSaslProvider = null;
    private String realm = null;
    private boolean jdkSaslSupported = false;
    private String authorizationID = null;

    private String mechanism = "DIGEST-MD5";

    // private LDAPJSSESecureSocketFactory ssf;

    public LdapSASLConnector(MuleContext context)
    {
        super(context);

        logger.debug("instantiated");

        if (useSSL)
        {
            setLdapPort(LDAPConnection.DEFAULT_SSL_PORT);
        }
        else
        {

            setLdapPort(LDAPConnection.DEFAULT_PORT);
        }

    }

    @Override
    protected void doInitialise() throws InitialisationException
    {

        // Security.addProvider(new com.novell.sasl.client.SaslProvider());

        if (isForceJDK14())
        {
            logger.debug("Forcing JDK 1.4 SASL mode, not using JDK >=1.5 SASL");

            if (alternativeSaslProvider == null)
            {
                Security.addProvider(new com.novell.sasl.client.SaslProvider());
            }
            else
            {
                try
                {
                    final Provider p = (Provider) Class.forName(
                            alternativeSaslProvider).newInstance();
                    Security.addProvider(p);
                }
                catch (final Exception e)
                {
                    throw new InitialisationException(e, this);
                }
            }

        }

        else
        {

            try
            {
                Class.forName("javax.security.sasl.Sasl");

                logger
                        .debug("Sasl implementation javax.security.sasl.Sasl (JDK >= 1.5) detected. Use it.");

                this.jdkSaslSupported = true;
            }
            catch (final ClassNotFoundException e1)
            {
                logger
                        .debug("No SunSASL implementation (JDK >= 1.5 detected. Fall back to JDK 1.4 mode");
                if (alternativeSaslProvider == null)
                {
                    Security
                            .addProvider(new com.novell.sasl.client.SaslProvider());
                }
                else
                {
                    try
                    {
                        final Provider p = (Provider) Class.forName(
                                alternativeSaslProvider).newInstance();
                        Security.addProvider(p);
                    }
                    catch (final Exception e)
                    {
                        throw new InitialisationException(e, this);
                    }
                }
            }
        }

        if (logger.isDebugEnabled())
        {

            final Provider[] ps = Security.getProviders();
            for (int i = 0; i < ps.length; i++)
            {
                final Provider provider = ps[i];
                logger.debug(provider.getClass() + "/" + provider.getName()
                        + "/" + provider.getVersion() + "/"
                        + provider.getInfo());

            }
        }

        super.doInitialise();
    }

    @Override
    protected void doBind(LDAPConnection lc) throws Exception
    {

        if (initSSL() && isStartTLS())
        {

            lc.startTLS();

        }

        logger.debug("bind with mechanism " + mechanism);

        final Map m = new HashMap();
        m.put("com.novell.security.sasl.client.pkgs",
                "org.mule.transport.ldap.sasl");
               
        lc.bind(getLoginDN(), authorizationID, new String[]
        {mechanism}, m, new BindCallbackHandler(getPassword()));

    }

    @Override
    public String getProtocol()
    {
        return "ldapsasl";
    }

    @Override
    protected boolean isAnonymousBindSupported()
    {

        return false;
    }

    public String getMechanism()
    {
        return mechanism;
    }

    public void setMechanism(final String mechanism)
    {
        this.mechanism = mechanism;
    }

    private class BindCallbackHandler implements CallbackHandler
    {
        // private final Log logger = LogFactory.getLog(getClass());
        private final char[] passwordChars;

        BindCallbackHandler(final String password)
        {

            passwordChars = new char[password.length()];
            password.getChars(0, password.length(), passwordChars, 0);
        }

        public void handle(final Callback[] callbacks) throws IOException,
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
                else if (jdkSaslSupported
                        && (callbacks[i] instanceof RealmCallback))
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
                else if (callbacks[i] instanceof javax.security.sasl.RealmCallback)
                {

                    String result = ((javax.security.sasl.RealmCallback) callbacks[i])
                            .getDefaultText();

                    if (realm != null)
                    {
                        result = realm;
                    }

                    ((javax.security.sasl.RealmCallback) callbacks[i])
                            .setText(result);

                    logger.debug(result);

                }
                else if (jdkSaslSupported
                        && (callbacks[i] instanceof RealmChoiceCallback))
                {

                    ((RealmChoiceCallback) callbacks[i]).setSelectedIndex(0);

                }
                else if (callbacks[i] instanceof javax.security.sasl.RealmChoiceCallback)
                {

                    ((javax.security.sasl.RealmChoiceCallback) callbacks[i])
                            .setSelectedIndex(0);

                }
                else
                {
                    logger.debug("Unmatched: "
                            + callbacks[i].getClass().getName());
                }
            }

        }

    }

    public boolean isUseSSL()
    {
        return useSSL;
    }

    public void setUseSSL(final boolean useSSL)
    {
        this.useSSL = useSSL;
    }

    public String getAlternativeSaslProvider()
    {
        return alternativeSaslProvider;
    }

    public void setAlternativeSaslProvider(final String alternativeSaslProvider)
    {
        this.alternativeSaslProvider = alternativeSaslProvider;
    }

    public void setForceJDK14(final boolean forceJDK14)
    {
        this.forceJDK14 = forceJDK14;
    }

    public String getRealm()
    {
        return realm;
    }

    public void setRealm(final String realm)
    {
        this.realm = realm;
    }

    public boolean isForceJDK14()
    {
        return forceJDK14;
    }

    public boolean isJdkSaslSupported()
    {
        return jdkSaslSupported;
    }

    @Override
    protected boolean initSSL()
    {
        // TODO Auto-generated method stub
        return useSSL;
    }

    public String getAuthorizationID()
    {
        return authorizationID;
    }

    public void setAuthorizationID(String authorizationID)
    {
        this.authorizationID = authorizationID;
    }

}
