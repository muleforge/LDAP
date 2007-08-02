package org.mule.providers.ldap.util;

import com.novell.ldap.LDAPAddRequest;
import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPAttributeSet;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;

public class TestHelper
{

    protected static long inc = 0;

    public static LDAPAddRequest getRandomEntryAddRequest()
            throws LDAPException
    {

        return new LDAPAddRequest(getRandomEntry(), null);

    }

    public static LDAPEntry getRandomEntry()
    {

        String cn = "test-cn-" + (inc++);
        String sn = "test-sn-" + (inc++);
        LDAPAttributeSet attr = new LDAPAttributeSet();
        attr.add(new LDAPAttribute("cn", cn));
        attr.add(new LDAPAttribute("sn", sn));
        attr.add(new LDAPAttribute("objectClass", "inetOrgPerson"));

        LDAPEntry entry = new LDAPEntry("cn=" + cn + ",o=sevenseas", attr);

        return entry;

    }

}
