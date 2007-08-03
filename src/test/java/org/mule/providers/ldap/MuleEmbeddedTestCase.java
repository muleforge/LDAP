package org.mule.providers.ldap;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.mule.config.builders.QuickConfigurationBuilder;
import org.mule.extras.client.MuleClient;
import org.mule.providers.ldap.util.DSManager;
import org.mule.providers.ldap.util.LDAPUtils;
import org.mule.providers.ldap.util.TestHelper;
import org.mule.tck.functional.EventCallback;
import org.mule.tck.functional.FunctionalTestComponent;
import org.mule.tck.functional.FunctionalTestNotification;
import org.mule.tck.functional.FunctionalTestNotificationListener;
import org.mule.umo.UMOEventContext;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.manager.UMOServerNotification;
import org.mule.umo.provider.DispatchException;
import org.mule.umo.provider.UMOConnector;
import org.mule.util.StringMessageUtils;

import com.novell.ldap.LDAPAddRequest;
import com.novell.ldap.LDAPDeleteRequest;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPMessage;
import com.novell.ldap.LDAPResponse;
import com.novell.ldap.LDAPSearchResult;
import com.novell.ldap.LDAPSearchResults;

public class MuleEmbeddedTestCase extends TestCase implements EventCallback,
        FunctionalTestNotificationListener
{

    QuickConfigurationBuilder builder;
    LdapConnector connector = null;

    // this is where we create our configuration

    protected void setUp() throws Exception
    {

        System.out.println(StringMessageUtils.getBoilerPlate("Testing: "
                + toString(), '=', 80));

        //DSHelper.startDS();
        DSManager.getInstance().start();
        System.out.println("ds started");

        builder = new QuickConfigurationBuilder();

        connector = (LdapConnector) getConnector();

        Map queries = new HashMap();
        queries.put("oc.payload", "(o=${payload})");
        queries.put("cn.payload", "(cn=${payload})");

        connector.setQueries(queries);

        builder.getManager().registerConnector(connector);

        // we create our MuleManager
        builder.createStartedManager(false, null);

        // we create a "SINGLE" endpoint and set the address to vm://single
        // UMOEndpoint ldapOut = builder.createEndpoint("ldap://ldap.in",
        // "LDAPInbound", true);

        // we create a FunctionalTestComponent and call it myComponent
        FunctionalTestComponent funcTestComponent = new FunctionalTestComponent();

        // we set out Event Callback on our test class
        funcTestComponent.setEventCallback(this);

        // we register our component instance.
        // builder.registerComponentInstance(funcTestComponent, "LDAP",
        // ldapOut.getEndpointURI());

        // we register our listener which we called "SINGLE"
        // builder.getManager().registerListener(this, "LDAP");
    }

    // callback
    public void eventReceived(UMOEventContext context, Object Component)
            throws Exception
    {
        // UMOMessage message = context.getMessage();
        FunctionalTestComponent fc = (FunctionalTestComponent) Component;
        fc.setReturnMessage("Customized Return Message");
    }

    public void testSendReceiveSearch() throws Exception
    {
        MuleClient client = new MuleClient();

        // we send a message on the endpoint we created, i.e. vm://Single
        UMOMessage result = client.send("ldap://ldap.out/oc.payload",
                "sevenseas", null);
        assertNotNull(result);
        assertTrue(result.getPayload() instanceof LDAPSearchResults);
        assertEquals(((LDAPSearchResults) result.getPayload()).next().getDN(),
                "o=sevenseas");

    }

    public void testSendAdd() throws Exception
    {
        MuleClient client = new MuleClient();

        // we send a message on the endpoint we created, i.e. vm://Single

        LDAPAddRequest addReq = TestHelper.getRandomEntryAddRequest();

        UMOMessage result = client.send("ldap://ldap.out", addReq, null);
        assertNotNull(result);
        assertTrue(result.getPayload() == addReq);

        result = client.send("ldap://ldap.out",
                LDAPUtils.createSearchRequest(connector, "(cn="
                        + addReq.getEntry().getAttribute("cn").getStringValue() + ")"), null);

        //System.out.println("cn: "+addReq.getEntry().getAttribute("cn").getStringValue());
        
        assertNotNull(result);
        assertTrue(result.getPayload() instanceof LDAPSearchResults);
        assertTrue(((LDAPSearchResults) result.getPayload()).next().getDN().equals(addReq.getEntry().getDN()));

        
        
    }
    
    public void testSendAddDupl() throws Exception
    {
        MuleClient client = new MuleClient();

        // we send a message on the endpoint we created, i.e. vm://Single

        LDAPAddRequest addReq = TestHelper.getRandomEntryAddRequest();

        UMOMessage result = client.send("ldap://ldap.out", addReq, null);
        assertNotNull(result);
        assertTrue(result.getPayload() == addReq);

        result = client.send("ldap://ldap.out",
                LDAPUtils.createSearchRequest(connector, "(cn="
                        + addReq.getEntry().getAttribute("cn").getStringValue() + ")"), null);

        //System.out.println("cn: "+addReq.getEntry().getAttribute("cn").getStringValue());
        
        assertNotNull(result);
        assertTrue(result.getPayload() instanceof LDAPSearchResults);
        assertTrue(((LDAPSearchResults) result.getPayload()).next().getDN().equals(addReq.getEntry().getDN()));

        try
        {
            client.send("ldap://ldap.out", addReq, null);
            fail();
        } catch (DispatchException e)
        {
            //expected, dup entry
           assertEquals(((LDAPException)e.getCause()).getResultCode(),68);
        }
        
        
    }

    public void testDispatchReceiveSearch() throws Exception
    {
        MuleClient client = new MuleClient();

        // we send a message on the endpoint we created, i.e. vm://Single
        client.dispatch("ldap://ldap.out/oc.payload", "sevenseas", null);

        UMOMessage result = client.receive("ldap://ldap.in", 15000);

        assertNotNull(result);
        assertTrue(result.getPayload() instanceof LDAPSearchResult);
        assertEquals(((LDAPSearchResult) result.getPayload()).getEntry()
                .getDN(), "o=sevenseas");

    }

    public void testDispatchReceiveSearchMultiple() throws Exception
    {
        MuleClient client = new MuleClient();

        final int addCount = 4;

        for (int i = 0; i < addCount; i++)
            client.dispatch("ldap://ldap.out", TestHelper
                    .getRandomEntryAddRequest(), null);

        Thread.sleep(1000);

        // we send a message on the endpoint we created, i.e. vm://Single
        client.dispatch("ldap://ldap.out/cn.payload", "*", null);

        UMOMessage result = null;
        UMOMessage lastResult = null;

        int count = 0;

        // addCount AddResponses, addCount Search Responses, 1x Search Result ok
        final int expected = (2 * addCount) + 1;

        while (true)
        {
            result = client.receive("ldap://ldap.in", 6000);

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
    }

    public void testDispatchReceiveSearchMultipleWithDelete() throws Exception
    {
        MuleClient client = new MuleClient();

        final int addCount = 4;
        LDAPAddRequest last = null;

        for (int i = 0; i < addCount; i++)
            client.dispatch("ldap://ldap.out", last = TestHelper
                    .getRandomEntryAddRequest(), null);

        Thread.sleep(1000);

        client.dispatch("ldap://ldap.out", new LDAPDeleteRequest(last
                .getEntry().getDN(), null), null);

        Thread.sleep(1000);

        // we send a message on the endpoint we created, i.e. vm://Single
        client.dispatch("ldap://ldap.out/cn.payload", "*", null);

        UMOMessage result = null;
        UMOMessage lastResult = null;

        int count = 0;

        // addCount AddResponses, addCount Search Responses, 1x Search Result
        // ok, 1 deleted, 1 delete response
        final int expected = (2 * addCount) + 1 - 1 + 1;

        while (true)
        {
            result = client.receive("ldap://ldap.in", 6000);

            if (result == null)
            {
                break;
            }

            System.out.println(LDAPUtils.dumpLDAPMessage(result.getPayload()));

            lastResult = result;
            count++;
        }

        assertTrue(count == expected);
        assertNull(result);
        assertNotNull(lastResult);
        assertTrue(lastResult.getPayload().getClass().toString(), lastResult
                .getPayload() instanceof LDAPResponse);
        assertTrue(((LDAPResponse) lastResult.getPayload()).getType() == LDAPMessage.SEARCH_RESULT);
    
        //TODO not predictable
    
    }

    public void testDispatchReceiveSearchDeleted() throws Exception
    {
        MuleClient client = new MuleClient();

        final int addCount = 4;
        LDAPAddRequest last = null;

        for (int i = 0; i < addCount; i++)
            client.dispatch("ldap://ldap.out", last = TestHelper
                    .getRandomEntryAddRequest(), null);

        Thread.sleep(1000);

        client.dispatch("ldap://ldap.out", new LDAPDeleteRequest(last
                .getEntry().getDN(), null), null);

        Thread.sleep(3000);

        // we send a message on the endpoint we created, i.e. vm://Single
        client.dispatch("ldap://ldap.out/cn.payload", last.getEntry().getDN(),
                null);

        UMOMessage result = null;
        UMOMessage lastResult = null;

        int count = 0;

        // addCount AddResponses, 1x Search Result, 1 delete response
        final int expected = addCount + 1 + 1;

        while (true)
        {
            result = client.receive("ldap://ldap.in", 6000);

            if (result == null)
            {
                break;
            }

            System.out.println(LDAPUtils.dumpLDAPMessage(result.getPayload()));

            lastResult = result;
            count++;
        }

        assertTrue(count == expected);
        assertNull(result);
        assertNotNull(lastResult);
        assertTrue(lastResult.getPayload() instanceof LDAPResponse);

        // TODO order is not predictable
        // assertTrue("Check type: "+((LDAPResponse)
        // lastResult.getPayload()).getType()+" should be
        // "+LDAPMessage.SEARCH_RESULT,((LDAPResponse)
        // lastResult.getPayload()).getType() == LDAPMessage.SEARCH_RESULT);
    }

    public void testReceiveTimeout() throws Exception
    {

        MuleClient client = new MuleClient();

        UMOMessage result = client.receive("ldap://ldap.in", 10000);

        assertNull(result);

    }

    public void testDispatchReceiveAdd() throws Exception
    {
        MuleClient client = new MuleClient();

        // we send a message on the endpoint we created, i.e. vm://Single
        client.dispatch("ldap://ldap.out", TestHelper
                .getRandomEntryAddRequest(), null);

        UMOMessage result = client.receive("ldap://ldap.in", 15000);

        assertNotNull(result);
        assertTrue(result.getPayload() instanceof LDAPResponse);
        assertEquals(((LDAPResponse) result.getPayload()).getResultCode(),
                LDAPException.SUCCESS);
        assertEquals(((LDAPResponse) result.getPayload()).getType(),
                LDAPMessage.ADD_RESPONSE);
    }

    public void testDispatchReceiveAddStress() throws Exception
    {
        final MuleClient client = new MuleClient();

        final int count = 10000;

        Runnable rec = new Runnable()
        {

            public void run()
            {

                int current = 0;

                try
                {

                    while (current < count)
                    {
                        UMOMessage result = client.receive("ldap://ldap.in",
                                15000);

                        assertNotNull(result);
                        assertTrue(result.getPayload() instanceof LDAPResponse);
                        assertEquals(((LDAPResponse) result.getPayload())
                                .getResultCode(), LDAPException.SUCCESS);
                        assertEquals(((LDAPResponse) result.getPayload())
                                .getType(), LDAPMessage.ADD_RESPONSE);
                        System.out.println("got " + current);
                        current++;
                    }

                } catch (UMOException e)
                {
                    fail(e.toString());

                }

                assertTrue(current == count);

            }

        };

        Thread t = new Thread(rec);
        t.start();

        Thread.yield();

        // we send a message on the endpoint we created, i.e. vm://Single
        for (int i = 0; i < count; i++)
            client.dispatch("ldap://ldap.out", TestHelper
                    .getRandomEntryAddRequest(), null);

        t.join();

    }

    public void onNotification(UMOServerNotification notification)
    {
        assertTrue(notification.getAction() == FunctionalTestNotification.EVENT_RECEIVED);
    }

    protected void tearDown() throws Exception
    {
        builder.disposeCurrent();
        DSManager.getInstance().stop();
        //DSHelper.stopDS();
    }

    public UMOConnector getConnector() throws Exception
    {

        LdapConnector c = new LdapConnector();
        c.setLdapHost("localhost");
        c.setLdapPort(10389);
        c.setName("ldapTestConnector");
        c.setLoginDN("uid=admin,ou=system");
        c.setPassword("secret");
        c.setSearchBase("o=sevenseas");
        c.setStartUnsolicitedNotificationListener(true);

        return c;
    }

}
