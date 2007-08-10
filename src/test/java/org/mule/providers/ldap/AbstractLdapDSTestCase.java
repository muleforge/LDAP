package org.mule.providers.ldap;

import junit.framework.TestCase;

import org.mule.providers.ldap.util.DSManager;

public class AbstractLdapDSTestCase extends TestCase
{

    protected boolean allowAnonymousBind;

    protected AbstractLdapDSTestCase(boolean allowAnonymousBind)
    {

        super();
        this.allowAnonymousBind = allowAnonymousBind;
    }

    protected void setUp() throws Exception
    {

        super.setUp();
        DSManager.getInstance().start(allowAnonymousBind);
    }

    protected void tearDown() throws Exception
    {

        DSManager.getInstance().stop();
        super.tearDown();
    }

}
