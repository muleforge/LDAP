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
        // TODO Auto-generated method stub
        return new Date();
    }

    public UMOMessageAdapter createAdapter(Object payload)
            throws MessagingException
    {
        // TODO Auto-generated method stub
        return new LdapMessageAdapter(payload);
    }

    public Object getValidMessage() throws Exception
    {
        // TODO Auto-generated method stub
        return new LDAPDeleteRequest("cn=test", null);
    }

    protected void doSetUp() throws Exception
    {
        // TODO Auto-generated method stub
        super.doSetUp();
    }

    protected void doTearDown() throws Exception
    {
        // TODO Auto-generated method stub
        super.doTearDown();
    }

}
