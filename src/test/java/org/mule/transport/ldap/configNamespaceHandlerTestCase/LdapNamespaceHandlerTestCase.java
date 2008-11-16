package org.mule.transport.ldap.configNamespaceHandlerTestCase;

import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.tck.FunctionalTestCase;
import org.mule.transaction.MuleTransactionConfig;
import org.mule.transaction.XaTransactionFactory;
import org.mule.transport.ldap.LdapConnector;

public class LdapNamespaceHandlerTestCase extends FunctionalTestCase
{
    
    protected String getConfigResources()
    {
        return "ldap-namespace-config.xml";
    }

    public void testWithDataSource() throws Exception
    {
        LdapConnector c = (LdapConnector) muleContext.getRegistry().lookupConnector("ldapConnector1");
        assertNotNull(c);        

        assertNull(c.getQueries());       
    }

    public void testWithDataSourceViaJndi() throws Exception
    {
        LdapConnector c = (LdapConnector) muleContext.getRegistry().lookupConnector("ldapConnector2");
        assertNotNull(c);
        
      
        assertNull(c.getQueries());
        assertTrue(c.isConnected());
        assertTrue(c.isStarted());
    }
    
    public void testFullyConfigured() throws Exception
    {
        LdapConnector c = (LdapConnector) muleContext.getRegistry().lookupConnector("ldapConnector3");
        assertNotNull(c);
        
     
        
        assertNotNull(c.getQueries());
        assertEquals(3, c.getQueries().size());
        
        assertTrue(c.isConnected());
        assertTrue(c.isStarted());
    }
    
    
    public void testEndpointQueryOverride() throws Exception
    {
        LdapConnector c = (LdapConnector) muleContext.getRegistry().lookupConnector("ldapConnector3");
        ImmutableEndpoint testLdapEndpoint = muleContext.getRegistry()
            .lookupEndpointFactory()
            .getInboundEndpoint("testLdapEndpoint");
        
        //On connector, not overridden
        assertNotNull(c.getQuery(testLdapEndpoint, "getTest"));
        
        //On connector, overridden on endpoint
        assertNotNull(c.getQuery(testLdapEndpoint, "getTest2"));
        assertEquals("OVERRIDDEN VALUE", c.getQuery(testLdapEndpoint, "getTest2"));
        
        //Only on endpoint
        assertNotNull(c.getQuery(testLdapEndpoint, "getTest3"));

        //Does not exist on either
        assertNull(c.getQuery(testLdapEndpoint, "getTest4"));
    }
    
    public void testEndpointWithTransaction() throws Exception
    {
        ImmutableEndpoint endpoint = muleContext.getRegistry().
            lookupEndpointBuilder("endpointWithTransaction").buildInboundEndpoint();
        assertNotNull(endpoint);
       
        assertEquals(MuleTransactionConfig.ACTION_NONE, 
            endpoint.getTransactionConfig().getAction());
    }
    
    public void testEndpointWithXaTransaction() throws Exception
    {
        ImmutableEndpoint endpoint = muleContext.getRegistry().
            lookupEndpointBuilder("endpointWithXaTransaction").buildInboundEndpoint();
        assertNotNull(endpoint);
        assertEquals(XaTransactionFactory.class, 
            endpoint.getTransactionConfig().getFactory().getClass());
        assertEquals(MuleTransactionConfig.ACTION_ALWAYS_BEGIN, 
            endpoint.getTransactionConfig().getAction());
    }

}

