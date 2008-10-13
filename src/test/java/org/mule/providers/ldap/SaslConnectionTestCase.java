package org.mule.providers.ldap;

import org.mule.providers.ldap.util.DSManager;
import org.mule.tck.AbstractMuleTestCase;

public class SaslConnectionTestCase extends AbstractMuleTestCase {
	


	public SaslConnectionTestCase() {
		super();
		//this.setStartContext(true);
	   
	}

	public  LdapSASLConnector getConnector(String password, String realm,
	            String mechanism, int port) throws Exception
	    {

	        LdapSASLConnector c = new LdapSASLConnector();
	        
	        c.setMuleContext(muleContext);
	        
	        c.setLdapHost("localhost");
	        c.setLdapPort(port);
	        c.setName("ldapSASLTestConnector1");
	        c.setLoginDN("hsaly");
	        c.setPassword(password);
	        c.setSearchBase("dc=example,dc=com");
	        c.setStartUnsolicitedNotificationListener(false);
	        
	        //SASL
	        
	        c.setMechanism(mechanism);
	        c.setRealm(realm);
	        c.setUseSSL(false);
	        c.setTrustAll(true);
	        c.setTrustStore(null);
	        
	        c.setForceJDK14(false);
	        c.setAlternativeSaslProvider(null);
	        return c;
	    }

	    public void testSASLDIGESTMD5Connect() throws Exception
	    {
	    	    	
	        LdapSASLConnector c = (LdapSASLConnector) getConnector("secret1",
	                "example.com", "DIGEST-MD5", 10389);
	        
	        c.initialise();
	        c.connect();
	        c.ensureConnected();
	        c.disconnect();
	        c.dispose();
	    }
	    
	   

	       public void rtestSASLEXTERNALConnect() throws Exception
	    {
	        LdapSASLConnector c = (LdapSASLConnector) getConnector("secret1",
	                "example.com", "EXTERNAL", 10389);
	        c.initialise();
	        c.connect();
	        c.ensureConnected();
	        c.disconnect();
	        c.dispose();
	    }
	       

	       public void rtestSASLPLAINConnect() throws Exception
	    {
	        LdapSASLConnector c = (LdapSASLConnector) getConnector("secret1",
	                "example.com", "PLAIN", 10389);
	        c.initialise();
	        c.connect();
	        c.ensureConnected();
	        c.disconnect();
	        c.dispose();
	    }
	       
	       public void etestSASLCRAMConnect() throws Exception
		    {
		        LdapSASLConnector c = (LdapSASLConnector) getConnector("secret1",
		                "example.com", "CRAM-MD5", 10389);
		        c.initialise();
		        c.connect();
		        c.ensureConnected();
		        c.disconnect();
		        c.dispose();
		    }
	
	    public void testSASLBadPassword() throws Exception
	    {
	    	
	    	
	    	
	        try
	        {
	        	 
	            LdapSASLConnector c =  getConnector("xxx","example.com", "DIGEST-MD5", 10389);
	         
	            
	    		c.connect();
	    		
	    		
	    		assertTrue(c.isConnected());
	    		assertTrue(c.isStarted());
	    		//c.doAsyncRequest(null);
	            c.ensureConnected();
	            c.disconnect();
	            c.dispose();
	            fail();
	        }
	        catch (Exception e)
	        {
	            // excpected
	        	
	        }
	        
	        
	    }

	    public void testUnknownMechanism() throws Exception
	    {
	        try
	        {
	        	
	            LdapSASLConnector c =  getConnector("secret1","example.com","unkjj",10389);
	            
	            
	    		c.connect();
	    		
	    		
	    		assertTrue(c.isConnected());
	    		assertTrue(c.isStarted());
	    	
	            c.ensureConnected();
	            c.disconnect();
	            c.dispose();
	            fail();

	        }
	        catch (Exception e)
	        {
	            // excpected
	        }
	    }
	 
	 
	 @Override
		protected void doSetUp() throws Exception {
		 
			DSManager.getInstance().start();
			super.doSetUp();
			

		}

		@Override
		protected void doTearDown() throws Exception {
			super.doTearDown();;
			DSManager.getInstance().stop();
			

		}


}
