/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.ldap.functional;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.RegistryContext;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.transport.DispatchException;
import org.mule.module.client.MuleClient;
import org.mule.tck.AbstractMuleTestCase;
import org.mule.transport.ldap.FetchSchemaAction;
import org.mule.transport.ldap.LdapConnector;
import org.mule.transport.ldap.util.DSManager;
import org.mule.transport.ldap.util.LDAPUtils;
import org.mule.transport.ldap.util.TestHelper;

import com.novell.ldap.LDAPAddRequest;
import com.novell.ldap.LDAPCompareRequest;
import com.novell.ldap.LDAPDeleteRequest;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPMessage;
import com.novell.ldap.LDAPResponse;
import com.novell.ldap.LDAPSchema;
import com.novell.ldap.LDAPSearchResult;
import com.novell.ldap.LDAPSearchResults;

public class MuleEmbeddedTestCase extends AbstractMuleTestCase // implements
// EventCallback,
// FunctionalTestNotificationListener
{
    protected final Log logger = LogFactory.getLog(getClass());

    // protected ConfigurationBuilder builder;
    protected LdapConnector connector = null;

    // this is where we create our configuration
    /*
     * protected void setUp() throws Exception {
     * 
     * logger.debug(StringMessageUtils.getBoilerPlate( "Testing: " + toString(),
     * '=', 80)); // DSHelper.startDS(); DSManager.getInstance().start();
     * logger.debug("ds started");
     * 
     * //FIXME builder = null;
     * 
     * connector = (LdapConnector) getConnector();
     * 
     * Map queries = new HashMap(); queries.put("oc.payload", "(o=${payload})");
     * queries.put("cn.payload", "(cn=${payload})");
     * 
     * connector.setQueries(queries);
     * 
     * builder.getManager().registerConnector(connector); // we create our
     * MuleManager builder.createStartedManager(false, null); // we create a
     * "SINGLE" endpoint and set the address to vm://single // UMOEndpoint
     * ldapOut = builder.createEndpoint("ldap://ldap.in", // "LDAPInbound",
     * true); // we create a FunctionalTestComponent and call it myComponent //
     * FunctionalTestComponent funcTestComponent = new //
     * FunctionalTestComponent(); // we set out Event Callback on our test class //
     * funcTestComponent.setEventCallback(this); // we register our component
     * instance. // builder.registerComponentInstance(funcTestComponent, "LDAP", //
     * ldapOut.getEndpointURI()); // we register our listener which we called
     * "SINGLE" // builder.getManager().registerListener(this, "LDAP"); }
     * 
     */

    /*
     * protected String getConfigResources() { connector = getConnector();
     * return super.getConfigResources(); }
     */

    public void testSendReceiveSearch() throws Exception
    {
        final MuleClient client = new MuleClient();

        // we send a message on the endpoint we created, i.e. vm://Single
        final MuleMessage result = client.send("ldap://ldap.out/oc.payload",
                "sevenseas", null);
        assertNotNull(result);
        assertTrue(result.getPayload() instanceof LDAPSearchResults);
        assertEquals(((LDAPSearchResults) result.getPayload()).next().getDN(),
                "o=sevenseas");
        try
        {
            DSManager.getInstance().stop();
        }
        catch (final Exception e)
        {

        }
    }

    public void testSendAdd() throws Exception
    {
        final MuleClient client = new MuleClient();

        // we send a message on the endpoint we created, i.e. vm://Single

        final LDAPAddRequest addReq = TestHelper.getRandomEntryAddRequest();

        MuleMessage result = client.send("ldap://ldap.out", addReq, null);
        assertNotNull(result);
        assertTrue(result.getPayload() == addReq);

        result = client.send("ldap://ldap.out", LDAPUtils.createSearchRequest(
                connector, "(cn="
                        + addReq.getEntry().getAttribute("cn").getStringValue()
                        + ")"), null);

        // logger.debug("cn:
        // "+addReq.getEntry().getAttribute("cn").getStringValue());

        assertNotNull(result);
        assertTrue(result.getPayload() instanceof LDAPSearchResults);
        assertTrue(((LDAPSearchResults) result.getPayload()).next().getDN()
                .equals(addReq.getEntry().getDN()));
        try
        {
            DSManager.getInstance().stop();
        }
        catch (final Exception e)
        {

        }
    }

    public void testPersistentSearch() throws Exception
    {
        final LdapConnector c = new LdapConnector();
        c.setMuleContext(muleContext);
        c.setLdapHost("localhost");
        c.setLdapPort(10389);
        c.setName("LdapTestConnectorP");
        c.setLoginDN("uid=admin,ou=system");
        c.setPassword("secret");
        c.setSearchBase("o=sevenseas");
        c.setStartUnsolicitedNotificationListener(true);
        c.setEnablePersistentSearch(true);
        final List < String > list = new ArrayList < String >();
        list.add("cn=*");
        c.setPsFilters(list);

        RegistryContext.getRegistry().registerConnector(c);

        final MuleClient client = new MuleClient();

        // start requester to collect messages
        MuleMessage result = client.request(
                "ldap://ldap.in?connector=LdapTestConnectorP", 500);
        assertNull(result);
        // we send a message on the endpoint we created, i.e. vm://Single

        final LDAPAddRequest addReq = TestHelper.getRandomEntryAddRequest();

        result = client.send("ldap://ldap.out?connector=LdapTestConnectorP",
                addReq, null);
        assertNotNull(result);
        assertTrue(result.getPayload() == addReq);

        Thread.sleep(500);

        result = client.request("ldap://ldap.in?connector=LdapTestConnectorP",
                5000);
        logger.debug(result);
        assertNotNull(result);
        assertTrue(result.getPayload() instanceof EventObject);

        try
        {
            DSManager.getInstance().stop();
        }
        catch (final Exception e)
        {

        }
    }

    public void testSendLDAPCompareRequest() throws Exception
    {
        final MuleClient client = new MuleClient();

        // we send a message on the endpoint we created, i.e. vm://Single

        final LDAPCompareRequest addReq = new LDAPCompareRequest(
                "uid=hsaly,ou=users,dc=example,dc=com", "uid", "hsaly"
                        .getBytes(), null);

        final MuleMessage result = client.send("ldap://ldap.out", addReq, null);
        assertNotNull(result);
        assertTrue(result.getPayload() instanceof Boolean);

        logger.debug(result.getPayload());

        assertTrue(((Boolean) result.getPayload()).booleanValue());

        try
        {
            DSManager.getInstance().stop();
        }
        catch (final Exception e)
        {

        }
    }

    public void testSendBadLDAPCompareRequest() throws Exception
    {
        final MuleClient client = new MuleClient();

        // we send a message on the endpoint we created, i.e. vm://Single

        final LDAPCompareRequest addReq = new LDAPCompareRequest(
                "uid=hsaly,ou=users,dc=example,dc=com", "uid", "hsaly123"
                        .getBytes(), null);

        final MuleMessage result = client.send("ldap://ldap.out", addReq, null);
        assertNotNull(result);
        assertTrue(result.getPayload() instanceof Boolean);

        logger.debug(result.getPayload());

        assertFalse(((Boolean) result.getPayload()).booleanValue());

        try
        {
            DSManager.getInstance().stop();
        }
        catch (final Exception e)
        {

        }
    }

    public void testSendFetchSchemaAction() throws Exception
    {
        final MuleClient client = new MuleClient();

        // we send a message on the endpoint we created, i.e. vm://Single

        final FetchSchemaAction action = new FetchSchemaAction(
                "uid=hsaly,ou=users,dc=example,dc=com");

        final MuleMessage result = client.send("ldap://ldap.out", action, null);
        assertNotNull(result);
        assertNotNull(result.getPayload());

        assertTrue(result.getPayload() instanceof LDAPSchema);

        try
        {
            DSManager.getInstance().stop();
        }
        catch (final Exception e)
        {

        }
    }

    public void testSendAddDupl() throws Exception
    {
        final MuleClient client = new MuleClient();

        // we send a message on the endpoint we created, i.e. vm://Single

        final LDAPAddRequest addReq = TestHelper.getRandomEntryAddRequest();

        MuleMessage result = client.send("ldap://ldap.out", addReq, null);
        assertNotNull(result);
        assertTrue(result.getPayload() == addReq);

        result = client.send("ldap://ldap.out", LDAPUtils.createSearchRequest(
                connector, "(cn="
                        + addReq.getEntry().getAttribute("cn").getStringValue()
                        + ")"), null);

        // logger.debug("cn:
        // "+addReq.getEntry().getAttribute("cn").getStringValue());

        assertNotNull(result);
        assertTrue(result.getPayload() instanceof LDAPSearchResults);
        assertTrue(((LDAPSearchResults) result.getPayload()).next().getDN()
                .equals(addReq.getEntry().getDN()));

        try
        {
            client.send("ldap://ldap.out", addReq, null);
            fail();
        }
        catch (final DispatchException e)
        {
            // expected, dup entry
            assertEquals(((LDAPException) e.getCause()).getResultCode(), 68);
        }
        try
        {
            DSManager.getInstance().stop();
        }
        catch (final Exception e)
        {

        }
    }

    public void testDispatchReceiveSearch() throws Exception
    {
        final MuleClient client = new MuleClient();

        // we send a message on the endpoint we created, i.e. vm://Single
        client.dispatch("ldap://ldap.out/oc.payload", "sevenseas", null);

        final MuleMessage result = client.request("ldap://ldap.in", 30000);

        assertNotNull(result);
        assertTrue(result.getPayload() instanceof LDAPSearchResult);
        assertEquals(((LDAPSearchResult) result.getPayload()).getEntry()
                .getDN(), "o=sevenseas");
        try
        {
            DSManager.getInstance().stop();
        }
        catch (final Exception e)
        {

        }
    }

    public void testDispatchReceiveSearchMultiple() throws Exception
    {
        final MuleClient client = new MuleClient();

        final int addCount = 4;

        for (int i = 0; i < addCount; i++)
        {
            client.dispatch("ldap://ldap.out", TestHelper
                    .getRandomEntryAddRequest(), null);
        }

        Thread.sleep(1000);

        // we send a message on the endpoint we created, i.e. vm://Single
        client.dispatch("ldap://ldap.out/cn.payload", "*", null);

        MuleMessage result = null;
        MuleMessage lastResult = null;

        int count = 0;

        // addCount AddResponses, addCount Search Responses, 1x Search Result ok
        final int expected = (2 * addCount) + 1;

        while (true)
        {
            result = client.request("ldap://ldap.in", 15000);

            if (result == null)
            {
                break;
            }

            lastResult = result;
            count++;
        }

        assertTrue(count == expected);
        assertNull(result);
        assertNotNull(lastResult);
        assertTrue(lastResult.getPayload() instanceof LDAPResponse);
        assertTrue(((LDAPResponse) lastResult.getPayload()).getType() == LDAPMessage.SEARCH_RESULT);

        try
        {
            DSManager.getInstance().stop();
        }
        catch (final Exception e)
        {

        }

    }

    public synchronized void testDispatchReceiveSearchMultipleWithDelete()
            throws Exception
    {
        final MuleClient client = new MuleClient();

        final int addCount = 4;
        LDAPAddRequest last = null;

        for (int i = 0; i < addCount; i++)
        {
            client.dispatch("ldap://ldap.out", last = TestHelper
                    .getRandomEntryAddRequest(), null);
        }

        // time for processing
        Thread.yield();
        Thread.sleep(10000);

        client.dispatch("ldap://ldap.out", new LDAPDeleteRequest(last
                .getEntry().getDN(), null), null);

        // time for processing
        Thread.yield();
        Thread.sleep(10000);

        // we send a message on the endpoint we created, i.e. vm://Single
        client.dispatch("ldap://ldap.out/cn.payload", "*", null);

        // time for processing
        Thread.yield();
        Thread.sleep(10000);

        MuleMessage result = null;
        MuleMessage lastResult = null;

        int count = 0;
        int tryCount = 0;

        // addCount * AddResponses, addCount * Search Responses, 1x Search
        // Result
        // ok, 1 delete response
        final int expected = (2 * addCount) + 1 - 1 + 1;

        // Wait until all dispatched messages are processed by DS
        while ((tryCount < 20) && (connector.getOutstandingMessageCount() != 6))
        {
            Thread.yield();
            Thread.sleep(2000);
            logger.debug(tryCount + ".try: Outstanding are "
                    + connector.getOutstandingMessageCount());
            tryCount++;
        }

        assertTrue("Outstanding message count ("
                + connector.getOutstandingMessageCount() + ") not expected ("
                + 6 + ")", connector.getOutstandingMessageCount() == 6);

        // time for processing
        Thread.yield();
        Thread.sleep(10000);

        logger.debug("Outstanding: " + connector.getOutstandingMessageCount());

        while (true)
        {
            result = client.request("ldap://ldap.in", 20000);

            if (result == null)
            {
                logger.debug("The " + count + ". message was null");
                break;
            }

            logger.debug(LDAPUtils.dumpLDAPMessage(result.getPayload()));

            lastResult = result;
            count++;
        }

        if (count != expected)
        {
            logger.warn("count (" + count + ") != expected (" + expected + ")");
        }

        assertNull("result was not null", result);
        assertNotNull("lastResult was null", lastResult);
        assertTrue(lastResult.getPayload().getClass().toString()
                + " instead of LDAPResponse",
                lastResult.getPayload() instanceof LDAPResponse);

        // TODO not predictable
        assertTrue(
                ((LDAPResponse) lastResult.getPayload()).getType()
                        + " type not expected (" + LDAPMessage.SEARCH_RESULT
                        + ")",
                ((LDAPResponse) lastResult.getPayload()).getType() == LDAPMessage.SEARCH_RESULT);
        assertTrue("count (" + count + ") != expected (" + expected + ")",
                count == expected);
        try
        {
            DSManager.getInstance().stop();
        }
        catch (final Exception e)
        {

        }
    }

    public synchronized void testDispatchReceiveSearchDeleted()
            throws Exception
    {
        final MuleClient client = new MuleClient();

        final int addCount = 4;
        LDAPAddRequest last = null;

        for (int i = 0; i < addCount; i++)
        {
            client.dispatch("ldap://ldap.out", last = TestHelper
                    .getRandomEntryAddRequest(), null);
        }

        Thread.yield();
        Thread.sleep(10000);

        client.dispatch("ldap://ldap.out", new LDAPDeleteRequest(last
                .getEntry().getDN(), null), null);

        Thread.yield();
        Thread.sleep(10000);

        // we send a message on the endpoint we created, i.e. vm://Single
        client.dispatch("ldap://ldap.out/cn.payload", last.getEntry().getDN(),
                null);

        Thread.yield();
        Thread.sleep(10000);

        MuleMessage result = null;
        MuleMessage lastResult = null;

        int count = 0;

        // addCount AddResponses, 1x Search Result, 1 delete response
        final int expected = addCount + 1 + 1;

        while (true)
        {
            result = client.request("ldap://ldap.in", 15000);

            if (result == null)
            {
                break;
            }

            logger.debug(LDAPUtils.dumpLDAPMessage(result.getPayload()));

            lastResult = result;
            count++;
        }

        assertTrue("Count (" + count + ") != expected (" + expected + ")",
                count == expected); // fails because of list returnded or
        // processing time to short?
        assertNull(result);
        assertNotNull(lastResult);
        assertTrue("instanceof " + lastResult.getPayload().getClass()
                + " instead of LDAPResponse",
                lastResult.getPayload() instanceof LDAPResponse);

        // TODO order is not predictable
        // assertTrue(
        // "Check type: "
        // + ((LDAPResponse) lastResult.getPayload()).getType()
        // + " should be " + LDAPMessage.SEARCH_RESULT,
        // ((LDAPResponse) lastResult.getPayload()).getType() ==
        // LDAPMessage.SEARCH_RESULT);

        // is 2xaddresponse, 1 searchresult, 1 addresponse, 1 delresponse, 1
        // addresponse

        try
        {
            DSManager.getInstance().stop();
        }
        catch (final Exception e)
        {

        }

    }

    public void testReceiveTimeout() throws Exception
    {

        final MuleClient client = new MuleClient();

        final MuleMessage result = client.request("ldap://ldap.in", 15000);

        assertNull(result);

        try
        {
            DSManager.getInstance().stop();
        }
        catch (final Exception e)
        {

        }

    }

    public void testDispatchReceiveAdd() throws Exception
    {
        final MuleClient client = new MuleClient();

        // we send a message on the endpoint we created, i.e. vm://Single
        client.dispatch("ldap://ldap.out", TestHelper
                .getRandomEntryAddRequest(), null);

        final MuleMessage result = client.request("ldap://ldap.in", 15000);

        assertNotNull(result);
        assertTrue(result.getPayload() instanceof LDAPResponse);
        assertEquals(((LDAPResponse) result.getPayload()).getResultCode(),
                LDAPException.SUCCESS);
        assertEquals(((LDAPResponse) result.getPayload()).getType(),
                LDAPMessage.ADD_RESPONSE);

        try
        {
            DSManager.getInstance().stop();
        }
        catch (final Exception e)
        {

        }
    }

    public void testDispatchReceiveAddStress() throws Exception
    {
        final MuleClient client = new MuleClient();

        final int count = 500;

        final Runnable rec = new Runnable()
        {

            public void run()
            {

                int current = 0;

                try
                {

                    while (current < count)
                    {
                        MuleMessage result = client.request("ldap://ldap.in",
                                15000);

                        assertNotNull(result);
                        assertTrue(result.getPayload() instanceof LDAPResponse);
                        assertEquals(((LDAPResponse) result.getPayload())
                                .getResultCode(), LDAPException.SUCCESS);
                        assertEquals(((LDAPResponse) result.getPayload())
                                .getType(), LDAPMessage.ADD_RESPONSE);
                        logger.debug("got " + current);
                        current++;
                    }

                }
                catch (Exception e)
                {
                    fail(e.toString());

                }

                assertTrue(current == count);

            }

        };

        final Thread t = new Thread(rec);
        t.start();

        Thread.yield();

        // we send a message on the endpoint we created, i.e. vm://Single
        for (int i = 0; i < count; i++)
        {
            client.dispatch("ldap://ldap.out", TestHelper
                    .getRandomEntryAddRequest(), null);
        }

        t.join();

        try
        {
            DSManager.getInstance().stop();
        }
        catch (final Exception e)
        {

        }
    }

    public LdapConnector initConnector() throws MuleException
    {

        // MuleClient client = new MuleClient();

        final LdapConnector c = new LdapConnector();
        c.setMuleContext(muleContext);
        c.setLdapHost("localhost");
        c.setLdapPort(10389);
        c.setName("innerLdapTestConnector");
        c.setLoginDN("uid=admin,ou=system");
        c.setPassword("secret");
        c.setSearchBase("o=sevenseas");
        c.setStartUnsolicitedNotificationListener(true);
        final Map queries = new HashMap();
        queries.put("oc.payload", "(o=#[payload:])");
        queries.put("cn.payload", "(cn=#[payload:])");

        c.setQueries(queries);

        RegistryContext.getRegistry().registerConnector(c);

        return c;
    }

    @Override
    protected void doSetUp() throws Exception
    {

        DSManager.getInstance().start();
        connector = initConnector();
        super.doSetUp();

    }

    @Override
    protected void doTearDown() throws Exception
    {
        super.doTearDown();
        ;
        DSManager.getInstance().stop();

    }
}
