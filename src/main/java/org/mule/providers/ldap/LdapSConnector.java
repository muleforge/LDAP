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
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

import org.mule.api.lifecycle.InitialisationException;
import org.mule.providers.ldap.util.TrustAllCertsManager;
import org.mule.util.StringUtils;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPJSSESecureSocketFactory;
import com.novell.ldap.LDAPJSSEStartTLSFactory;
import com.novell.ldap.LDAPSocketFactory;

public class LdapSConnector extends LdapConnector
{

    protected  LDAPSocketFactory ssf = null;

    protected boolean trustAll = false;
    protected String trustStore = null;
    protected boolean startTLS = false;

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
        setLdapPort(LDAPConnection.DEFAULT_SSL_PORT);

    }

    public String getProtocol()
    {
        return "ldaps";
    }

    
    protected void setupSSL() throws InitialisationException
    {
    	 try
         {
         	logger.debug("trustAll: "+trustAll);
         	logger.debug("trustStore: "+trustStore);
             if (trustAll)
             {
                 SSLContext context = SSLContext.getInstance("TLS");
                 context.init(null, trustAll ? TrustAllCertsManager
                         .getTrustAllCertsManager() : null, null);

                 // certificate_unknown
              
                 if(startTLS)
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

                 System.setProperty("javax.net.ssl.trustStore",trustStoreFile.getAbsolutePath());
                 //System.setProperty ( "javax.net.ssl.keyStore",trustStoreFile.getAbsolutePath() );
                 //System.setProperty ( "javax.net.ssl.keyStorePassword", "changeit" );

                 logger.debug("truststore set to "
                         + trustStoreFile.getAbsolutePath());
                 if(startTLS)
                 {
                 	 ssf = new LDAPJSSEStartTLSFactory();
                 }
                 else
                 {
                 	ssf = new LDAPJSSESecureSocketFactory();
                 }
             }
            
         }
         catch (KeyManagementException e)
         {
             throw new InitialisationException(e, this);
         }
         catch (NoSuchAlgorithmException e)
         {
             throw new InitialisationException(e, this);
         }

         // super.setSsf(ssf);
    }
    
    protected boolean initSSL()
    {
    	return true;
    }
    
    // @Override
    protected void doInitialise() throws InitialisationException
    {

       if(initSSL())
       {
    	   setupSSL();
       }

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

        return false;
    }

    // @Override
    protected void setLDAPConnection()
    {
    	LDAPConnection c = null;
    	
    	if(initSSL())
    	{
    		c = new LDAPConnection(ssf);
    		

        	if (startTLS)
        	try {
    			c.startTLS();
    		} catch (LDAPException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    		
    	}
    	else
    	{
    		c = new LDAPConnection();
    	}
    	
        setLdapConnection(c);
    }

	public boolean isStartTLS() {
		return startTLS;
	}

	public void setStartTLS(boolean startTLS) {
		this.startTLS = startTLS;
	}

}
