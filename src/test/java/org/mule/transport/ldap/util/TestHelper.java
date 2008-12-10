package org.mule.transport.ldap.util;

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

    public static LDAPAddRequest getRandomEntrySysAddRequest()
            throws LDAPException
    {

        return new LDAPAddRequest(getRandomEntrySys(), null);

    }

    public static LDAPEntry getRandomEntry()
    {

        final String cn = "test-cn-" + (inc++);
        final String sn = "test-sn-" + (inc++);
        final LDAPAttributeSet attr = new LDAPAttributeSet();
        attr.add(new LDAPAttribute("cn", cn));
        attr.add(new LDAPAttribute("sn", sn));
        attr.add(new LDAPAttribute("objectClass", "inetOrgPerson"));

        final LDAPEntry entry = new LDAPEntry("cn=" + cn + ",o=sevenseas", attr);

        return entry;

    }

    public static LDAPEntry getRandomEntrySys()
    {

        final String cn = "test-cn-" + (inc++);
        final String sn = "test-sn-" + (inc++);
        final String uid = "test-uid-" + (inc++);
        final LDAPAttributeSet attr = new LDAPAttributeSet();
        attr.add(new LDAPAttribute("cn", cn));
        attr.add(new LDAPAttribute("sn", sn));
        attr.add(new LDAPAttribute("uid", uid));
        attr.add(new LDAPAttribute("objectClass", "inetOrgPerson"));

        final LDAPEntry entry = new LDAPEntry("uid=" + cn + ",ou=system", attr);

        return entry;

    }

}
