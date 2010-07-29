package org.mule.transport.ldap.functional;

import java.util.Map;

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleMessage;
import org.mule.module.client.MuleClient;
import org.mule.transport.ldap.transformers.LDAPSearchResultToString;
import org.mule.transport.ldap.util.DSManager;
import org.mule.transport.ldap.util.TestHelper;

import com.novell.ldap.LDAPAddRequest;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPMessage;
import com.novell.ldap.LDAPSearchResults;
import com.novell.ldap.util.DN;

public class LdapListenerSynchronTestCase extends
        AbstractLdapFunctionalTestCase
{

    @Override
    protected String getConfigResources()
    {

        return super.getConfigResources() + ",ldap-functional-config.xml";
    }

    public void testSearch() throws Exception
    {

        final MuleClient client = new MuleClient(muleContext);
        final MuleMessage msg = client.send("ldap://ldap.out/",
                new DefaultMuleMessage("(objectclass=*)", muleContext));
        assertNotNull(msg);
        assertTrue(msg.getPayload() instanceof LDAPSearchResults);

        final LDAPSearchResultToString trans = new LDAPSearchResultToString();
        final String s = (String) trans.transform(msg.getPayload());

        assertTrue(s.indexOf("<batchResponse") > -1);
        assertTrue(s.indexOf("<searchResultEntry") > -1);
        assertTrue(s.indexOf("dn=\"o=sevenseas\"><attr name=\"o\">") > -1);

        try
        {
            DSManager.getInstance().stop();
        }
        catch (final Exception e)
        {

        }

    }

    public void testAsyncSearch() throws Exception
    {

        final MuleClient client = new MuleClient(muleContext);
        client.dispatch("ldap://ldap.out", new DefaultMuleMessage("(cn=*)",
                muleContext));

        // FIXME
        final MuleMessage msg = client.request("ldap://ldap.in", 15000);

        assertNotNull(msg);
        assertTrue(msg.getPayload() instanceof LDAPMessage);

        try
        {
            DSManager.getInstance().stop();
        }
        catch (final Exception e)
        {

        }

    }

    public void testAddSearch() throws Exception
    {
        final MuleClient client = new MuleClient(muleContext);

        final int addCount = 4;

        for (int i = 0; i < addCount; i++)
        {
            client.dispatch("ldap://ldap.out", TestHelper
                    .getRandomEntryAddRequest(), null);
        }

        Thread.sleep(1000);

        final MuleMessage msg = client.send("vm://test_in_async",
                new DefaultMuleMessage("(cn=*)", muleContext));

        assertNotNull(msg);
        assertTrue(msg.getPayload() instanceof String);

        // logger.debug(msg.getPayload());

        assertTrue(msg.getPayloadAsString().indexOf("<batchResponse") > -1);
        assertTrue(msg.getPayloadAsString().indexOf(
                "<searchResultEntry dn=\"cn=test-cn") > -1);

        assertTrue(org.apache.commons.lang.StringUtils.countMatches(msg
                .getPayloadAsString(), "test-cn-") >= addCount);
        try
        {
            DSManager.getInstance().stop();
        }
        catch (final Exception e)
        {

        }
    }

    public void testJavaBeanModificationRequest() throws Exception
    {
        final MuleClient client = new MuleClient(muleContext);

        final LDAPAddRequest add = TestHelper.getRandomEntryAddRequest();

        client.send("ldap://ldap.out", add, null);

        final Bean bean = new Bean();

        bean.setDn(add.getEntry().getDN());
        bean.setMail("hsaly@mulesource.org");
        bean.setDescription("desc");

        client.send("vm://test_in_bean", new DefaultMuleMessage(bean,muleContext));

        final MuleMessage msg = client.send("ldap://ldap.out",
                new DefaultMuleMessage(new DN(add.getEntry().getDN()),muleContext));

        assertNotNull(msg);

        assertTrue(msg.getPayload() instanceof LDAPEntry);

        final LDAPEntry res = (LDAPEntry) msg.getPayload();

        assertTrue(res.getDN().equals(add.getEntry().getDN()));

        logger.debug("dn:: " + res.getDN());

        assertNotNull(res.getAttribute("mail"));
        assertTrue(res.getAttribute("mail").getStringValue().equals(
                bean.getMail()));
        assertNotNull(res.getAttribute("description"));
        assertTrue(res.getAttribute("description").getStringValue().equals(
                bean.getDescription()));
        try
        {
            DSManager.getInstance().stop();
        }
        catch (final Exception e)
        {

        }
    }

    public static class Bean
    {

        private String dn;
        private String description;
        private String mail;

        public String getDn()
        {
            return dn;
        }

        public void setDn(final String dn)
        {
            this.dn = dn;
        }

        public String getDescription()
        {
            return description;
        }

        public void setDescription(final String description)
        {
            this.description = description;
        }

        public String getMail()
        {
            return mail;
        }

        public void setMail(final String mail)
        {
            this.mail = mail;
        }

    }

    public static class BeanWithoutDn
    {

        private String description;
        private String mail;

        public String getDescription()
        {
            return description;
        }

        public void setDescription(final String description)
        {
            this.description = description;
        }

        public String getMail()
        {
            return mail;
        }

        public void setMail(final String mail)
        {
            this.mail = mail;
        }

    }

}
