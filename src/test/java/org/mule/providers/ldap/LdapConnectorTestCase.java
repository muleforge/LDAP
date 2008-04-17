package org.mule.providers.ldap;

import org.mule.api.transport.Connector;
import org.mule.providers.ldap.util.DSManager;
import org.mule.transport.AbstractConnectorTestCase;

import com.novell.ldap.LDAPDeleteRequest;

public class LdapConnectorTestCase extends AbstractConnectorTestCase
{

    // running

    @Override
    public Connector createConnector() throws Exception
    {
      //FIXME
        return null;
    }

    public Connector getConnector()
    {

        LdapConnector c = new LdapConnector();
        c.setLdapHost("localhost");
        c.setLdapPort(10389);
        c.setName("ldapTestConnector");

        c.setLoginDN("uid=admin,ou=system");
        c.setPassword("secret");

        c.setSearchBase("o=sevenSeas");
        c.setStartUnsolicitedNotificationListener(true);
        
        //FIXME
        //c.initialise();

        return c;
    }

    public String getTestEndpointURI()
    {

        return "ldap://ldap.out";
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
