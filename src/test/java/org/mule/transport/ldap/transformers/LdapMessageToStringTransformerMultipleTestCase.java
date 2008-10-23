package org.mule.transport.ldap.transformers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.mule.api.transformer.Transformer;
import org.mule.transformer.AbstractTransformerTestCase;

import com.novell.ldap.LDAPAddRequest;
import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPAttributeSet;
import com.novell.ldap.LDAPDeleteRequest;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPMessage;

public class LdapMessageToStringTransformerMultipleTestCase extends
        AbstractTransformerTestCase
{

    @Override
    public Object getResultData()
    {

        try
        {
            final String file = FileUtils.readFileToString(new File(
                    "src/test/resources/LDAPMultipleRequest.dsml"));
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
            final String cn = "hsaly";
            final String sn = "sntest";
            final LDAPAttributeSet attr = new LDAPAttributeSet();
            attr.add(new LDAPAttribute("cn", cn));
            attr.add(new LDAPAttribute("sn", sn));
            attr.add(new LDAPAttribute("objectClass", "inetOrgPerson"));

            final LDAPEntry entry = new LDAPEntry("cn=" + cn + ",o=sevenseas",
                    attr);

            final List list = new ArrayList();

            LDAPMessage msg = new LDAPDeleteRequest("dn=test,o=toporga", null);
            msg.setTag("1");

            list.add(msg);

            msg = new LDAPDeleteRequest("dn=test1,o=toporga", null);
            msg.setTag("2");
            list.add(msg);

            msg = new LDAPAddRequest(entry, null);
            msg.setTag("3");
            list.add(msg);

            msg = new LDAPDeleteRequest("dn=test2,o=toporga", null);
            msg.setTag("4");
            list.add(msg);

            return list;
        }
        catch (final Exception e)
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
        // logger.debug(LDAPUtils.dumpLDAPMessage(expected));
        // logger.debug("resu"+ LDAPUtils.dumpLDAPMessage(result));

        if (!(expected instanceof List) || !(result instanceof List))
        {
            return false;
        }

        final List ex = (List) expected;
        final List res = (List) result;

        if (ex.size() != res.size())
        {
            return false;
        }

        int i = 0;
        for (final Iterator it = res.iterator(); it.hasNext();)
        {
            final LDAPMessage m1 = (LDAPMessage) it.next();
            final LDAPMessage m2 = (LDAPMessage) ex.get(i);

            // logger.debug(m1.getType()+"//"+m2.getType());

            if (m1.getType() != m2.getType())
            {
                return false;
            }

            if (!m1.getTag().equals(m2.getTag()))
            {
                return false;
            }

            i++;
        }

        return true;

    }

}
