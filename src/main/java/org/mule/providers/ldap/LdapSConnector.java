package org.mule.providers.ldap;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.mule.umo.lifecycle.InitialisationException;

import com.novell.ldap.LDAPJSSESecureSocketFactory;


public class LdapSConnector extends LdapConnector{

	public static final String PROPERTY_TRUST_ALL = "trustAll";
	
	protected  LDAPJSSESecureSocketFactory ssf = null;
	
	protected boolean trustAll = false; 
	
	public LdapSConnector()
    {
        super(true);
        
    }
    
    public String getProtocol()
    {
        return "ldaps";
    }

	@Override
	protected void doInitialise() throws InitialisationException {
		
		try {
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(null, trustAll?trustAllCerts:null , null);
			ssf = new LDAPJSSESecureSocketFactory(context.getSocketFactory());
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		//pix path
		//ssf = new LDAPJSSESecureSocketFactory((SSLSocketFactory) SSLSocketFactory.getDefault());
		
		super.setSsf(ssf);
		
		// TODO Auto-generated method stub
		super.doInitialise();
	}

	public boolean isTrustAll() {
		return trustAll;
	}

	public void setTrustAll(boolean trustAll) {
		this.trustAll = trustAll;
	}
    
	protected TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager()
    {

		public void checkClientTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
			// TODO Auto-generated method stub
			
		}

		public void checkServerTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
			// TODO Auto-generated method stub
			
		}

		public X509Certificate[] getAcceptedIssuers() {
			// TODO Auto-generated method stub
			return null;
		}
        
		
    } };
	



    
    
    

}
