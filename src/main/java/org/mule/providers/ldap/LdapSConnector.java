package org.mule.providers.ldap;

import java.io.File;

import javax.net.ssl.SSLContext;

import org.mule.providers.ldap.util.TrustAllCertsManager;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.util.StringUtils;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPJSSESecureSocketFactory;

public class LdapSConnector extends LdapConnector
{

    public static final String PROPERTY_TRUST_ALL = "trustAll";

    protected LDAPJSSESecureSocketFactory ssf = null;

    protected boolean trustAll = false;
    protected String trustStore = null;

    public String getTrustStore()
    {
        return trustStore;
    }

    public void setTrustStore(String trustStore)
    {
        this.trustStore = trustStore;
    }

    public LdapSConnector()
    {
        super();
        this.ldapPort = LDAPConnection.DEFAULT_SSL_PORT;

    }

    public String getProtocol()
    {
        return "ldaps";
    }

    // @Override
    protected void doInitialise() throws InitialisationException
    {

        try
        {

            if (trustAll)
            {
                SSLContext context = SSLContext.getInstance("TLS");
                context.init(null,
                        trustAll ? TrustAllCertsManager.trustAllCertsManager
                                : null, null);

                // certificate_unknown
                ssf = new LDAPJSSESecureSocketFactory(context
                        .getSocketFactory());
            } else
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
                                    + " do not exist or is not readable"), this);
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

        } catch (Exception e)
        {
            // TODO Auto-generated catch block

            throw new InitialisationException(e, this);
        }

        // super.setSsf(ssf);

        // TODO Auto-generated method stub
        super.doInitialise();
    }

    public boolean isTrustAll()
    {
        return trustAll;
    }

    public void setTrustAll(boolean trustAll)
    {
        this.trustAll = trustAll;
    }

    // @Override
    protected boolean isAnonymousBindSupported()
    {
        // TODO Auto-generated method stub
        return false;
    }

    // @Override
    protected void setLDAPConnection()
    {
        ldapConnection = new LDAPConnection(ssf);
    }

}
