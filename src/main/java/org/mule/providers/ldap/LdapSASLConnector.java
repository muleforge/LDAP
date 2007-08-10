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

import java.io.IOException;
import java.security.Provider;
import java.security.Security;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.mule.umo.lifecycle.InitialisationException;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPJSSESecureSocketFactory;
import com.novell.security.sasl.RealmCallback;
import com.novell.security.sasl.RealmChoiceCallback;

public class LdapSASLConnector extends LdapConnector
{

    private static final String MECHANISM_DIGEST_MD5 = "DIGEST-MD5";
    private static final String MECHANISM_DIGEST_EXTERNAL = "EXTERNAL";
    private boolean trustAll = false;

    private String mechanism = MECHANISM_DIGEST_MD5;

    private LDAPJSSESecureSocketFactory ssf;

    private static class BindCallbackHandler implements CallbackHandler
    {

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

                if (callbacks[i] instanceof PasswordCallback)
                {

                    ((PasswordCallback) callbacks[i])
                            .setPassword(passwordChars);

                }
                else if (callbacks[i] instanceof NameCallback)
                {

                    ((NameCallback) callbacks[i])
                            .setName(((NameCallback) callbacks[i])
                                    .getDefaultName());

                }
                else if (callbacks[i] instanceof RealmCallback)
                {

                    ((RealmCallback) callbacks[i])
                            .setText(((RealmCallback) callbacks[i])
                                    .getDefaultText());

                }
                else if (callbacks[i] instanceof RealmChoiceCallback)
                {

                    ((RealmChoiceCallback) callbacks[i]).setSelectedIndex(0);

                }

            }

        }

    }

    public LdapSASLConnector()
    {
        super();
        setLdapPort(LDAPConnection.DEFAULT_SSL_PORT);
    }

    // @Override
    protected void doBind() throws Exception
    {

        if (MECHANISM_DIGEST_EXTERNAL.equals(mechanism))
        {
            getLdapConnection().bind((String) null, (String) null, new String[]
            {mechanism}, null, (Object) null);
        }
        else
        {

            getLdapConnection().bind(getLoginDN(), "dn: " + getLoginDN(),
                    new String[]
                    {mechanism}, null, new BindCallbackHandler(getPassword()));
        }
    }

    // @Override
    protected void doInitialise() throws InitialisationException
    {

        Security.addProvider(new com.novell.sasl.client.SaslProvider());

        Provider[] ps = Security.getProviders();
        for (int i = 0; i < ps.length; i++)
        {
            Provider provider = ps[i];
            System.out.println(provider.getClass() + "/" + provider.getName()
                    + "/" + provider.getVersion() + "/" + provider.getInfo());

        }

        if (MECHANISM_DIGEST_EXTERNAL.equals(mechanism))
        {

            /*
             * try { SSLContext context = SSLContext.getInstance("TLS");
             * context.init(null,
             * trustAll?TrustAllCertsManager.trustAllCertsManager:null , null);
             * ssf = new
             * LDAPJSSESecureSocketFactory(context.getSocketFactory()); } catch
             * (Exception e) { throw new InitialisationException(e, this); }
             */

            // TODO SSL<->TLS (TLS maybe require startTLS() call on lc
            // ssf = new LDAPJSSEStartTLSFactory()
            ssf = new LDAPJSSESecureSocketFactory();
        }

        super.doInitialise();
    }

    public String getProtocol()
    {
        return "ldap";
    }

    // @Override
    protected boolean isAnonymousBindSupported()
    {
        // TODO Auto-generated method stub
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
            setLdapConnection(new LDAPConnection(ssf));
        }
        else
        {
            setLdapConnection(new LDAPConnection());
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

}
