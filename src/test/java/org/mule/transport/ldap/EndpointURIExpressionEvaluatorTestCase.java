package org.mule.transport.ldap;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.mule.DefaultMuleMessage;
import org.mule.api.endpoint.EndpointURI;
import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.endpoint.DefaultOutboundEndpoint;
import org.mule.endpoint.MuleEndpointURI;
import org.mule.tck.AbstractMuleTestCase;
import org.mule.transport.ldap.util.EndpointURIExpressionEvaluator;
import org.mule.util.expression.ExpressionEvaluatorManager;

public class EndpointURIExpressionEvaluatorTestCase extends
        AbstractMuleTestCase
{

    @Override
    protected void doSetUp() throws Exception
    {
        if (!ExpressionEvaluatorManager
                .isEvaluatorRegistered(EndpointURIExpressionEvaluator.NAME))
        {
            ExpressionEvaluatorManager
                    .registerEvaluator(new EndpointURIExpressionEvaluator());
        }
        super.doSetUp();
    }

    public void testValid()
    {
        boolean validExpression = ExpressionEvaluatorManager
                .isValidExpression("endpointuri:test");

        assertTrue(validExpression);

        validExpression = ExpressionEvaluatorManager
                .isValidExpression("endpointuri");

        assertTrue(validExpression);

        validExpression = ExpressionEvaluatorManager
                .isValidExpression("endpointuri:xxx.xxx:xxx");

        assertTrue(validExpression);

        validExpression = ExpressionEvaluatorManager
                .isValidExpression("endpointuri:xxx.xxx");

        assertTrue(validExpression);
    }

    public void testInValid()
    {
        boolean validExpression = ExpressionEvaluatorManager
                .isValidExpression("endpointurxxx");

        assertFalse(validExpression);

    }

    public void testNullEval() throws Exception
    {
        Object o = ExpressionEvaluatorManager.evaluate("endpointuri:xxx.yyy",
                new DefaultMuleMessage(""));
        assertNull(o);

    }

    public void testEval() throws Exception
    {
        EndpointURI url = new MuleEndpointURI("ldap://ldap.out/payload.cn");
        url.initialise();

        ImmutableEndpoint endpoint = new DefaultOutboundEndpoint(null, url,
                null, null, "testendpoint", new Properties(), null, null, true,
                null, false, false, 0, null, null, muleContext, null);

        muleContext.getRegistry().registerEndpoint(endpoint);

        Object o = ExpressionEvaluatorManager.evaluate(
                "endpointuri:testendpoint.host", new DefaultMuleMessage(""));
        assertNotNull(o);
        assertTrue(o instanceof String);
        assertTrue(o.toString().equals("ldap.out"));

    }

    public void testEval2() throws Exception
    {
        EndpointURI url = new MuleEndpointURI("ldap://ldap.out/?a=b");
        url.initialise();

        Map map = new HashMap();

        ImmutableEndpoint endpoint = new DefaultOutboundEndpoint(null, url,
                null, null, "testendpoint", map, null, null, true, null, false,
                false, 0, null, null, muleContext, null);

        muleContext.getRegistry().registerEndpoint(endpoint);

        Object o = ExpressionEvaluatorManager
                .evaluate("endpointuri:testendpoint.params:a",
                        new DefaultMuleMessage(""));
        assertNotNull(o);
        logger.debug(o);
        assertTrue(o instanceof String);
        assertTrue(o.toString().equals("b"));

    }

}
