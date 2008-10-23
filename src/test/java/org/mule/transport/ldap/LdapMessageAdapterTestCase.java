package org.mule.transport.ldap;

import java.util.Date;

import org.mule.api.MessagingException;
import org.mule.api.transport.MessageAdapter;

import com.novell.ldap.LDAPDeleteRequest;

public class LdapMessageAdapterTestCase extends
        org.mule.transport.AbstractMessageAdapterTestCase
{

    @Override
    public Object getInvalidMessage()
    {

        return new Date();
    }

    @Override
    public MessageAdapter createAdapter(final Object payload)
            throws MessagingException
    {

        return new LdapMessageAdapter(payload);
    }

    @Override
    public Object getValidMessage() throws Exception
    {

        return new LDAPDeleteRequest("cn=test", null);
    }

    @Override
    protected void doSetUp() throws Exception
    {

        super.doSetUp();
    }

    @Override
    protected void doTearDown() throws Exception
    {

        super.doTearDown();
    }

}
