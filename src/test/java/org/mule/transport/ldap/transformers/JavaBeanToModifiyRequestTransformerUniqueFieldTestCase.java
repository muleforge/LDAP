package org.mule.transport.ldap.transformers;

import java.util.Properties;

import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.api.transformer.Transformer;
import org.mule.api.transport.Connector;
import org.mule.endpoint.DefaultOutboundEndpoint;
import org.mule.endpoint.MuleEndpointURI;
import org.mule.retry.policies.NoRetryPolicyTemplate;
import org.mule.transport.ldap.LdapConnector;
import org.mule.transport.ldap.util.DSManager;

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

    @Override
    public Object getResultData()
    {

        final org.mule.transport.ldap.functional.LdapListenerSynchronTestCase.BeanWithoutDn bean = (org.mule.transport.ldap.functional.LdapListenerSynchronTestCase.BeanWithoutDn) getTestData();

        try
        {

            final LDAPModification[] mods = new LDAPModification[2];

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
        catch (final LDAPException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }

    }

    @Override
    public Object getTestData()
    {

        final org.mule.transport.ldap.functional.LdapListenerSynchronTestCase.BeanWithoutDn bean = new org.mule.transport.ldap.functional.LdapListenerSynchronTestCase.BeanWithoutDn();
        bean.setDescription("test");
        bean.setMail("mail@mail.com");
        return bean;

    }

    protected String getUniqueField()
    {
        return "mail";
    }

    @Override
    public Transformer getTransformer() throws Exception
    {
        final JavaBeanToModifyRequest trans = new JavaBeanToModifyRequest();

        trans.setUniqueField(getUniqueField());

        //
        final Connector connector = getConnector();
        final MuleEndpointURI url = new MuleEndpointURI("ldap://ldap.out");
        final ImmutableEndpoint ep = new DefaultOutboundEndpoint(connector,
                url, null, null, "testendpoint", new Properties(), null, null,
                true, null, false, false, 0, null, null, null, null,
                new NoRetryPolicyTemplate());

        final LDAPAttributeSet attr = new LDAPAttributeSet();
        attr.add(new LDAPAttribute("cn", "test-cn-javabean"));
        attr.add(new LDAPAttribute("sn", "test-sn-javabean"));
        attr.add(new LDAPAttribute("objectClass", "inetOrgPerson"));
        attr.add(new LDAPAttribute("mail", "mail@mail.com"));

        final LDAPEntry entry = new LDAPEntry(
                "cn=test-cn-javabean,o=sevenseas", attr);

        final LDAPConnection con = new LDAPConnection();
        con.connect("localhost", 10389);
        con.bind(LDAPConnection.LDAP_V3, "uid=admin,ou=system", "secret"
                .getBytes());
        con.add(entry);
        con.disconnect();

        trans.setEndpoint(ep);

        //

        return trans;
    }

    public Connector getConnector() throws Exception
    {

        final LdapConnector c = new LdapConnector();
        c.setRetryPolicyTemplate(new NoRetryPolicyTemplate());
        c.setMuleContext(muleContext);
        c.setLdapHost("localhost");
        c.setLdapPort(10389);
        c.setName("ldapTestConnector");

        c.setLoginDN("uid=admin,ou=system");
        c.setPassword("secret");

        c.setSearchBase("o=sevenSeas");
        c.setStartUnsolicitedNotificationListener(true);
        // c.initialise();
        c.connect();

        return c;
    }

    @Override
    protected void doSetUp() throws Exception
    {

        super.doSetUp();
        DSManager.getInstance().start();
    }

    @Override
    protected void doTearDown() throws Exception
    {
        DSManager.getInstance().stop();
        super.doTearDown();
    }

}
