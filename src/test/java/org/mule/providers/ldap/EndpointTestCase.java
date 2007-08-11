package org.mule.providers.ldap;

import java.util.HashMap;
import java.util.Map;

import org.mule.impl.endpoint.MuleEndpoint;
import org.mule.impl.endpoint.MuleEndpointURI;
import org.mule.providers.ldap.util.LDAPUtils;
import org.mule.tck.AbstractMuleTestCase;
import org.mule.umo.endpoint.MalformedEndpointException;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.provider.UMOConnector;

public class EndpointTestCase extends AbstractMuleTestCase
{

    // running

    public void testInboundUrl() throws Exception
    {
        UMOEndpointURI url = new MuleEndpointURI("ldap://ldap.in");
        assertEquals("ldap", url.getScheme());
        assertEquals("ldap.in", url.getAddress());
        assertEquals("ldap://ldap.in", url.toString());
    }

    public void testIncompleteInboundUrl() throws Exception
    {

        try
        {
            new MuleEndpointURI("ldap://ldap.i");
            fail();
        }
        catch (MalformedEndpointException e)
        {

            assertTrue(true);
        }

    }

    public void testInboundUrlWithParamaters() throws Exception
    {

        try
        {
            new MuleEndpointURI("ldap://ldap.in/xxx");
            fail();
        }
        catch (MalformedEndpointException e)
        {

            assertTrue(true);
        }

    }

    public void testIncompleteOutboundUrl() throws Exception
    {

        try
        {
            new MuleEndpointURI("ldap://ldapout");
            fail();
        }
        catch (MalformedEndpointException e)
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
        catch (MalformedEndpointException e)
        {

            assertTrue(true);
        }

    }

    public void testOutboundUrl() throws Exception
    {
        UMOEndpointURI url = new MuleEndpointURI("ldap://ldap.out");
        assertEquals("ldap", url.getScheme());
        assertEquals("ldap.out", url.getAddress());
        assertEquals("ldap://ldap.out", url.toString());
    }

    public void testOutboundUrlWithQuery() throws Exception
    {
        UMOEndpointURI url = new MuleEndpointURI("ldap://ldap.out/payload.dc");
        assertEquals("ldap", url.getScheme());
        assertEquals("ldap.out/payload.dc", url.getAddress());
        assertEquals("ldap://ldap.out/payload.dc", url.toString());
        assertEquals("/payload.dc", url.getPath());
    }
    
    public void testSearchStringFromEndpoint1() throws Exception
    {
        UMOEndpointURI url = new MuleEndpointURI("ldap://ldap.out/(cn=*)");
        UMOConnector connector = getConnector();
        UMOEndpoint endpoint = new MuleEndpoint("testendpoint", url, connector,null,UMOEndpoint.ENDPOINT_TYPE_SENDER_AND_RECEIVER,0,null,null);
        
        String searchStr = LDAPUtils.getSearchStringFromEndpoint(endpoint, "test");
        
        assertEquals("(cn=*)", searchStr);
        
    }
    
    public void testSearchStringFromEndpoint2() throws Exception
    {
        UMOEndpointURI url = new MuleEndpointURI("ldap://ldap.out/payload.cn");
        UMOConnector connector = getConnector();
        UMOEndpoint endpoint = new MuleEndpoint("testendpoint", url, connector,null,UMOEndpoint.ENDPOINT_TYPE_SENDER_AND_RECEIVER,0,null,null);
        
        String searchStr = LDAPUtils.getSearchStringFromEndpoint(endpoint, "test");
        
        assertEquals("(cn=test)", searchStr);
        
    }
    
    public void testSearchStringFromEndpoint3() throws Exception
    {
        UMOEndpointURI url = new MuleEndpointURI("ldap://ldap.out/");
        UMOConnector connector = getConnector();
        UMOEndpoint endpoint = new MuleEndpoint("testendpoint", url, connector,null,UMOEndpoint.ENDPOINT_TYPE_SENDER_AND_RECEIVER,0,null,null);
        
        String searchStr = LDAPUtils.getSearchStringFromEndpoint(endpoint, "test");
        
        assertEquals("test", searchStr);
        
    }
    
    public void testSearchStringFromEndpoint4() throws Exception
    {
        UMOEndpointURI url = new MuleEndpointURI("ldap://ldap.out");
        UMOConnector connector = getConnector();
        UMOEndpoint endpoint = new MuleEndpoint("testendpoint", url, connector,null,UMOEndpoint.ENDPOINT_TYPE_SENDER_AND_RECEIVER,0,null,null);
        
        String searchStr = LDAPUtils.getSearchStringFromEndpoint(endpoint, "test");
        
        assertEquals("test", searchStr);
        
    }
    
    public void testSearchStringFromEndpoint5() throws Exception
    {
        UMOEndpointURI url = new MuleEndpointURI("ldap://ldap.out/payload.noquery");
        UMOConnector connector = getConnector();
        UMOEndpoint endpoint = new MuleEndpoint("testendpoint", url, connector,null,UMOEndpoint.ENDPOINT_TYPE_SENDER_AND_RECEIVER,0,null,null);
        
        String searchStr = LDAPUtils.getSearchStringFromEndpoint(endpoint, "test");
        
        assertEquals("payload.noquery", searchStr);
        
    }
    
    public void testSearchStringFromEndpointOverrideProps() throws Exception
    {
        UMOEndpointURI url = new MuleEndpointURI("ldap://ldap.out/payload.cn");
        UMOConnector connector = getConnector();
        
        Map map = new HashMap();
        Map inner = new HashMap();
        inner.put("payload.cn", "(objectclass=${payload})");
        map.put("queries", inner);
        
        UMOEndpoint endpoint = new MuleEndpoint("testendpoint", url, connector,null,UMOEndpoint.ENDPOINT_TYPE_SENDER_AND_RECEIVER,0,null,map);
        
        
        String searchStr = LDAPUtils.getSearchStringFromEndpoint(endpoint, "test");
        
        assertEquals("(objectclass=test)", searchStr);
        
    }
    
    public void testSearchStringFromEndpointOverrideProps2() throws Exception
    {
        UMOEndpointURI url = new MuleEndpointURI("ldap://ldap.out/payload.cn?cnprop=testprop");
        UMOConnector connector = getConnector();
        
        Map map = new HashMap();
        Map inner = new HashMap();
        inner.put("payload.cn", "(sn=${name}, cn=${cnprop})");
        map.put("queries", inner);
        
        UMOEndpoint endpoint = new MuleEndpoint("testendpoint", url, connector,null,UMOEndpoint.ENDPOINT_TYPE_SENDER_AND_RECEIVER,0,null,map);
                
        String searchStr = LDAPUtils.getSearchStringFromEndpoint(endpoint, new User("john"));
        
        assertEquals("(sn=john, cn=testprop)", searchStr);
        
    }
    
    public UMOConnector getConnector() throws Exception
    {

        LdapConnector c = new LdapConnector();
        c.setLdapHost("localhost");
        c.setLdapPort(10389);
        c.setName("ldapTestConnector");

        c.setLoginDN("uid=admin,ou=system");
        c.setPassword("secret");

        c.setSearchBase("o=sevenSeas");
        
        Map map = new HashMap();
        map.put("payload.cn", "(cn=${payload})");
        
        c.setQueries(map);
        
        c.initialise();

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
