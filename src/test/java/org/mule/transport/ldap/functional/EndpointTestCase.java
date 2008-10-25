package org.mule.transport.ldap.functional;

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
import org.mule.retry.policies.NoRetryPolicyTemplate;
import org.mule.tck.AbstractMuleTestCase;
import org.mule.transport.ldap.LdapConnector;
import org.mule.transport.ldap.util.LDAPUtils;

public class EndpointTestCase extends AbstractMuleTestCase
{

    // running

    public void testInboundUrl() throws Exception
    {
        final EndpointURI url = new MuleEndpointURI("ldap://ldap.in");
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
        catch (final Exception e)
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
        catch (final Exception e)
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
        catch (final Exception e)
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
        catch (final EndpointException e)
        {

            assertTrue(true);
        }

    }

    public void testBadParamatersWithDollarSign2() throws Exception
    {

        try
        {
            new MuleEndpointURI("ldap://ldap.out/(cn=$[payload:])");
            fail();
        }
        catch (final EndpointException e)
        {

            assertTrue(true);
        }

    }

    public void testValidParamatersWithHashSign() throws Exception
    {

        try
        {
            // FIXME
            new MuleEndpointURI("ldap://ldap.out/(cn=#[payload:])");

        }
        catch (final EndpointException e)
        {
            fail();

        }

    }

    public void testOutboundUrl() throws Exception
    {
        final EndpointURI url = new MuleEndpointURI("ldap://ldap.out");
        url.initialise();
        assertEquals("ldap", url.getScheme());
        assertEquals("ldap.out", url.getAddress());
        assertEquals("ldap://ldap.out", url.toString());
    }

    public void testOutboundUrlWithQuery() throws Exception
    {
        final EndpointURI url = new MuleEndpointURI(
                "ldap://ldap.out/payload.dc");
        url.initialise();
        assertEquals("ldap", url.getScheme());
        assertEquals("ldap.out/payload.dc", url.getAddress());
        assertEquals("ldap://ldap.out/payload.dc", url.toString());
        assertEquals("/payload.dc", url.getPath());
    }

    public void testSearchStringFromEndpoint1() throws Exception
    {
        final EndpointURI url = new MuleEndpointURI("ldap://ldap.out/(cn=*)");
        url.initialise();
        final Connector connector = getConnector();
        final ImmutableEndpoint endpoint = new DefaultOutboundEndpoint(
                connector, url, null, null, "testendpoint", new Properties(),
                null, null, true, null, false, false, 0, null, null, null,
                null, new NoRetryPolicyTemplate());

        final String searchStr = LDAPUtils.getSearchStringFromEndpoint(
                endpoint, "test");

        assertEquals("(cn=*)", searchStr);

    }

    public void testSearchStringFromEndpoint2() throws Exception
    {
        final EndpointURI url = new MuleEndpointURI(
                "ldap://ldap.out/payload.cn");
        url.initialise();
        final Connector connector = getConnector();
        final ImmutableEndpoint endpoint = new DefaultOutboundEndpoint(
                connector, url, null, null, "testendpoint", new Properties(),
                null, null, true, null, false, false, 0, null, null, null,
                null, new NoRetryPolicyTemplate());

        final String searchStr = LDAPUtils.getSearchStringFromEndpoint(
                endpoint, "test");

        assertEquals("(cn=test)", searchStr);

    }

    public void testSearchStringFromEndpoint3() throws Exception
    {
        final EndpointURI url = new MuleEndpointURI("ldap://ldap.out/");
        url.initialise();
        final Connector connector = getConnector();
        final ImmutableEndpoint endpoint = new DefaultOutboundEndpoint(
                connector, url, null, null, "testendpoint", new Properties(),
                null, null, true, null, false, false, 0, null, null, null,
                null, new NoRetryPolicyTemplate());

        final String searchStr = LDAPUtils.getSearchStringFromEndpoint(
                endpoint, "test");

        assertEquals("test", searchStr);

    }

    public void testSearchStringFromEndpoint4() throws Exception
    {
        final EndpointURI url = new MuleEndpointURI("ldap://ldap.out");
        url.initialise();
        final Connector connector = getConnector();
        final ImmutableEndpoint endpoint = new DefaultOutboundEndpoint(
                connector, url, null, null, "testendpoint", new Properties(),
                null, null, true, null, false, false, 0, null, null, null,
                null, new NoRetryPolicyTemplate());

        final String searchStr = LDAPUtils.getSearchStringFromEndpoint(
                endpoint, "test");

        assertEquals("test", searchStr);

    }

    public void testSearchStringFromEndpoint5() throws Exception
    {
        final EndpointURI url = new MuleEndpointURI(
                "ldap://ldap.out/payload.noquery");
        url.initialise();
        final Connector connector = getConnector();
        final ImmutableEndpoint endpoint = new DefaultOutboundEndpoint(
                connector, url, null, null, "testendpoint", new Properties(),
                null, null, true, null, false, false, 0, null, null, null,
                null, new NoRetryPolicyTemplate());

        final String searchStr = LDAPUtils.getSearchStringFromEndpoint(
                endpoint, "test");

        assertEquals("payload.noquery", searchStr);

    }

    public void testSearchStringFromEndpointOverrideProps() throws Exception
    {
        final EndpointURI url = new MuleEndpointURI(
                "ldap://ldap.out/payload.cn");
        url.initialise();
        final Connector connector = getConnector();

        final Map map = new HashMap();
        final Map inner = new HashMap();
        inner.put("payload.cn", "(objectclass=#[payload:])");
        map.put("queries", inner);

        final ImmutableEndpoint endpoint = new DefaultOutboundEndpoint(
                connector, url, null, null, "testendpoint", map, null, null,
                true, null, false, false, 0, null, null, null, null,
                new NoRetryPolicyTemplate());

        final String searchStr = LDAPUtils.getSearchStringFromEndpoint(
                endpoint, "test");

        assertEquals("(objectclass=test)", searchStr);

    }

    public void testSearchStringFromEndpointOverrideProps2() throws Exception
    {
        final EndpointURI url = new MuleEndpointURI(
                "ldap://ldap.out/payload.cn?cnprop=testprop");

        url.initialise();
        final Connector connector = getConnector();

        /*
         * if(!ExpressionEvaluatorManager.isEvaluatorRegistered(EndpointURIExpressionEvaluator.NAME)) {
         * ExpressionEvaluatorManager.registerEvaluator(new
         * EndpointURIExpressionEvaluator()); }
         */

        // System.out.println("isEvaluatorRegistered:
        // "+ExpressionEvaluatorManager.isEvaluatorRegistered("endpointuri"));
        final Map map = new HashMap();
        final Map inner = new HashMap();
        inner
                .put("payload.cn",
                        "(sn=#[bean:name], cn=#[endpointuri:testendpoint.params:cnprop])");
        map.put("queries", inner);

        final ImmutableEndpoint endpoint = new DefaultOutboundEndpoint(
                connector, url, null, null, "testendpoint", map, null, null,
                true, null, false, false, 0, null, null, null, muleContext,
                new NoRetryPolicyTemplate());

        // EndpointURIEndpointBuilder b = new
        // EndpointURIEndpointBuilder(endpoint,muleContext);

        RegistryContext.getRegistry().registerEndpoint(endpoint);
        RegistryContext.getRegistry().registerObject("testendpoint", endpoint);
        // RegistryContext.getRegistry().registerEndpointBuilder("teb", b);

        // EndpointInfoExpressionEvaluator

        final String searchStr = LDAPUtils.getSearchStringFromEndpoint(
                endpoint, new User("john"));

        assertEquals("(sn=john, cn=testprop)", searchStr);

    }

    public Connector getConnector() throws Exception
    {

        final LdapConnector c = new LdapConnector();
        c.setMuleContext(muleContext);
        c.setLdapHost("localhost");
        c.setLdapPort(10389);
        c.setName("ldapTestConnector");

        c.setLoginDN("uid=admin,ou=system");
        c.setPassword("secret");

        c.setSearchBase("o=sevenSeas");

        final Map map = new HashMap();
        map.put("payload.cn", "(cn=#[payload:])");

        c.setQueries(map);

        RegistryContext.getRegistry().registerConnector(c);

        return c;
    }

    public static final class User
    {
        private String name = "default";

        public User(final String name)
        {
            setName(name);
        }

        public String getName()
        {
            return name;
        }

        public void setName(final String name)
        {
            this.name = name;
        }
    }

}
