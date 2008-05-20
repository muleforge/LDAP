package org.mule.providers.ldap;

import java.util.Date;

import org.mule.tck.providers.AbstractMessageAdapterTestCase;
import org.mule.umo.MessagingException;
import org.mule.umo.provider.UMOMessageAdapter;

import com.novell.ldap.LDAPDeleteRequest;

public class LdapMessageAdapterTestCase extends AbstractMessageAdapterTestCase
{

    public Object getInvalidMessage()
    {

        return new Date();
    }

    public UMOMessageAdapter createAdapter(Object payload)
            throws MessagingException
    {

        return new LdapMessageAdapter(payload);
    }

    public Object getValidMessage() throws Exception
    {

        return new LDAPDeleteRequest("cn=test", null);
    }

    protected void doSetUp() throws Exception
    {

        super.doSetUp();
    }

    protected void doTearDown() throws Exception
    {

        super.doTearDown();
    }

}
