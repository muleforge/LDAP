package org.mule.providers.ldap.functional;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.mule.RegistryContext;
import org.mule.api.endpoint.EndpointException;
import org.mule.api.endpoint.EndpointURI;
import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.api.transport.Connector;
import org.mule.endpoint.DefaultOutboundEndpoint;
import org.mule.endpoint.MuleEndpointURI;
import org.mule.providers.ldap.LdapConnector;
import org.mule.providers.ldap.util.LDAPUtils;
import org.mule.tck.AbstractMuleTestCase;

public class EndpointTestCase extends AbstractMuleTestCase
{

    // running

    public void testInboundUrl() throws Exception
    {
        EndpointURI url = new MuleEndpointURI("ldap://ldap.in");
        url.initialise();
        
        assertEquals("ldap", url.getScheme());
        assertEquals("ldap.in", url.getAddress());
        assertEquals("ldap://ldap.in", url.toString());
    }

    public void testIncompleteInboundUrl() throws Exception
    {

        try
        {
            new MuleEndpointURI("ldap://ldap.i").initialise();
            fail();
        }
        catch (Exception e)
        {

            assertTrue(true);
        }

    }

    public void testInboundUrlWithParamaters() throws Exception
    {

        try
        {
            new MuleEndpointURI("ldap://ldap.in/xxx").initialise();
            fail();
        }
        catch (Exception e)
        {

            assertTrue(true);
        }

    }

    public void testIncompleteOutboundUrl() throws Exception
    {

        try
        {
            new MuleEndpointURI("ldap://ldapout").initialise();
            fail();
        }
        catch (Exception e)
        {

            assertTrue(true);
        }

    }

    public void testBadParamatersWithDollarSign() throws Exception
    {

        try
        {
            new MuleEndpointURI("ldap://ldap.out/(cn=${payload})");
            fail();
        }
        catch (EndpointException e)
        {

            assertTrue(true);
        }

    }

    public void testOutboundUrl() throws Exception
    {
    	EndpointURI url = new MuleEndpointURI("ldap://ldap.out");
    	url.initialise();
        assertEquals("ldap", url.getScheme());
        assertEquals("ldap.out", url.getAddress());
        assertEquals("ldap://ldap.out", url.toString());
    }

    public void testOutboundUrlWithQuery() throws Exception
    {
    	EndpointURI url = new MuleEndpointURI("ldap://ldap.out/payload.dc");
    	url.initialise();
        assertEquals("ldap", url.getScheme());
        assertEquals("ldap.out/payload.dc", url.getAddress());
        assertEquals("ldap://ldap.out/payload.dc", url.toString());
        assertEquals("/payload.dc", url.getPath());
    }

    public void testSearchStringFromEndpoint1() throws Exception
    {
        EndpointURI url = new MuleEndpointURI("ldap://ldap.out/(cn=*)");
        url.initialise();
        Connector connector = getConnector();
       ImmutableEndpoint endpoint = new DefaultOutboundEndpoint(connector,url,null,null,"testendpoint",new Properties(),
    		   null,
    		   null,
              true,
              null,
               false,
               false,
               0,
               null,
               null,
               null,
               null);
    	   
    	   
    	  

        String searchStr = LDAPUtils.getSearchStringFromEndpoint(endpoint,
                "test");

        assertEquals("(cn=*)", searchStr);

    }

    public void testSearchStringFromEndpoint2() throws Exception
    {
        EndpointURI url = new MuleEndpointURI("ldap://ldap.out/payload.cn");
        url.initialise();
        Connector connector = getConnector();
        ImmutableEndpoint endpoint = new DefaultOutboundEndpoint(connector,url,null,null,"testendpoint",new Properties(),
     		   null,
     		   null,
               true,
               null,
                false,
                false,
                0,
                null,
                null,
                null,
                null);
     	   

        String searchStr = LDAPUtils.getSearchStringFromEndpoint(endpoint,
                "test");

        assertEquals("(cn=test)", searchStr);

    }

    public void testSearchStringFromEndpoint3() throws Exception
    {
        EndpointURI url = new MuleEndpointURI("ldap://ldap.out/");
        url.initialise();
        Connector connector = getConnector();
        ImmutableEndpoint endpoint = new DefaultOutboundEndpoint(connector,url,null,null,"testendpoint",new Properties(),
     		   null,
     		   null,
               true,
               null,
                false,
                false,
                0,
                null,
                null,
                null,
                null);
     	   

        String searchStr = LDAPUtils.getSearchStringFromEndpoint(endpoint,
                "test");

        assertEquals("test", searchStr);

    }

    public void testSearchStringFromEndpoint4() throws Exception
    {
        EndpointURI url = new MuleEndpointURI("ldap://ldap.out");
        url.initialise();
        Connector connector = getConnector();
        ImmutableEndpoint endpoint = new DefaultOutboundEndpoint(connector,url,null,null,"testendpoint",new Properties(),
     		   null,
     		   null,
               true,
               null,
                false,
                false,
                0,
                null,
                null,
                null,
                null);
     	   

        String searchStr = LDAPUtils.getSearchStringFromEndpoint(endpoint,
                "test");

        assertEquals("test", searchStr);

    }

    public void testSearchStringFromEndpoint5() throws Exception
    {
        EndpointURI url = new MuleEndpointURI(
                "ldap://ldap.out/payload.noquery");
                url.initialise();
        Connector connector = getConnector();
        ImmutableEndpoint endpoint = new DefaultOutboundEndpoint(connector,url,null,null,"testendpoint",new Properties(),
     		   null,
     		   null,
               true,
               null,
                false,
                false,
                0,
                null,
                null,
                null,
                null);
     	   

        String searchStr = LDAPUtils.getSearchStringFromEndpoint(endpoint,
                "test");

        assertEquals("payload.noquery", searchStr);

    }

    public void testSearchStringFromEndpointOverrideProps() throws Exception
    {
        EndpointURI url = new MuleEndpointURI("ldap://ldap.out/payload.cn");
        url.initialise();
        Connector connector = getConnector();

        Map map = new HashMap();
        Map inner = new HashMap();
        inner.put("payload.cn", "(objectclass=${payload})");
        map.put("queries", inner);

        ImmutableEndpoint endpoint = new DefaultOutboundEndpoint(connector,url,null,null,"testendpoint",map,
     		   null,
     		   null,
               true,
               null,
                false,
                false,
                0,
                null,
                null,
                null,
                null);
     	   

        String searchStr = LDAPUtils.getSearchStringFromEndpoint(endpoint,
                "test");

        assertEquals("(objectclass=test)", searchStr);

    }

    public void testSearchStringFromEndpointOverrideProps2() throws Exception
    {
        EndpointURI url = new MuleEndpointURI(
                "ldap://ldap.out/payload.cn?cnprop=testprop");
     
        
                url.initialise();
        Connector connector = getConnector();
        
        /*if(!ExpressionEvaluatorManager.isEvaluatorRegistered(EndpointURIExpressionEvaluator.NAME))
		{
			ExpressionEvaluatorManager.registerEvaluator(new EndpointURIExpressionEvaluator());
		}*/
        
		//System.out.println("isEvaluatorRegistered: "+ExpressionEvaluatorManager.isEvaluatorRegistered("endpointuri"));

        Map map = new HashMap();
        Map inner = new HashMap();
        inner.put("payload.cn", "(sn=${bean:name}, cn=${endpointuri:testendpoint.params:cnprop})");
        map.put("queries", inner);

        ImmutableEndpoint endpoint = new DefaultOutboundEndpoint(connector,url,null,null,"testendpoint",map,
     		   null,
     		   null,
               true,
               null,
                false,
                false,
                0,
                null,
                null,
                muleContext,
                null);
     	
        //EndpointURIEndpointBuilder b = new EndpointURIEndpointBuilder(endpoint,muleContext);
        
        RegistryContext.getRegistry().registerEndpoint(endpoint);
        //RegistryContext.getRegistry().registerEndpointBuilder("teb", b);
        
        //EndpointInfoExpressionEvaluator

        String searchStr = LDAPUtils.getSearchStringFromEndpoint(endpoint,
                new User("john"));

        assertEquals("(sn=john, cn=testprop)", searchStr);

    }

    public Connector getConnector() throws Exception
    {

        LdapConnector c = new LdapConnector();
        c.setMuleContext(muleContext);
        c.setLdapHost("localhost");
        c.setLdapPort(10389);
        c.setName("ldapTestConnector");

        c.setLoginDN("uid=admin,ou=system");
        c.setPassword("secret");

        c.setSearchBase("o=sevenSeas");

        Map map = new HashMap();
        map.put("payload.cn", "(cn=${payload})");

        c.setQueries(map);

        RegistryContext.getRegistry().registerConnector(c);

        return c;
    }

    public static final class User
    {
        private String name = "default";

        public User(String name)
        {
            setName(name);
        }

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }
    }

}
