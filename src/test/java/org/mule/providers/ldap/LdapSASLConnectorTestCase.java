package org.mule.providers.ldap;

import org.mule.api.transport.Connector;
import org.mule.transport.AbstractConnectorTestCase;

import com.novell.ldap.LDAPDeleteRequest;

public class LdapSASLConnectorTestCase extends AbstractConnectorTestCase
{

    public Connector getConnector(String password, String realm,
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

    public Connector getConnector()
    {
        //FIXME
        return null;//getConnector("secret", "example.com", "DIGEST-MD5", 10389);
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

    @Override
    public Connector createConnector() throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

}
