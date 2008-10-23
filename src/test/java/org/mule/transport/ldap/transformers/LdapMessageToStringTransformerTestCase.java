package org.mule.transport.ldap.transformers;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.mule.api.transformer.Transformer;
import org.mule.transformer.AbstractTransformerTestCase;
import org.mule.transport.ldap.util.LDAPUtils;

import com.novell.ldap.LDAPDeleteRequest;
import com.novell.ldap.LDAPException;

public class LdapMessageToStringTransformerTestCase extends
        AbstractTransformerTestCase
{

    @Override
    public Object getResultData()
    {

        try
        {
            final String file = FileUtils.readFileToString(new File(
                    "src/test/resources/LDAPDeleteRequest.dsml"));
            logger.debug(file);
            return file;
        }
        catch (final IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Transformer getRoundTripTransformer() throws Exception
    {

        return new StringToLDAPMessage();
    }

    @Override
    public Object getTestData()
    {

        try
        {
            return new LDAPDeleteRequest("dn=test,o=toporga", null);
        }
        catch (final LDAPException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Transformer getTransformer() throws Exception
    {

        return new LDAPMessageToString();
    }

    @Override
    public boolean compareRoundtripResults(final Object expected,
            final Object result)
    {
        logger.debug(LDAPUtils.dumpLDAPMessage(expected));
        logger.debug("resu" + LDAPUtils.dumpLDAPMessage(result));

        if (!(expected instanceof LDAPDeleteRequest)
                || !(result instanceof LDAPDeleteRequest))
        {
            return false;
        }

        final LDAPDeleteRequest ex = (LDAPDeleteRequest) expected;
        final LDAPDeleteRequest res = (LDAPDeleteRequest) result;

        if (ex.getDN().equals(res.getDN()) && (ex.getType() == res.getType())
                && (ex.isRequest() == res.isRequest()))
        {
            return true;
        }

        return false;

    }

    // @Override
    @Override
    public boolean compareResults(final Object expected, final Object result)
    {
        logger.debug("compareResults");
        logger.debug(expected);
        logger.debug(result);

        if ((expected == null) || (result == null))
        {
            return false;
        }

        final int index = expected.toString().indexOf("requestID");

        if (index == -1)
        {
            return false;
        }

        if (index != result.toString().indexOf("requestID"))
        {
            return false;
        }

        if ((result.toString().indexOf("dn=test,o=toporga") != -1)
                && (expected.toString().indexOf("dn=test,o=toporga") != -1))
        {
            return true;
        }

        return false;

    }

}
