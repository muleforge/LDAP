package org.mule.providers.ldap.transformers;

import java.io.File;
import java.io.IOException;

import org.mule.umo.transformer.UMOTransformer;
//import org.mule.util.FileUtils;

import com.novell.ldap.LDAPDeleteRequest;
import com.novell.ldap.LDAPException;

public class LdapMessageToStringTransformerTestCase extends
        org.mule.tck.AbstractTransformerTestCase
{

    public Object getResultData()
    {
        // TODO Auto-generated method stub
        try
        {
            String file = org.mule.util.FileUtils.readFileToString(new File(
                    "src/test/resources/LDAPDeleteRequest.dsml"));
            System.out.println(file);
            return file;
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public UMOTransformer getRoundTripTransformer() throws Exception
    {
        // TODO Auto-generated method stub
        return new StringToLDAPMessage();
    }

    public Object getTestData()
    {
        // TODO Auto-generated method stub
        try
        {
            return new LDAPDeleteRequest("dn=test,o=toporga", null);
        } catch (LDAPException e)
        {
            throw new RuntimeException(e);
        }
    }

    public UMOTransformer getTransformer() throws Exception
    {
        // TODO Auto-generated method stub
        return new LDAPMessageToString();
    }

    public boolean compareRoundtripResults(Object expected, Object result)
    {

        if (!(expected instanceof LDAPDeleteRequest)
                || !(result instanceof LDAPDeleteRequest))
            return false;

        LDAPDeleteRequest ex = (LDAPDeleteRequest) expected;
        LDAPDeleteRequest res = (LDAPDeleteRequest) result;

        if (ex.getDN().equals(res.getDN()) && ex.getType() == res.getType()
                && ex.isRequest() == res.isRequest())
            return true;

        return false;

        // TODO Auto-generated method stub
        // System.out.println(LDAPUtils.dumpLDAPMessage(expected));
        // System.out.println("resu"+ LDAPUtils.dumpLDAPMessage(result));
    }

}
