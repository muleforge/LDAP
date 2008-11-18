package org.mule.transport.ldap.config;

import org.mule.tck.FunctionalTestCase;
import org.mule.transport.ldap.LdapSConnector;
import org.mule.transport.ldap.util.DSManager;

public class LdapSNamespaceHandlerTestCase extends FunctionalTestCase
{

    @Override
    protected String getConfigResources()
    {
        return "ldaps-namespace-config.xml";
    }

    public void testNormal() throws Exception
    {
        final LdapSConnector c = (LdapSConnector) muleContext.getRegistry()
                .lookupConnector("ldapConnector1");
        assertNotNull(c);
        assertNotNull(c.getQueries());
        assertTrue(c.getQueries().size() == 2);
        assertNotNull(c.getAttributes());
        assertNotNull(c.getPsFilters());

        assertFalse(c.isStartTLS());

        System.out.println(c.getAttributes());
        System.out.println(c.getPsFilters());

        assertTrue(c.getAttributesAsArray().length == 2);
        assertTrue(c.getpsFiltersAsArray().length == 2);

        assertTrue(c.isConnected());
        assertTrue(c.isStarted());

    }

    /*
     * public void testWithDataSourceViaJndi() throws Exception { LdapConnector
     * c = (LdapConnector)
     * muleContext.getRegistry().lookupConnector("ldapConnector2");
     * assertNotNull(c);
     * 
     * 
     * assertNull(c.getQueries()); assertTrue(c.isConnected());
     * assertTrue(c.isStarted()); }
     * 
     * public void testFullyConfigured() throws Exception { LdapConnector c =
     * (LdapConnector)
     * muleContext.getRegistry().lookupConnector("ldapConnector3");
     * assertNotNull(c);
     * 
     * 
     * 
     * assertNotNull(c.getQueries()); assertEquals(3, c.getQueries().size());
     * 
     * assertTrue(c.isConnected()); assertTrue(c.isStarted()); }
     * 
     * 
     * public void testEndpointQueryOverride() throws Exception { LdapConnector
     * c = (LdapConnector)
     * muleContext.getRegistry().lookupConnector("ldapConnector3");
     * ImmutableEndpoint testLdapEndpoint = muleContext.getRegistry()
     * .lookupEndpointFactory() .getInboundEndpoint("testLdapEndpoint");
     * 
     * //On connector, not overridden assertNotNull(c.getQuery(testLdapEndpoint,
     * "getTest"));
     * 
     * //On connector, overridden on endpoint
     * assertNotNull(c.getQuery(testLdapEndpoint, "getTest2"));
     * assertEquals("OVERRIDDEN VALUE", c.getQuery(testLdapEndpoint,
     * "getTest2"));
     * 
     * //Only on endpoint assertNotNull(c.getQuery(testLdapEndpoint,
     * "getTest3"));
     * 
     * //Does not exist on either assertNull(c.getQuery(testLdapEndpoint,
     * "getTest4")); }
     * 
     * public void testEndpointWithTransaction() throws Exception {
     * ImmutableEndpoint endpoint = muleContext.getRegistry().
     * lookupEndpointBuilder("endpointWithTransaction").buildInboundEndpoint();
     * assertNotNull(endpoint);
     * 
     * assertEquals(MuleTransactionConfig.ACTION_NONE,
     * endpoint.getTransactionConfig().getAction()); }
     * 
     * public void testEndpointWithXaTransaction() throws Exception {
     * ImmutableEndpoint endpoint = muleContext.getRegistry().
     * lookupEndpointBuilder("endpointWithXaTransaction").buildInboundEndpoint();
     * assertNotNull(endpoint); assertEquals(XaTransactionFactory.class,
     * endpoint.getTransactionConfig().getFactory().getClass());
     * assertEquals(MuleTransactionConfig.ACTION_ALWAYS_BEGIN,
     * endpoint.getTransactionConfig().getAction()); }
     */

    @Override
    protected void doSetUp() throws Exception
    {
        // TODO Auto-generated method stub
        super.doSetUp();
        System.out.println("doSetUp()");
    }

    @Override
    protected void suitePreSetUp() throws Exception
    {
        // TODO Auto-generated method stub
        super.suitePreSetUp();
        System.out.println("suitePreSetUp()");
        DSManager.getInstance().start(true);
    }
}
