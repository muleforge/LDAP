package org.mule.transport.ldap;

import org.mule.api.transport.Connector;
import org.mule.transport.AbstractConnectorTestCase;
import org.mule.transport.ldap.util.DSManager;

import com.novell.ldap.LDAPDeleteRequest;

public class LdapSASLConnectorTestCase extends AbstractConnectorTestCase
{

    private static volatile String password = "secret1";
    private static volatile String mechanism = "DIGEST-MD5";

    @Override
    public Connector createConnector() throws Exception
    {

        logger.debug("create called (" + password + "||" + mechanism + ")");

        return defineConnector(password, "example.com", mechanism, 10389);
    }

    public Connector defineConnector(final String password, final String realm,
            final String mechanism, final int port) throws Exception
    {

        logger.debug("defineConnector (" + password + "||" + mechanism + ")");

        final LdapSASLConnector c = new LdapSASLConnector();
        c.setLdapHost("localhost");
        c.setLdapPort(port);
        c.setName("ldapSASLTestConnector");
        c.setLoginDN("hsaly");
        c.setPassword(password);
        c.setSearchBase("dc=example,dc=com");
        c.setStartUnsolicitedNotificationListener(false);
        c.setRealm(realm);
        c.setMechanism(mechanism);
        c.setTrustAll(true);

        // c.initialise();
        return c;
    }

    @Override
    public String getTestEndpointURI()
    {

        return "ldap://ldap.in";
    }

    @Override
    public Object getValidMessage() throws Exception
    {

        return new LDAPDeleteRequest("o=sevenSeas", null);
    }

    @Override
    protected void doSetUp() throws Exception
    {
        DSManager.getInstance().start();

        System.out.println(this.getTestInfo().getName());

        super.doSetUp();

    }

    @Override
    protected void doTearDown() throws Exception
    {
        super.doTearDown();
        DSManager.getInstance().stop();

    }

}
