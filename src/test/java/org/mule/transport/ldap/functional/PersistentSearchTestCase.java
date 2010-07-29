package org.mule.transport.ldap.functional;

import java.util.ArrayList;
import java.util.List;

import org.mule.api.MuleMessage;
import org.mule.module.client.MuleClient;
import org.mule.tck.TestCaseWatchdog;
import org.mule.transport.ldap.util.DSManager;
import org.mule.transport.ldap.util.TestHelper;

public class PersistentSearchTestCase extends AbstractLdapFunctionalTestCase
{

    @Override
    protected String getConfigResources()
    {

        return super.getConfigResources()
                + ",ldap-connector-ps.xml,ldap-functional-config-ps.xml";
    }

    public void testAsyncPSearch() throws Exception
    {
        final MuleClient client = new MuleClient(muleContext);

        final int addCount = 4;

        MuleMessage msg = null;
        final List l = new ArrayList();
        int s = 0;
        do
        {
            msg = client.request("vm://out", 5500);

            if (s == 0)
            {
                for (int i = 0; i < addCount; i++)
                {
                    client.dispatch(
                            "ldap://ldap.out?connector=ldapConnector-ps",
                            TestHelper.getRandomEntryAddRequest(), null);
                }
            }

            s++;

            if (msg != null)
            {
                l.add(msg);
                logger.debug(msg.getPayload().getClass());
            }
        }
        while (((msg != null) || (s <= 15)) && (s < 150));

        logger.debug(l.size());
        logger.debug(l);
        logger.debug(s); // 9
        assertTrue("was " + l.size() + " but expected " + (2 * addCount), l
                .size() == (2 * addCount));

        try
        {
            DSManager.getInstance().stop();
        }
        catch (final Exception e)
        {

        }
    }

    public void testSyncPSearch() throws Exception
    {
        final MuleClient client = new MuleClient(muleContext);
        // TODO FIXME fails
        final int addCount = 4;

        MuleMessage msg = null;
        final List l = new ArrayList();
        int s = 0;
        do
        {
            msg = client.request("vm://out", 5500);

            if (s == 0)
            {
                for (int i = 0; i < addCount; i++)
                {
                    client.send("ldap://ldap.out?connector=ldapConnector-ps",
                            TestHelper.getRandomEntryAddRequest(), null);
                }
            }
            s++;

            if (msg != null)
            {
                l.add(msg);
            }

        }
        while ((msg != null) || ((s <= 15) && (s < 150)));

        logger.debug(l.size());
        logger.debug(l);
        assertTrue("was " + l.size() + " but expected " + (addCount),
                l.size() == addCount);

        try
        {
            DSManager.getInstance().stop();
        }
        catch (final Exception e)
        {

        }
    }
    
    @Override
    protected TestCaseWatchdog createWatchdog()
    {
        // TODO Auto-generated method stub
        return new TestCaseWatchdog(
                10,
                edu.emory.mathcs.backport.java.util.concurrent.TimeUnit.MINUTES,
                this);
    }
}
