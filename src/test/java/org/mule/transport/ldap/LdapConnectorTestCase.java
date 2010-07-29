package org.mule.transport.ldap;

import org.mule.api.transport.Connector;
import org.mule.transport.AbstractConnectorTestCase;
import org.mule.transport.ldap.util.DSManager;

import com.novell.ldap.LDAPDeleteRequest;

public class LdapConnectorTestCase extends AbstractConnectorTestCase
{

    // running

    @Override
    public Connector createConnector() throws Exception
    {
        final LdapConnector c = new LdapConnector(muleContext);
        c.setLdapHost("localhost");
        c.setLdapPort(10389);
        c.setName("ldapTestConnector");

        c.setLoginDN("uid=admin,ou=system");
        c.setPassword("secret");

        c.setSearchBase("o=sevenSeas");
        c.setStartUnsolicitedNotificationListener(true);

        return c;
    }

    @Override
    public String getTestEndpointURI()
    {

        return "ldap://ldap.out";
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
        super.doSetUp();

    }

    @Override
    protected void doTearDown() throws Exception
    {
        DSManager.getInstance().stop();
        super.doTearDown();

    }

}
