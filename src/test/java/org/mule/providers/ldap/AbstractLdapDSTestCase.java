package org.mule.providers.ldap;

import junit.framework.TestCase;

import org.mule.providers.ldap.util.DSManager;

public class AbstractLdapDSTestCase extends TestCase
{

    protected boolean allowAnonymousBind;

    public AbstractLdapDSTestCase(boolean allowAnonymousBind)
    {
        super();
        this.allowAnonymousBind = allowAnonymousBind;
    }

    protected void setUp() throws Exception
    {
        // TODO Auto-generated method stub
        super.setUp();
        //DSHelper.startDS(allowAnonymousBind);
        DSManager.getInstance().start(allowAnonymousBind);
    }

    protected void tearDown() throws Exception
    {
        //DSHelper.stopDS();
        DSManager.getInstance().stop();
        super.tearDown();
    }

}
