package org.mule.transport.ldap.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleContext;
import org.mule.api.endpoint.EndpointMessageProcessorChainFactory;
import org.mule.api.endpoint.EndpointURI;
import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.api.processor.MessageProcessor;
import org.mule.api.retry.RetryPolicyTemplate;
import org.mule.api.routing.filter.Filter;
import org.mule.api.security.EndpointSecurityFilter;
import org.mule.api.transaction.TransactionConfig;
import org.mule.endpoint.DefaultInboundEndpoint;
import org.mule.endpoint.MuleEndpointURI;
import org.mule.tck.AbstractMuleTestCase;

public class EndpointURIExpressionEvaluatorTestCase extends
        AbstractMuleTestCase
{

    @Override
    protected void doSetUp() throws Exception
    {
        if (!muleContext.getExpressionManager().isEvaluatorRegistered(
                EndpointURIExpressionEvaluator.NAME))
        {
            muleContext.getExpressionManager().registerEvaluator(
                    new EndpointURIExpressionEvaluator());
        }
        super.doSetUp();
    }

    public void testValid()
    {

        final boolean reg = muleContext.getExpressionManager()
                .isEvaluatorRegistered(EndpointURIExpressionEvaluator.NAME);

        assertTrue(reg);

        boolean validExpression = muleContext.getExpressionManager()
                .isValidExpression("#[endpointuri:test]");

        assertTrue(validExpression);

        validExpression = muleContext.getExpressionManager().isValidExpression(
                "#[endpointuri:]");

        assertTrue(validExpression);

        validExpression = muleContext.getExpressionManager().isValidExpression(
                "#[endpointuri:xxx.xxx:xxx]");

        assertTrue(validExpression);

        validExpression = muleContext.getExpressionManager().isValidExpression(
                "#[endpointuri:xxx.xxx]");

        assertTrue(validExpression);
    }

    public void testInValid()
    {
        boolean validExpression = muleContext.getExpressionManager()
                .isValidExpression("#[endpointurxxx121q]");

        assertFalse(validExpression);

        validExpression = muleContext.getExpressionManager().isValidExpression(
                "#[endpointuri]");

        assertFalse(validExpression);
    }

    public void testNullEval() throws Exception
    {
        final Object o = muleContext.getExpressionManager().evaluate(
                "#[endpointuri:xxx.yyy]",
                new DefaultMuleMessage("", muleContext));
        assertNull(o);

    }

    public void testEval() throws Exception
    {
        final EndpointURI url = new MuleEndpointURI(
                "ldap://ldap.out/payload.cn",muleContext);
        url.initialise();

        final ImmutableEndpoint endpoint = new DefaultInboundEndpoint(null,
                url, (List) null, (List) null, "testendpoint",
                new Properties(), (TransactionConfig) null, (Filter) null,
                false, (EndpointSecurityFilter) null, false, 0, (String) null,
                (String) null, (String) null, (MuleContext) null,
                (RetryPolicyTemplate) null, (EndpointMessageProcessorChainFactory) null,(List<MessageProcessor>) null,(List<MessageProcessor>) null);

        muleContext.getRegistry().registerEndpoint(endpoint);

        final Object o = muleContext.getExpressionManager().evaluate(
                "#[endpointuri:testendpoint.host]",
                new DefaultMuleMessage("", muleContext));
        assertNotNull(o);
        assertTrue(o instanceof String);
        assertTrue(o.toString().equals("ldap.out"));

    }

    public void testEval2() throws Exception
    {
        final EndpointURI url = new MuleEndpointURI("ldap://ldap.out/?a=b",muleContext);
        url.initialise();

        final Map map = new HashMap();

        final ImmutableEndpoint endpoint = new DefaultInboundEndpoint(null,
                url, (List) null, (List) null, "testendpoint", map,
                (TransactionConfig) null, (Filter) null, false,
                (EndpointSecurityFilter) null, false, 0, (String) null,
                (String) null, (String) null, (MuleContext) null,
                (RetryPolicyTemplate) null, (EndpointMessageProcessorChainFactory) null,(List<MessageProcessor>) null,(List<MessageProcessor>) null);

        muleContext.getRegistry().registerEndpoint(endpoint);

        final Object o = muleContext.getExpressionManager().evaluate(
                "#[endpointuri:testendpoint.params:a]",
                new DefaultMuleMessage("", muleContext));
        assertNotNull(o);
        logger.debug(o);
        assertTrue(o instanceof String);
        assertTrue(o.toString().equals("b"));

    }

}
