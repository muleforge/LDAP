package org.mule.transport.ldap;

import junit.framework.TestCase;

import org.mule.transport.ldap.util.DSManager;

public class AbstractLdapDSTestCase extends TestCase
{

    protected boolean allowAnonymousBind;

    protected AbstractLdapDSTestCase(final boolean allowAnonymousBind)
    {

        super();
        this.allowAnonymousBind = allowAnonymousBind;
    }

    @Override
    protected void setUp() throws Exception
    {

        super.setUp();
        DSManager.getInstance().start(allowAnonymousBind);
    }

    @Override
    protected void tearDown() throws Exception
    {

        DSManager.getInstance().stop();
        super.tearDown();
    }

}
