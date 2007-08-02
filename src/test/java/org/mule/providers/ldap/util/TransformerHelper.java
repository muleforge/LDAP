package org.mule.providers.ldap.util;

import java.io.File;

import com.novell.ldap.LDAPDeleteRequest;
import com.novell.ldap.util.DSMLWriter;

public class TransformerHelper
{

    public static void main(String[] args) throws Exception
    {

        // DSMLWriter writer = new
        // DSMLWriter("G:\\j2ee-test\\mule-svn\\mule\\transports\\ldap\\src\\test\\resources\\LDAPDeleteRequest.dsml");
        String file = "src/test/resources/LDAPDeleteRequest.dsml";
        File fileFile = new File(file);
        if (fileFile.exists())
            fileFile.delete();

        DSMLWriter writer = new DSMLWriter(file);

        writer.writeMessage(new LDAPDeleteRequest("dn=test,o=toporga", null));
        writer.finish();

    }

}
