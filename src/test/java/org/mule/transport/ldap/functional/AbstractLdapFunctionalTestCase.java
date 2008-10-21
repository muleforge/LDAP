package org.mule.transport.ldap.functional;

import org.mule.tck.FunctionalTestCase;
import org.mule.transport.ldap.util.DSManager;

public abstract class AbstractLdapFunctionalTestCase extends FunctionalTestCase
{

    // protected LdapConnector ldapConnector = null;

    protected String getConfigResources()
    {

        try
        {
            DSManager.getInstance().stop();
        }
        catch (Exception e)
        {

        }

        try
        {
            DSManager.getInstance().start();
            // ldapConnector = (LdapConnector)
            // muleContext.getRegistry().lookupConnector("ldapConnector");

        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "ldap-connector.xml";
    }

}
