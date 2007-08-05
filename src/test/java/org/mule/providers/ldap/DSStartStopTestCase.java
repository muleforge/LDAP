package org.mule.providers.ldap;

import junit.framework.TestCase;

import org.codehaus.stax2.io.EscapingWriterFactory;
import org.mule.providers.ldap.util.DSManager;

public class DSStartStopTestCase extends TestCase
{

    

    public void testDSStartStop() throws Exception
    {
        DSManager m = DSManager.getInstance();
        
        System.out.println(m.getClass().getClassLoader());

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
        } catch (RuntimeException e)
        {
           //expected
        }

        assertFalse(DSManager.checkSocketNotConnected());

        m.stop();
        
        m.stop();
        
        assertTrue(DSManager.checkSocketNotConnected());

    }
    
    

}
