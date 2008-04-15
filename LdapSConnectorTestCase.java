package org.mule.providers.ldap;

import org.mule.providers.ldap.util.DSManager;
import org.mule.tck.providers.AbstractConnectorTestCase;
import org.mule.umo.provider.UMOConnector;

import com.novell.ldap.LDAPDeleteRequest;

public class LdapSConnectorTestCase extends AbstractConnectorTestCase
{

    // running

    public UMOConnector getConnector(boolean trustAll, String trustStore)
            throws Exception
    {

        LdapSConnector c = new LdapSConnector();
        c.setLdapHost("localhost");
        c.setLdapPort(10636);
        c.setName("ldapSTestConnector");
        c.setLoginDN("uid=admin,ou=system");
        c.setPassword("secret");
        c.setSearchBase("o=sevenSeas");
        c.setStartUnsolicitedNotificationListener(true);
        c.setTrustAll(trustAll);
        c.setTrustStore(trustStore);
        c.initialise();
        return c;
    }

    public UMOConnector getConnector() throws Exception
    {
        return getConnector(true, null);
    }

    public void testSSLConnectTrustAll() throws Exception
    {
        LdapSConnector c = (LdapSConnector) getConnector();
        c.connect();
        c.ensureConnected();
        c.disconnect();
        c.dispose();
    }

    public void testSSLConnectTrustNotAllWithoutTruststore() throws Exception
    {
        LdapSConnector c = null;
        try
        {
            c = (LdapSConnector) getConnector(false, null);
            fail();
        }
        catch (Exception e)
        {
            // expected
        }

        assertNull(c);
    }

    public void testSSLConnectTrustNotAll() throws Exception
    {
        LdapSConnector c = (LdapSConnector) getConnector(false,
                "src/test/resources/truststore.jks");
        c.connect();
        c.ensureConnected();
        c.disconnect();
        c.dispose();
    }

    public String getTestEndpointURI()
    {

        return "ldaps://ldap.out";
    }

    public Object getValidMessage() throws Exception
    {

        return new LDAPDeleteRequest("o=sevenSeas", null);
    }

    protected void doSetUp() throws Exception
    {
        super.doSetUp();
        // DSHelper.startDS();
        DSManager.getInstance().start();

    }

    protected void doTearDown() throws Exception
    {
        DSManager.getInstance().stop();
        // DSHelper.stopDS();
        super.doTearDown();

    }

}
