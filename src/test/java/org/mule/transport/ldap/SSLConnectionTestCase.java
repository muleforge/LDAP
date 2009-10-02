package org.mule.transport.ldap;

import org.mule.tck.AbstractMuleTestCase;
import org.mule.transport.ldap.util.DSManager;

public class SSLConnectionTestCase extends AbstractMuleTestCase
{

    public SSLConnectionTestCase()
    {
        super();
        // this.setStartContext(true);

    }

    public LdapSConnector getConnector(final boolean trustAll,
            final String trustStore, final String password, final int port)
            throws Exception
    {

        final LdapSConnector c = new LdapSConnector();
        c.setMuleContext(muleContext);
        c.setLdapHost("localhost");
        c.setLdapPort(port);
        c.setName("ldapSTestConnector1");
        c.setLoginDN("uid=admin,ou=system");
        c.setPassword(password);
        c.setSearchBase("o=sevenSeas");
        c.setStartUnsolicitedNotificationListener(false);

        // SSL
        c.setTrustAll(trustAll);
        c.setTrustStore(trustStore);

        return c;
    }

    public void testSSLConnectTrustAll() throws Exception
    {

        DSManager.getInstance().start();

        final LdapSConnector c = this.getConnector(true, null, "secret", 10636);
        c.initialise();
        c.connect();
        c.ensureConnected();
        c.disconnect();
        c.dispose();
    }

    public void testSSLStartTLS() throws Exception
    {

        DSManager.getInstance().start();

        final LdapSConnector c = this.getConnector(true, null, "secret", 10389);
        c.setStartTLS(true);

        c.initialise();
        c.connect();
        c.ensureConnected();
        c.disconnect();
        c.dispose();
    }

    //FIXME fail sometimes, maybe an apache ds bug?
    /*
    public void testSSLConnectToNonSSLPort() throws Exception
    {

        DSManager.getInstance().start();

        LdapSConnector c = null;
        try
        {
            c = this.getConnector(true, null, "secret", 10389);
            c.initialise();
            c.connect();
            c.ensureConnected();
            c.disconnect();
            c.dispose();
            fail();
        }
        catch (final Exception e)
        {
            logger.debug(e.toString());
            assertFalse(c.isStarted());
        }
        finally
        {
            if (c != null)
            {
                try
                {
                    c.dispose();
                }
                catch (final Exception e)
                {

                }
            }

        }
    }*/

    public void testSSLNonexistentKeystore() throws Exception
    {

        DSManager.getInstance().start();

        LdapSConnector c = null;
        try
        {
            c = this.getConnector(false, "bcbcbcvf", "secret", 10389);
            c.initialise();
            c.connect();
            c.ensureConnected();
            c.disconnect();
            c.dispose();
            fail();
        }
        catch (final Exception e)
        {
            logger.debug(e.toString());
            assertFalse(c.isStarted());
        }
        finally
        {
            if (c != null)
            {
                try
                {
                    c.dispose();
                }
                catch (final Exception e)
                {

                }
            }

        }
    }

    public void testSSLConnectBadPassword() throws Exception
    {

        DSManager.getInstance().start();

        LdapSConnector c = null;
        try
        {
            c = this.getConnector(true, null, "xxx", 10636);
            c.initialise();
            c.connect();
            c.ensureConnected();
            c.disconnect();
            c.dispose();
            fail();
        }
        catch (final Exception e)
        {
            assertFalse(c.isStarted());
        }
        finally
        {
            if (c != null)
            {
                try
                {
                    c.dispose();
                }
                catch (final Exception e)
                {

                }
            }

        }
    }

    public void testSSLConnectTrustNotAllWithoutTruststore() throws Exception
    {

        DSManager.getInstance().start();

        LdapSConnector c = null;
        try
        {
            c = this.getConnector(false, null, "secret", 10636);
            c.initialise();
            c.connect();
            c.ensureConnected();
            c.disconnect();
            c.dispose();
            fail();
        }
        catch (final Exception e)
        {
            assertFalse(c.isStarted());
        }
        finally
        {
            if (c != null)
            {
                try
                {
                    c.dispose();
                }
                catch (final Exception e)
                {

                }
            }

        }

    }

    //FIXME fail if its running with other test? Single exec ok
    /*
     * public void testSSLConnectTrustNotAll() throws Exception {
     * 
     * DSManager.getInstance().start();
     * 
     * LdapSConnector c = getConnector(false, "target/truststore_tmp.jks",
     * "secret", 10636); c.initialise(); c.connect();
     * assertTrue(c.isConnected()); c.ensureConnected(); c.disconnect();
     * c.dispose(); }
     */

    @Override
    protected void doSetUp() throws Exception
    {

        DSManager.getInstance().start();
        super.doSetUp();

    }

    @Override
    protected void doTearDown() throws Exception
    {
        super.doTearDown();
        DSManager.getInstance().stop();

    }

}
