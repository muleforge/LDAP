package org.mule.providers.ldap.util;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class TrustAllCertsManager implements X509TrustManager
{

    public static final TrustManager[] trustAllCertsManager = new TrustManager[]
    { new TrustAllCertsManager() };

    public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException
    {

    }

    public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException
    {

    }

    public X509Certificate[] getAcceptedIssuers()
    {

        return null;
    }

}
