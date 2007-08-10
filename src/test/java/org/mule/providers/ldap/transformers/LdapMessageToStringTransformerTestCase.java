package org.mule.providers.ldap.transformers;

import java.io.File;
import java.io.IOException;

import org.mule.providers.ldap.util.LDAPUtils;
import org.mule.umo.transformer.UMOTransformer; //import org.mule.util.FileUtils;

import com.novell.ldap.LDAPDeleteRequest;
import com.novell.ldap.LDAPException;

public class LdapMessageToStringTransformerTestCase extends
        org.mule.tck.AbstractTransformerTestCase
{

    public Object getResultData()
    {

        try
        {
            String file = org.mule.util.FileUtils.readFileToString(new File(
                    "src/test/resources/LDAPDeleteRequest.dsml"));
            logger.debug(file);
            return file;
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public UMOTransformer getRoundTripTransformer() throws Exception
    {

        return new StringToLDAPMessage();
    }

    public Object getTestData()
    {

        try
        {
            return new LDAPDeleteRequest("dn=test,o=toporga", null);
        }
        catch (LDAPException e)
        {
            throw new RuntimeException(e);
        }
    }

    public UMOTransformer getTransformer() throws Exception
    {

        return new LDAPMessageToString();
    }

    public boolean compareRoundtripResults(Object expected, Object result)
    {
        logger.debug(LDAPUtils.dumpLDAPMessage(expected));
        logger.debug("resu" + LDAPUtils.dumpLDAPMessage(result));

        if (!(expected instanceof LDAPDeleteRequest)
                || !(result instanceof LDAPDeleteRequest))
            return false;

        LDAPDeleteRequest ex = (LDAPDeleteRequest) expected;
        LDAPDeleteRequest res = (LDAPDeleteRequest) result;

        if (ex.getDN().equals(res.getDN()) && ex.getType() == res.getType()
                && ex.isRequest() == res.isRequest())
            return true;

        return false;

    }

}
