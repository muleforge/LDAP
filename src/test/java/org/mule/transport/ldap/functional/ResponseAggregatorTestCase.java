package org.mule.transport.ldap.functional;

import java.util.List;
import java.util.Map;

import org.mule.DefaultMuleMessage;
import org.mule.MuleServer;
import org.mule.api.MuleMessage;
import org.mule.module.client.MuleClient;
import org.mule.transport.ldap.util.DSManager;
import org.mule.transport.ldap.util.TestHelper;

import com.novell.ldap.LDAPAddRequest;

public class ResponseAggregatorTestCase extends AbstractLdapFunctionalTestCase
{

    @Override
    protected String getConfigResources()
    {

        return super.getConfigResources() + ",ResponseAggregatorTest.xml";
    }

    public void testAsyncResponse() throws Exception
    {
        final MuleClient client = new MuleClient(muleContext);

        final int addCount = 4;

        for (int i = 0; i < addCount; i++)
        {
            final MuleMessage ret = client.send("ldap://ldap.out", TestHelper
                    .getRandomEntryAddRequest(), null);
            assertNotNull(ret);
            assertTrue(ret.getPayload().getClass().toString(),
                    ret.getPayload() instanceof LDAPAddRequest);
            final LDAPAddRequest lar = ((LDAPAddRequest) ret.getPayload());
            logger.debug(i + ": " + ret + "//" + lar.getTag() + "//"
                    + lar.getEntry());
            // correlationId=null, correlationGroup=-1, correlationSeq=-1
        }

        logger.debug(addCount + " add messages sended");
        // no responses because of synchronous send

        Thread.sleep(5 * 1000);

        final MuleMessage msg = client.send("vm://test_in",
                new DefaultMuleMessage("dummy_for_static_search", muleContext));

        assertNotNull(msg);

        logger.debug(msg.getPayload().getClass().toString());

        assertTrue(msg.getPayload().getClass().toString(),
                msg.getPayload() instanceof List);

        final List list = (List) msg.getPayload();

        logger.debug(list);

        assertEquals(list.size(), addCount + 1);

        // Thread.sleep(5 * 1000);

        muleContext.stop();
        // client.dispose();

        try
        {
            DSManager.getInstance().stop();
        }
        catch (final Exception e)
        {

        }

    }

}
