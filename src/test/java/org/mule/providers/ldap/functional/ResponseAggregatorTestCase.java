package org.mule.providers.ldap.functional;

import java.util.List;

import org.apache.directory.server.core.interceptor.context.AddContextPartitionOperationContext;
import org.mule.DefaultMuleMessage;
import org.mule.api.MuleMessage;
import org.mule.module.client.MuleClient;
import org.mule.providers.ldap.util.DSManager;
import org.mule.providers.ldap.util.TestHelper;

import com.novell.ldap.LDAPAddRequest;

public class ResponseAggregatorTestCase extends AbstractLdapFunctionalTestCase {

	protected String getConfigResources() {

		return super.getConfigResources() + ",ResponseAggregatorTest.xml";
	}

	public void testAsyncResponse() throws Exception {
		MuleClient client = new MuleClient(muleContext);
		

		final int addCount = 4;

		for (int i = 0; i < addCount; i++) {
			MuleMessage ret = client.send("ldap://ldap.out", TestHelper
					.getRandomEntryAddRequest(), null);
			assertNotNull(ret);
			assertTrue(ret.getPayload().getClass().toString(),
					ret.getPayload() instanceof LDAPAddRequest);
			LDAPAddRequest lar =	((LDAPAddRequest) ret.getPayload());
			logger.debug(i+": "+ret+"//"+ lar.getTag() +"//"+lar.getEntry());
			//correlationId=null, correlationGroup=-1, correlationSeq=-1
		}

		logger.debug(addCount + " add messages sended");
		//no responses because of synchronous send

		Thread.sleep(5 * 1000);

		MuleMessage msg = client.send("vm://test_in", new DefaultMuleMessage(
				"dummy_for_static_search"));

		assertNotNull(msg);

		logger.debug(msg.getPayload().getClass().toString());

		assertTrue(msg.getPayload().getClass().toString(),
				msg.getPayload() instanceof List);

		List list = (List) msg.getPayload();

		logger.debug(list);

		assertEquals(list.size(), addCount + 1);

		client.dispose();
		// MuleManager.getInstance().stop();
		// MuleManager.getInstance().dispose();

		try {
			DSManager.getInstance().stop();
		} catch (Exception e) {

		}

	}

}