package org.mule.transport.ldap;

import org.mule.api.transport.Connector;
import org.mule.transport.AbstractConnectorTestCase;
import org.mule.transport.ldap.util.DSManager;

import com.novell.ldap.LDAPDeleteRequest;

public class LdapSConnectorTestCase extends AbstractConnectorTestCase
{

    // running

    public Connector getConnector(final boolean trustAll,
            final String trustStore) throws Exception
    {

        final LdapSConnector c = new LdapSConnector(muleContext);
        c.setLdapHost("localhost");
        c.setLdapPort(10636);
        c.setName("ldapSTestConnector");
        c.setLoginDN("uid=admin,ou=system");
        c.setPassword("secret");
        c.setSearchBase("o=sevenSeas");
        c.setStartUnsolicitedNotificationListener(true);
        c.setTrustAll(trustAll);
        c.setTrustStore(trustStore);

        return c;
    }

    @Override
    public Connector createConnector() throws Exception
    {
        // TODO Auto-generated method stub
        // return getConnector(false, "src/test/resources/truststore.jks");
        return getConnector(true, null);
    }

    @Override
    public String getTestEndpointURI()
    {

        return "ldaps://ldap.out";
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
        super.doTearDown();
        DSManager.getInstance().stop();

    }

}
