package org.mule.providers.ldap;

import org.mule.providers.ldap.util.DSHelper;
import org.mule.tck.providers.AbstractConnectorTestCase;
import org.mule.umo.provider.UMOConnector;

import com.novell.ldap.LDAPDeleteRequest;

public class LdapConnectorTestCase extends AbstractConnectorTestCase
{

    // running

    public UMOConnector getConnector() throws Exception
    {

        LdapConnector c = new LdapConnector();
        c.setLdapHost("localhost");
        c.setLdapPort(10389);
        c.setName("ldapTestConnector");

        c.setLoginDN("uid=admin,ou=system");
        c.setPassword("secret");

        c.setSearchBase("o=sevenSeas");
        c.setStartUnsolicitedNotificationListener(true);
        c.initialise();

        return c;
    }

    public String getTestEndpointURI()
    {
        // TODO Auto-generated method stub
        return "ldap://ldap.out";
    }

    public Object getValidMessage() throws Exception
    {
        // TODO Auto-generated method stub
        return new LDAPDeleteRequest("o=sevenSeas", null);
    }

    protected void doSetUp() throws Exception
    {
        super.doSetUp();
        DSHelper.startDS();

    }

    protected void doTearDown() throws Exception
    {
        DSHelper.stopDS();
        super.doTearDown();

    }

}