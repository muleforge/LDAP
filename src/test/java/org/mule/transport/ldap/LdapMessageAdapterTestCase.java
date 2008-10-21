package org.mule.transport.ldap;

import java.util.Date;

import org.mule.api.MessagingException;
import org.mule.api.transport.MessageAdapter;

import com.novell.ldap.LDAPDeleteRequest;

public class LdapMessageAdapterTestCase extends
        org.mule.transport.AbstractMessageAdapterTestCase
{

    public Object getInvalidMessage()
    {

        return new Date();
    }

    public MessageAdapter createAdapter(Object payload)
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
