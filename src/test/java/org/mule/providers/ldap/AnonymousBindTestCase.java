package org.mule.providers.ldap;

import org.mule.providers.ldap.util.DSHelper;

import junit.framework.TestCase;

public class AnonymousBindTestCase extends TestCase
{

    // OK

    public void testAnonymousBindAllowed()
    {

        try
        {
            anonymousBind(true);
        } catch (Exception e)
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
        } catch (Exception e)
        {
            // expected

        }

    }

    public void anonymousBind(boolean allow) throws Exception
    {

        DSHelper.startDS(allow);

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

        } finally
        {
            DSHelper.stopDS();
        }

    }
}
