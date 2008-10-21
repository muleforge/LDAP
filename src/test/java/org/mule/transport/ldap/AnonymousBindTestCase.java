package org.mule.transport.ldap;

import org.mule.tck.AbstractMuleTestCase;
import org.mule.transport.ldap.util.DSManager;

public class AnonymousBindTestCase extends AbstractMuleTestCase
{

    // OK

    public void testAnonymousBindAllowed()
    {

        try
        {
            anonymousBind(true);
        }
        catch (Exception e)
        {

            fail(e.toString());
            e.printStackTrace();
        }

    }

    public void testAnonymousBindNotAllowed()
    {

        try
        {
            anonymousBind(false);
            fail("exception expected");
        }
        catch (Exception e)
        {
            // expected

        }

    }

    public void anonymousBind(boolean allow) throws Exception
    {

        // DSHelper.startDS(allow);
        DSManager.getInstance().start(allow);

        LdapConnector c = new LdapConnector();
        c.setLdapHost("localhost");
        c.setLdapPort(10389);
        c.setName("ldapTestConnector");
        c.setSearchBase("o=sevenSeas");
        c.setStartUnsolicitedNotificationListener(true);

        try
        {

            c.setMuleContext(muleContext);
            muleContext.getRegistry().registerConnector(c);

            // instead of
            // c.initialise();

            c.connect();
            c.ensureConnected();

        }
        finally
        {
            // DSHelper.stopDS();
            DSManager.getInstance().stop();
        }

    }
}
