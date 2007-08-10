package org.mule.providers.ldap.transformers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mule.umo.transformer.UMOTransformer;

import com.novell.ldap.LDAPAddRequest;
import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPAttributeSet;
import com.novell.ldap.LDAPDeleteRequest;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPMessage;

public class LdapMessageToStringTransformerMultipleTestCase extends
        org.mule.tck.AbstractTransformerTestCase
{

    public Object getResultData()
    {
        // TODO Auto-generated method stub
        try
        {
            String file = org.mule.util.FileUtils.readFileToString(new File(
                    "src/test/resources/LDAPMultipleRequest.dsml"));
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
        // TODO Auto-generated method stub
        return new StringToLDAPMessage();
    }

    public Object getTestData()
    {
        // TODO Auto-generated method stub
        try
        {
            String cn = "hsaly";
            String sn = "sntest";
            LDAPAttributeSet attr = new LDAPAttributeSet();
            attr.add(new LDAPAttribute("cn", cn));
            attr.add(new LDAPAttribute("sn", sn));
            attr.add(new LDAPAttribute("objectClass", "inetOrgPerson"));

            LDAPEntry entry = new LDAPEntry("cn=" + cn + ",o=sevenseas", attr);

            List list = new ArrayList();

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
        catch (Exception e)
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
        // logger.debug(LDAPUtils.dumpLDAPMessage(expected));
        // logger.debug("resu"+ LDAPUtils.dumpLDAPMessage(result));

        if (!(expected instanceof List) || !(result instanceof List))
            return false;

        List ex = (List) expected;
        List res = (List) result;

        if (ex.size() != res.size())
            return false;

        int i = 0;
        for (Iterator it = res.iterator(); it.hasNext();)
        {
            LDAPMessage m1 = (LDAPMessage) it.next();
            LDAPMessage m2 = (LDAPMessage) ex.get(i);

            // logger.debug(m1.getType()+"//"+m2.getType());

            if (m1.getType() != m2.getType())
                return false;

            if (!m1.getTag().equals(m2.getTag()))
                return false;

            i++;
        }

        return true;

    }

}
