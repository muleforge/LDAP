package org.mule.providers.ldap;

import java.util.List;

import org.mule.MuleManager;
import org.mule.extras.client.MuleClient;
import org.mule.impl.MuleMessage;
import org.mule.providers.ldap.transformers.LDAPSearchResultToString;
import org.mule.providers.ldap.util.DSManager;
import org.mule.providers.ldap.util.TestHelper;
import org.mule.tck.FunctionalTestCase;
import org.mule.umo.UMOMessage;
import org.mule.util.StringUtils;

import com.novell.ldap.LDAPAddRequest;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPMessage;
import com.novell.ldap.LDAPSearchResult;
import com.novell.ldap.LDAPSearchResults;
import com.novell.ldap.util.DN;

public class ResponseAggregatorTestCase extends FunctionalTestCase {

	protected String getConfigResources() {

		return "ResponseAggregatorTest.xml";
	}

	public void testAsyncResponse() throws Exception {
		MuleClient client = new MuleClient();

		final int addCount = 4;

		for (int i = 0; i < addCount; i++)
			client.send("ldap://ldap.out", TestHelper
					.getRandomEntryAddRequest(), null);

		Thread.sleep(1000);
		
		//logger.debug("add messages sended");

		UMOMessage msg = client.send("vm://test_in", new MuleMessage("dummy_for_static_search"));

		assertNotNull(msg);
				
		//logger.debug(msg.getPayload().getClass().toString());
		
		assertTrue(msg.getPayload().getClass().toString(),msg.getPayload() instanceof List);

		List list = (List) msg.getPayload();
		
		//logger.debug(list);

		assertEquals(list.size(), addCount+1);
		
		client.dispose();
		MuleManager.getInstance().stop();
		MuleManager.getInstance().dispose();

	}

	protected void doFunctionalTearDown() throws Exception {

		DSManager.getInstance().stop();
		super.doFunctionalTearDown();
	}

	protected void doPreFunctionalSetUp() throws Exception {

		DSManager.getInstance().start();
		super.doPreFunctionalSetUp();
	}

}
