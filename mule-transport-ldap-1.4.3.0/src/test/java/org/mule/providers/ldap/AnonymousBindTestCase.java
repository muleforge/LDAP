package org.mule.providers.ldap;

import junit.framework.TestCase;

import org.mule.providers.ldap.util.DSManager;

public class AnonymousBindTestCase extends TestCase
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
            e.printStackTrace();
            fail(e.toString());
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
            c.initialise();
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
