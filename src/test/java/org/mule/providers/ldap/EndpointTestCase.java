package org.mule.providers.ldap;

import org.mule.impl.endpoint.MuleEndpointURI;
import org.mule.tck.AbstractMuleTestCase;
import org.mule.umo.endpoint.MalformedEndpointException;
import org.mule.umo.endpoint.UMOEndpointURI;

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
        } catch (MalformedEndpointException e)
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
        } catch (MalformedEndpointException e)
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
        } catch (MalformedEndpointException e)
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

}
