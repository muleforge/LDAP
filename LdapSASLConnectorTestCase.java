package org.mule.providers.ldap;

import org.mule.tck.providers.AbstractConnectorTestCase;
import org.mule.umo.provider.UMOConnector;

import com.novell.ldap.LDAPDeleteRequest;

public class LdapSASLConnectorTestCase extends AbstractConnectorTestCase
{

    public UMOConnector getConnector(String password, String realm,
            String mechanism, int port) throws Exception
    {

        LdapSASLConnector c = new LdapSASLConnector();
        c.setLdapHost("localhost");
        c.setLdapPort(port);
        c.setName("ldapSASLTestConnector");
        c.setLoginDN("admin");
        c.setPassword(password);
        c.setSearchBase("ou=system");
        c.setStartUnsolicitedNotificationListener(false);
        c.setRealm(realm);
        c.setMechanism(mechanism);
        // c.setForceJDK14(true);
        c.setTrustAll(true);
        c.initialise();
        return c;
    }

    public UMOConnector getConnector() throws Exception
    {
        return getConnector("secret", "example.com", "DIGEST-MD5", 10389);
    }

    public void testSASLDIGESTMD5Connect() throws Exception
    {
        LdapSASLConnector c = (LdapSASLConnector) getConnector("secret",
                "example.com", "DIGEST-MD5", 10389);
        c.connect();
        c.ensureConnected();
        c.disconnect();
        c.dispose();
    }

    public void testSASLEXTERNALConnect() throws Exception
    {
        LdapSASLConnector c = (LdapSASLConnector) getConnector("secret",
                "example.com", "EXTERNAL", 10636);
        c.connect();
        c.ensureConnected();
        c.disconnect();
        c.dispose();
    }

    public void testSASLBadPassword() throws Exception
    {
        try
        {
            LdapSASLConnector c = (LdapSASLConnector) getConnector("bad12412",
                    "example.com", "DIGEST-MD5", 10389);
            c.connect();
            c.ensureConnected();
            c.disconnect();
            c.dispose();
            fail();
        }
        catch (Exception e)
        {
            // excpected
        }
    }

    public void testUnknownMechanism() throws Exception
    {
        try
        {
            LdapSASLConnector c = (LdapSASLConnector) getConnector("secret",
                    "example.com", "UNKNOWN-66", 10389);
            c.connect();
            c.ensureConnected();
            c.disconnect();
            c.dispose();
            fail();

        }
        catch (Exception e)
        {
            // excpected
        }
    }

    public String getTestEndpointURI()
    {

        return "ldap://ldap.in";
    }

    public Object getValidMessage() throws Exception
    {

        return new LDAPDeleteRequest("o=sevenSeas", null);
    }

    protected void doSetUp() throws Exception
    {
        super.doSetUp();
        // DSManager.getInstance().start();

    }

    protected void doTearDown() throws Exception
    {
        // DSManager.getInstance().stop();
        super.doTearDown();

    }

}
