package org.mule.providers.ldap;

import org.mule.extras.client.MuleClient;
import org.mule.impl.MuleMessage;
import org.mule.providers.ldap.transformers.LDAPSearchResultToString;
import org.mule.providers.ldap.util.DSHelper;
import org.mule.providers.ldap.util.TestHelper;
import org.mule.tck.FunctionalTestCase;
import org.mule.umo.UMOMessage;
import org.mule.util.StringUtils;

import com.novell.ldap.LDAPMessage;
import com.novell.ldap.LDAPSearchResults;

public class LdapListenerSynchronTestCase extends FunctionalTestCase
{

    protected String getConfigResources()
    {
        // TODO Auto-generated method stub
        return "LdapListenerTest.xml";
    }

    public void testSearch() throws Exception
    {

        MuleClient client = new MuleClient();
        UMOMessage msg = client.send("ldap://ldap.out/", new MuleMessage(
                "(objectclass=*)"));
        assertNotNull(msg);
        assertTrue(msg.getPayload() instanceof LDAPSearchResults);

        LDAPSearchResultToString trans = new LDAPSearchResultToString();
        String s = (String) trans.transform(msg.getPayload());
        System.out.println(s);
        assertTrue(s.indexOf("<batchResponse") > -1);
        assertTrue(s.indexOf("<searchResultEntry") > -1);
        assertTrue(s.indexOf("dn=\"o=sevenseas\"><attr name=\"o\">") > -1);
    }

    public void testAsyncSearch() throws Exception
    {

        MuleClient client = new MuleClient();
        client.dispatch("ldap://ldap.out", new MuleMessage("(cn=*)"));

        UMOMessage msg = client.receive("ldap://ldap.in", 15000);

        assertNotNull(msg);
        assertTrue(msg.getPayload() instanceof LDAPMessage);
        System.out.println(msg.getPayload());

    }

    public void testAddSearch() throws Exception
    {
        MuleClient client = new MuleClient();

        final int addCount = 4;

        for (int i = 0; i < addCount; i++)
            client.dispatch("ldap://ldap.out", TestHelper
                    .getRandomEntryAddRequest(), null);

        Thread.sleep(1000);

        UMOMessage msg = client.send("vm://test_in_async", new MuleMessage(
                "(cn=*)"));

        assertNotNull(msg);
        assertTrue(msg.getPayload() instanceof String);

        // System.out.println(msg.getPayload());

        assertTrue(msg.getPayloadAsString().indexOf("<batchResponse") > -1);
        assertTrue(msg.getPayloadAsString().indexOf(
                "<searchResultEntry dn=\"cn=test-cn") > -1);

        System.out.println(StringUtils.countMatches(msg.getPayloadAsString(),
                "test-cn-"));

        assertTrue(StringUtils.countMatches(msg.getPayloadAsString(),
                "test-cn-") >= addCount);

    }

    protected void doFunctionalTearDown() throws Exception
    {
        // TODO Auto-generated method stub
        DSHelper.stopDS();
        super.doFunctionalTearDown();
    }

    protected void doPreFunctionalSetUp() throws Exception
    {
        DSHelper.startDS();
        super.doPreFunctionalSetUp();
    }

}
