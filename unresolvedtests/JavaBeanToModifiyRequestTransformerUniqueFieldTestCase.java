package org.mule.providers.ldap.transformers;

import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.api.transport.Connector;
import org.mule.endpoint.MuleEndpointURI;
import org.mule.providers.ldap.LdapConnector;
import org.mule.providers.ldap.LdapListenerSynchronTestCase;
import org.mule.providers.ldap.util.DSManager;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPAttributeSet;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPModification;
import com.novell.ldap.LDAPModifyRequest;

public class JavaBeanToModifiyRequestTransformerUniqueFieldTestCase extends
        JavaBeanToModifyRequestTransformerTestCase
{

    public Object getResultData()
    {

        LdapListenerSynchronTestCase.BeanWithoutDn bean = (LdapListenerSynchronTestCase.BeanWithoutDn) getTestData();

        try
        {

            LDAPModification[] mods = new LDAPModification[2];

            LDAPModification mod = new LDAPModification(
                    LDAPModification.REPLACE, new LDAPAttribute("description",
                            bean.getDescription()));
            mods[0] = mod;

            mod = new LDAPModification(LDAPModification.REPLACE,
                    new LDAPAttribute("mail", bean.getMail()));
            mods[1] = mod;

            return new LDAPModifyRequest("cn=test-cn-javabean,o=sevenseas",
                    mods, null);
        }
        catch (LDAPException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }

    }

    public Object getTestData()
    {

        LdapListenerSynchronTestCase.BeanWithoutDn bean = new LdapListenerSynchronTestCase.BeanWithoutDn();
        bean.setDescription("test");
        bean.setMail("mail@mail.com");
        return bean;

    }

    protected String getUniqueField()
    {
        return "mail";
    }

    public UMOTransformer getTransformer() throws Exception
    {
        JavaBeanToModifyRequest trans = new JavaBeanToModifyRequest();

        trans.setUniqueField(getUniqueField());

        Connector connector = getConnector();

        ImmutableEndpoint ep = new ImmutableMuleEndpoint("ldap",
                new MuleEndpointURI("ldap://ldap.out"), connector, null,
                ImmutableMuleEndpoint.ENDPOINT_TYPE_SENDER, 0, null, null);

        LDAPAttributeSet attr = new LDAPAttributeSet();
        attr.add(new LDAPAttribute("cn", "test-cn-javabean"));
        attr.add(new LDAPAttribute("sn", "test-sn-javabean"));
        attr.add(new LDAPAttribute("objectClass", "inetOrgPerson"));
        attr.add(new LDAPAttribute("mail", "mail@mail.com"));

        LDAPEntry entry = new LDAPEntry("cn=test-cn-javabean,o=sevenseas", attr);

        LDAPConnection con = new LDAPConnection();
        con.connect("localhost", 10389);
        con.bind(LDAPConnection.LDAP_V3, "uid=admin,ou=system", "secret"
                .getBytes());
        con.add(entry);
        con.disconnect();

        trans.setEndpoint(ep);

        return trans;
    }

    public Connector getConnector() throws Exception
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
        c.connect();

        return c;
    }

    protected void doSetUp() throws Exception
    {

        super.doSetUp();
        DSManager.getInstance().start();
    }

    protected void doTearDown() throws Exception
    {
        DSManager.getInstance().stop();
        super.doTearDown();
    }

}
