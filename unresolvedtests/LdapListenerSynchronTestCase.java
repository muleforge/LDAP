package org.mule.providers.ldap;

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleMessage;
import org.mule.module.client.MuleClient;
import org.mule.providers.ldap.transformers.LDAPSearchResultToString;
import org.mule.providers.ldap.util.DSManager;
import org.mule.providers.ldap.util.TestHelper;

import org.mule.tck.FunctionalTestCase;
import org.mule.util.StringUtils;

import com.novell.ldap.LDAPAddRequest;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPMessage;
import com.novell.ldap.LDAPSearchResults;
import com.novell.ldap.util.DN;

public class LdapListenerSynchronTestCase extends FunctionalTestCase {

	protected String getConfigResources() {

		return "LdapListenerTest.xml";
	}

	public void testSearch() throws Exception {

		MuleClient client = new MuleClient();
		MuleMessage msg = client.send("ldap://ldap.out/", new DefaultMuleMessage(
				"(objectclass=*)"));
		assertNotNull(msg);
		assertTrue(msg.getPayload() instanceof LDAPSearchResults);

		LDAPSearchResultToString trans = new LDAPSearchResultToString();
		String s = (String) trans.transform(msg.getPayload());
		logger.debug(s);
		assertTrue(s.indexOf("<batchResponse") > -1);
		assertTrue(s.indexOf("<searchResultEntry") > -1);
		assertTrue(s.indexOf("dn=\"o=sevenseas\"><attr name=\"o\">") > -1);
	}

	public void testAsyncSearch() throws Exception {

		MuleClient client = new MuleClient();
		client.dispatch("ldap://ldap.out", new DefaultMuleMessage("(cn=*)"));

		//FIXME
		MuleMessage msg = null;//client.receive("ldap://ldap.in", 15000);

		assertNotNull(msg);
		assertTrue(msg.getPayload() instanceof LDAPMessage);
		logger.debug(msg.getPayload());

	}

	public void testAddSearch() throws Exception {
		MuleClient client = new MuleClient();

		final int addCount = 4;

		for (int i = 0; i < addCount; i++)
			client.dispatch("ldap://ldap.out", TestHelper
					.getRandomEntryAddRequest(), null);

		Thread.sleep(1000);

		MuleMessage msg = client.send("vm://test_in_async", new DefaultMuleMessage(
				"(cn=*)"));

		assertNotNull(msg);
		assertTrue(msg.getPayload() instanceof String);

		// logger.debug(msg.getPayload());

		assertTrue(msg.getPayloadAsString().indexOf("<batchResponse") > -1);
		assertTrue(msg.getPayloadAsString().indexOf(
				"<searchResultEntry dn=\"cn=test-cn") > -1);

		assertTrue(StringUtils.countMatches(msg.getPayloadAsString(),
				"test-cn-") >= addCount);

	}

	public void testJavaBeanModificationRequest() throws Exception {
		MuleClient client = new MuleClient();

		LDAPAddRequest add = TestHelper.getRandomEntryAddRequest();

		client.send("ldap://ldap.out", add, null);

		Bean bean = new Bean();

		bean.setDn(add.getEntry().getDN());
		bean.setMail("hsaly@mulesource.org");
		bean.setDescription("desc");

		client.send("vm://test_in_bean", new DefaultMuleMessage(bean));

		MuleMessage msg = client.send("ldap://ldap.out", new DefaultMuleMessage(new DN(
				add.getEntry().getDN())));

		assertNotNull(msg);

		assertTrue(msg.getPayload() instanceof LDAPEntry);

		logger.debug(msg.getPayload());

		LDAPEntry res = (LDAPEntry) msg.getPayload();

		assertTrue(res.getDN().equals(add.getEntry().getDN()));
		assertTrue(res.getAttribute("mail").getStringValue().equals(
				bean.getMail()));
		assertTrue(res.getAttribute("description").getStringValue().equals(
				bean.getDescription()));

	}

	

	public static class Bean {

		private String dn;
		private String description;
		private String mail;

		public String getDn() {
			return dn;
		}

		public void setDn(String dn) {
			this.dn = dn;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getMail() {
			return mail;
		}

		public void setMail(String mail) {
			this.mail = mail;
		}

	}

	public static class BeanWithoutDn {

		private String description;
		private String mail;

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getMail() {
			return mail;
		}

		public void setMail(String mail) {
			this.mail = mail;
		}

	}

    @Override
    protected void doSetUp() throws Exception
    {
        
        DSManager.getInstance().start();
        super.doSetUp();
    }

    @Override
    protected void doTearDown() throws Exception
    {
        DSManager.getInstance().stop();
        super.doTearDown();
    }

}
