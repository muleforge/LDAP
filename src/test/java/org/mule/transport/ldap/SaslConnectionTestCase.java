package org.mule.transport.ldap;

import org.mule.tck.AbstractMuleTestCase;
import org.mule.transport.ldap.util.DSManager;

import com.novell.ldap.client.Debug;

public class SaslConnectionTestCase extends AbstractMuleTestCase
{

    public SaslConnectionTestCase()
    {
        super();

    }

    public LdapSASLConnector getConnector(final String password,
            final String realm, final String mechanism, final int port)
            throws Exception
    {

        final LdapSASLConnector c = new LdapSASLConnector(muleContext);

        //c.setMuleContext(muleContext);

        c.setLdapHost("localhost");
        c.setLdapPort(port);
        c.setName("ldapSASLTestConnector1");
        c.setLoginDN("hsaly");
        c.setPassword(password);
        c.setSearchBase("dc=example,dc=com");
        c.setStartUnsolicitedNotificationListener(false);

        // SASL

        c.setMechanism(mechanism);
        c.setRealm(realm);
        c.setUseSSL(false);
        c.setTrustAll(true);
        c.setTrustStore(null);

        c.setForceJDK14(false);
        c.setAlternativeSaslProvider(null);
        return c;
    }

    public void testSASLDIGESTMD5Connect() throws Exception
    {

        final LdapSASLConnector c = getConnector("secret1", "example.com",
                "DIGEST-MD5", 10389);

        c.initialise();
        c.connect();
        c.ensureConnected();
        c.disconnect();
        c.dispose();
    }

    public void testSASLDIGESTMD5ConnectViaSSL() throws Exception
    {

        final LdapSASLConnector c = getConnector("secret1", "example.com",
                "DIGEST-MD5", 10636);

        c.setUseSSL(true);

        c.initialise();
        c.connect();
        c.ensureConnected();
        c.disconnect();
        c.dispose();
    }

    public void testSASLDIGESTMD5ConnectViaStartTLS() throws Exception
    {

        final LdapSASLConnector c = getConnector("secret1", "example.com",
                "DIGEST-MD5", 10389);

        c.setStartTLS(true);

        c.initialise();
        c.connect();
        c.ensureConnected();
        c.disconnect();
        c.dispose();
    }

    public void testSASLPLAINConnect() throws Exception
    {

        final LdapSASLConnector c = getConnector("secret1", "example.com",
                "PLAIN", 10389);

        c.initialise();
        c.connect();
        c.ensureConnected();
        c.disconnect();
        c.dispose();
    }

    public void testSASLPLAINConnectViaSSL() throws Exception
    {

        final LdapSASLConnector c = getConnector("secret1", "example.com",
                "PLAIN", 10636);

        c.setUseSSL(true);

        c.initialise();
        c.connect();
        c.ensureConnected();
        c.disconnect();
        c.dispose();
    }

    public void testSASLPLAINConnectViaStartTLS() throws Exception
    {

        final LdapSASLConnector c = getConnector("secret1", "example.com",
                "PLAIN", 10389);

        c.setStartTLS(true);

        c.initialise();
        c.connect();
        c.ensureConnected();
        c.disconnect();
        c.dispose();
    }

    public void testSASLCRAMMD5Connect() throws Exception
    {

        Debug.setTrace(Debug.saslBind, true);
        Debug.setTrace(Debug.bindSemaphore, true);
        Debug.setTraceStream(System.err);

        final LdapSASLConnector c = getConnector("secret1", "example.com",
                "CRAM-MD5", 10389);

        c.initialise();
        c.connect();
        c.ensureConnected();
        c.disconnect();
        c.dispose();
    }

    public void testSASLCRAMMD5ConnectViaSSL() throws Exception
    {

        final LdapSASLConnector c = getConnector("secret1", "example.com",
                "CRAM-MD5", 10636);

        c.setUseSSL(true);

        c.initialise();
        c.connect();
        c.ensureConnected();
        c.disconnect();
        c.dispose();
    }

    public void testSASLCRAMMD5ConnectViaStartTLS() throws Exception
    {

        final LdapSASLConnector c = getConnector("secret1", "example.com",
                "CRAM-MD5", 10389);

        c.setStartTLS(true);

        c.initialise();
        c.connect();
        c.ensureConnected();
        c.disconnect();
        c.dispose();
    }

    public void testSASLBadSSL() throws Exception
    {

        try
        {

            final LdapSASLConnector c = getConnector("secret1", "example.com",
                    "DIGEST-MD5", 10636);

            c.setUseSSL(false);
            c.connect();

            assertTrue(c.isConnected());
            assertTrue(c.isStarted());
            // c.doAsyncRequest(null);
            c.ensureConnected();
            c.disconnect();
            c.dispose();
            fail();
        }
        catch (final Exception e)
        {
            // excpected
            logger.debug(e.toString());

        }

    }

    public void testSASLBadStartTLS() throws Exception
    {

        try
        {

            final LdapSASLConnector c = getConnector("secret1", "example.com",
                    "DIGEST-MD5", 10389);

            c.setStartTLS(true);
            c.connect();

            assertTrue(c.isConnected());
            assertTrue(c.isStarted());
            // c.doAsyncRequest(null);
            c.ensureConnected();
            c.disconnect();
            c.dispose();
            fail();
        }
        catch (final Exception e)
        {
            // excpected
            logger.debug(e.toString());

        }

    }

    public void testSASLBadPassword() throws Exception
    {

        try
        {

            final LdapSASLConnector c = getConnector("xxx", "example.com",
                    "DIGEST-MD5", 10389);

            c.connect();

            assertTrue(c.isConnected());
            assertTrue(c.isStarted());
            // c.doAsyncRequest(null);
            c.ensureConnected();
            c.disconnect();
            c.dispose();
            fail();
        }
        catch (final Exception e)
        {
            // excpected

        }

    }

    public void testUnknownMechanism() throws Exception
    {
        try
        {

            final LdapSASLConnector c = getConnector("secret1", "example.com",
                    "unkjj", 10389);

            c.connect();

            assertTrue(c.isConnected());
            assertTrue(c.isStarted());

            c.ensureConnected();
            c.disconnect();
            c.dispose();
            fail();

        }
        catch (final Exception e)
        {
            // excpected
        }
    }

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
        ;
        DSManager.getInstance().stop();

    }

}
