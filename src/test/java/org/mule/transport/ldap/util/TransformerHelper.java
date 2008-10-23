package org.mule.transport.ldap.util;

import java.io.File;

import com.novell.ldap.LDAPDeleteRequest;
import com.novell.ldap.util.DSMLWriter;

public class TransformerHelper
{

    public static void main(final String[] args) throws Exception
    {

        // DSMLWriter writer = new
        // DSMLWriter("G:\\j2ee-test\\mule-svn\\mule\\transports\\ldap\\src\\test\\resources\\LDAPDeleteRequest.dsml");
        final String file = "src/test/resources/LDAPDeleteRequest.dsml";
        final File fileFile = new File(file);
        if (fileFile.exists())
        {
            fileFile.delete();
        }

        final DSMLWriter writer = new DSMLWriter(file);

        writer.writeMessage(new LDAPDeleteRequest("dn=test,o=toporga", null));
        writer.finish();

    }

}
