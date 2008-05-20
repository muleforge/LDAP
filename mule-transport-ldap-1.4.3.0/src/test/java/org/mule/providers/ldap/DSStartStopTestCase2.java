package org.mule.providers.ldap;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.providers.ldap.util.DSManager;

public class DSStartStopTestCase2 extends TestCase
{

    /**
     * logger used by this class
     */
    protected final Log logger = LogFactory.getLog(getClass());

    public void testDSStartStop() throws Exception
    {
        DSManager m = DSManager.getInstance();

        logger.debug(m.getClass().getClassLoader());

        assertTrue(DSManager.checkSocketNotConnected());

        m.start();

        assertFalse(DSManager.checkSocketNotConnected());

        m.stop();

        assertTrue(DSManager.checkSocketNotConnected());

        m.start();

        assertFalse(DSManager.checkSocketNotConnected());

        m.stop();

        assertTrue(DSManager.checkSocketNotConnected());

        m.start();

        assertFalse(DSManager.checkSocketNotConnected());

        m.stop();

        assertTrue(DSManager.checkSocketNotConnected());

    }

    public void testDSAlreadyRunning() throws Exception
    {
        DSManager m = DSManager.getInstance();

        assertTrue(DSManager.checkSocketNotConnected());

        m.start();

        try
        {
            m.start();
            fail();
        }
        catch (RuntimeException e)
        {
            // expected
        }

        assertFalse(DSManager.checkSocketNotConnected());

        m.stop();

        m.stop();

        assertTrue(DSManager.checkSocketNotConnected());

    }

}
