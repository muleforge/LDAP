package org.mule.providers.ldap.components;

import java.util.Random;

import org.mule.umo.UMOEventContext;
import org.mule.umo.lifecycle.Callable;

import com.novell.ldap.LDAPAddRequest;
import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPAttributeSet;
import com.novell.ldap.LDAPEntry;

public class RandomEntryComponent implements Callable
{

    private Random rand = new Random(System.currentTimeMillis());

    public Object onCall(UMOEventContext eventContext) throws Exception
    {

        String cn = "hsaly-" + rand.nextInt(Integer.MAX_VALUE);
        String sn = "sn-" + rand.nextInt(Integer.MAX_VALUE);
        LDAPAttributeSet attr = new LDAPAttributeSet();
        attr.add(new LDAPAttribute("cn", cn));
        attr.add(new LDAPAttribute("sn", sn));
        attr.add(new LDAPAttribute("objectClass", "inetOrgPerson"));

        LDAPEntry entry = new LDAPEntry("cn=" + cn
                + ",o=sevenseas", attr);

        return new LDAPAddRequest(entry, null);

    }

}
