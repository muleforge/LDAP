package org.mule.transport.ldap.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.Security;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.directory.server.constants.ServerDNConstants;
import org.apache.directory.server.core.CoreSession;
import org.apache.directory.server.core.DefaultDirectoryService;
import org.apache.directory.server.core.DirectoryService;
import org.apache.directory.server.core.entry.DefaultServerEntry;
import org.apache.directory.server.core.entry.ServerEntry;
import org.apache.directory.server.core.interceptor.context.AddOperationContext;
import org.apache.directory.server.core.jndi.CoreContextFactory;
import org.apache.directory.server.core.partition.Partition;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmPartition;
import org.apache.directory.server.core.security.TlsKeyGenerator;
import org.apache.directory.server.ldap.LdapService;
import org.apache.directory.server.ldap.handlers.bind.MechanismHandler;
import org.apache.directory.server.ldap.handlers.bind.cramMD5.CramMd5MechanismHandler;
import org.apache.directory.server.ldap.handlers.bind.digestMD5.DigestMd5MechanismHandler;
import org.apache.directory.server.ldap.handlers.bind.gssapi.GssapiMechanismHandler;
import org.apache.directory.server.ldap.handlers.bind.ntlm.NtlmMechanismHandler;
import org.apache.directory.server.ldap.handlers.bind.plain.PlainMechanismHandler;
import org.apache.directory.server.ldap.handlers.extended.StartTlsHandler;
import org.apache.directory.server.ldap.handlers.extended.StoredProcedureExtendedOperationHandler;
import org.apache.directory.server.ldap.handlers.ssl.ServerX509TrustManager;
import org.apache.directory.server.protocol.shared.SocketAcceptor;
import org.apache.directory.shared.ldap.constants.SupportedSaslMechanisms;
import org.apache.directory.shared.ldap.entry.EntryAttribute;
import org.apache.directory.shared.ldap.exception.LdapConfigurationException;
import org.apache.directory.shared.ldap.ldif.LdifEntry;
import org.apache.directory.shared.ldap.ldif.LdifReader;
import org.apache.directory.shared.ldap.name.LdapDN;
import org.apache.mina.common.DefaultIoFilterChainBuilder;
import org.apache.mina.common.IoFilterChainBuilder;
import org.apache.mina.filter.SSLFilter;
import org.mule.util.IOUtils;

public final class DSManager
{
    /*
     * Licensed to the Apache Software Foundation (ASF) under one or more
     * contributor license agreements. See the NOTICE file distributed with this
     * work for additional information regarding copyright ownership. The ASF
     * licenses this file to you under the Apache License, Version 2.0 (the
     * "License"); you may not use this file except in compliance with the
     * License. You may obtain a copy of the License at
     * 
     * http://www.apache.org/licenses/LICENSE-2.0
     * 
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
     * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
     * License for the specific language governing permissions and limitations
     * under the License.
     * 
     */

    /**
     * logger used by this class
     */
    protected static final Log logger = LogFactory.getLog(DSManager.class);

    /**
     * A simple testcase for testing JNDI provider functionality.
     * 
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory
     *         Project</a>
     * @version $Rev: 502690 $
     */

    private static final DSManager instance = new DSManager();

    // private static final String CTX_FACTORY =
    // "com.sun.jndi.ldap.LdapCtxFactory";

    /** the context root for the system partition */
    protected LdapContext sysRoot;

    /** the context root for the system partition */
    protected LdapContext exampleRoot;

    protected LdapContext root;

    /** The RootDSE is a standard attribute */
    protected CoreSession rootDSE;

    protected LdapContext schemaRoot;

    /** flag whether to delete database files for each test or not */
    protected boolean doDelete = true;

    protected int port = -1;

    protected volatile boolean running;

    protected DirectoryService directoryService;
    protected SocketAcceptor socketAcceptor;
    protected LdapService ldapService;
    protected LdapService ldapSService;

    /*
     * static{ try {
     * 
     * //new BouncyCastleProvider(); // TlsKeyGenerator.
     * 
     * System.setProperty (
     * "javax.net.ssl.trustStore","/home/hsaly/devel/projects/mule-transport-ldap/trunk/src/test/resources/ldaps-server-cert.jks" ); //
     * System.setProperty (
     * "javax.net.ssl.keyStore","/home/hsaly/devel/projects/mule-transport-ldap/trunk/src/test/resources/ldaps-server-cert.jks" ); //
     * System.setProperty ( "javax.net.ssl.keyStorePassword", "changeit" );
     * logger.debug(KeyStore.getDefaultType()); KeyStore ks =
     * KeyStore.getInstance(KeyStore.getDefaultType());
     * 
     * logger.debug(new File(".").getAbsolutePath());
     * 
     * ks.load(new FileInputStream(
     * "/home/hsaly/devel/projects/mule-transport-ldap/trunk/src/test/resources/ldaps-server-cert.jks"),
     * "changeit".toCharArray());
     * 
     * logger.debug(ks.getCertificateChain("test").toString());
     * 
     * //DSManager.init(ks); } catch (KeyStoreException e) { // TODO
     * Auto-generated catch block e.printStackTrace(); } catch
     * (NoSuchAlgorithmException e) { // TODO Auto-generated catch block
     * e.printStackTrace(); } catch (CertificateException e) { // TODO
     * Auto-generated catch block e.printStackTrace(); } catch
     * (FileNotFoundException e) { // TODO Auto-generated catch block
     * e.printStackTrace(); } catch (IOException e) { // TODO Auto-generated
     * catch block e.printStackTrace(); } catch (Exception e) { // TODO
     * Auto-generated catch block e.printStackTrace(); } }
     */

    public boolean isRunning()
    {
        return running;
    }

    private DSManager()
    {
        logger.debug("DSManager instantiated");
    }

    public static DSManager getInstance()
    {
        return instance;
    }

    /**
     * Get's the initial context factory for the provider's ou=system context
     * root.
     * 
     * @see junit.framework.TestCase#setUp()
     */

    public synchronized void start() throws Exception
    {
        start(false);
    }

    public synchronized void start(final boolean allowAnon) throws Exception
    {
        if (running)
        {

            logger.debug("start() called while already running");

            if (checkSocketNotConnected())
            {
                logger.debug("start() forced");
            }
            else
            {
                logger.debug("DS is already running, stop it, then start it.");

                try
                {
                    stop();

                }
                catch (final Exception e)
                {
                    // TODO: handle exception
                }

                // throw new IllegalStateException("DS already running on port "
                // + port);
            }

        }

        logger.debug("DS is starting ...");

        port = 10389;

        directoryService = new DefaultDirectoryService();
        directoryService.setShutdownHookEnabled(false);
        directoryService.getChangeLog().setEnabled(true);
        directoryService.setAllowAnonymousAccess(allowAnon);

        socketAcceptor = new SocketAcceptor(null);
        ldapService = new LdapService();
        ldapService.setSocketAcceptor(socketAcceptor);
        ldapService.setDirectoryService(directoryService);
        ldapService.setIpPort(port);
        // ldapService.setIpAddress("gkar.kerb.de");

        // ldapService.setAccessControlEnabled(false);
        // ldapService.setShutdownHookEnabled(false);
        ldapService.setAllowAnonymousAccess(allowAnon);

        // ldapService.getLdapsConfiguration().setIpPort(10636);
        // ldapService.getLdapsConfiguration().setEnabled(true);
        // ldapService.getLdapsConfiguration().setLdapsCertificateFile(new
        // File("src/test/resources/ldaps-server-cert.jks"));
        // ldapService.getLdapsConfiguration().setIpPort(10636);

        setupSaslMechanisms(ldapService);

        // S
        ldapSService = new LdapService();
        ldapSService.setSocketAcceptor(socketAcceptor);
        ldapSService.setDirectoryService(directoryService);
        ldapSService.setIpPort(10636);
        ldapSService.setEnableLdaps(true);

        // ldapSService.setConfidentialityRequired(true);
        ldapSService.setConfidentialityRequired(true);
        // ldapService.setIpAddress("gkar.kerb.de");

        // ldapService.setAccessControlEnabled(false);
        // ldapService.setShutdownHookEnabled(false);
        ldapSService.setAllowAnonymousAccess(allowAnon);

        // ldapService.getLdapsConfiguration().setIpPort(10636);
        // ldapService.getLdapsConfiguration().setEnabled(true);
        // ldapService.getLdapsConfiguration().setLdapsCertificateFile(new
        // File("src/test/resources/ldaps-server-cert.jks"));
        // ldapService.getLdapsConfiguration().setIpPort(10636);

        setupSaslMechanisms(ldapSService);

        doDelete(directoryService.getWorkingDirectory());

        directoryService.startup();

        // java.security.cert.X509Certificate cert =
        // TlsKeyGenerator.getCertificate(directoryService.getAdminSession().lookup(new
        // LdapDN("uid=admin,ou=system")).getOriginalEntry());

        final KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(new FileInputStream("src/test/resources/truststore_2.jks"),
                "changeit".toCharArray());

        // java.security.cert.X509Certificate testcert =
        // (java.security.cert.X509Certificate) ks.getCertificate("test");
        // logger.debug(testcert);
        // directoryService.getAdminSession().lookup(new
        // LdapDN("uid=admin,ou=system")).getOriginalEntry().put("userCertificate",testcert.getEncoded());

        // logger.debug("type: "+testcert.getType());
        final java.security.cert.X509Certificate cert = TlsKeyGenerator
                .getCertificate(directoryService.getAdminSession().lookup(
                        new LdapDN("uid=admin,ou=system")).getOriginalEntry());

        ks.setCertificateEntry("apachetmp", cert);

        final File tmpKs = new File("target/truststore_tmp.jks");
        if (tmpKs.exists())
        {
            boolean del = tmpKs.delete();

            if (!del)
            {
                logger.error("Unable to delete " + tmpKs.getAbsolutePath());
                // throw new Exception("Unable to delete
                // "+tmpKs.getAbsolutePath());
            }
        }

        ks.store(new FileOutputStream("target/truststore_tmp.jks"), "changeit"
                .toCharArray());

        logger.debug(cert);

        // TODO shouldn't this be before calling configureLdapServer() ???
        ldapService.addExtendedOperationHandler(new StartTlsHandler());
        ldapService
                .addExtendedOperationHandler(new StoredProcedureExtendedOperationHandler());

        ldapService.start();

        ldapSService.addExtendedOperationHandler(new StartTlsHandler());
        // ldapSService.add( new LdapsInitializer() );
        ldapSService
                .addExtendedOperationHandler(new StoredProcedureExtendedOperationHandler());

        ldapSService.start();

        // dn: uid=admin,ou=system
        setContexts(ServerDNConstants.ADMIN_SYSTEM_DN, "secret");

        setUpPartition();

        importLdif(IOUtils.getResourceAsStream("examplecom.ldif", this
                .getClass()));

        final Attributes attrs = new BasicAttributes();
        attrs.put(new BasicAttribute("userCertificate", ""));

        // sysRoot.modifyAttributes("uid=admin",LdapContext.REPLACE_ATTRIBUTE,attrs);
        logger.debug(rootDSE.getAuthenticatedPrincipal());
        logger.debug(rootDSE.getAuthenticationLevel());
        logger.debug(rootDSE.getEffectivePrincipal());
        logger.debug(rootDSE.toString());

        running = true;

        logger.debug("DS now started!");
    }

    private void setupSaslMechanisms(final LdapService server)
    {
        final Map < String, MechanismHandler > mechanismHandlerMap = new HashMap < String, MechanismHandler >();

        mechanismHandlerMap.put(SupportedSaslMechanisms.PLAIN,
                new PlainMechanismHandler());

        final CramMd5MechanismHandler cramMd5MechanismHandler = new CramMd5MechanismHandler();
        mechanismHandlerMap.put(SupportedSaslMechanisms.CRAM_MD5,
                cramMd5MechanismHandler);

        final DigestMd5MechanismHandler digestMd5MechanismHandler = new DigestMd5MechanismHandler();
        mechanismHandlerMap.put(SupportedSaslMechanisms.DIGEST_MD5,
                digestMd5MechanismHandler);

        final GssapiMechanismHandler gssapiMechanismHandler = new GssapiMechanismHandler();
        mechanismHandlerMap.put(SupportedSaslMechanisms.GSSAPI,
                gssapiMechanismHandler);

        final NtlmMechanismHandler ntlmMechanismHandler = new NtlmMechanismHandler();
        // TODO - set some sort of default NtlmProvider implementation here
        // ntlmMechanismHandler.setNtlmProvider( provider );
        // TODO - or set FQCN of some sort of default NtlmProvider
        // implementation here
        // ntlmMechanismHandler.setNtlmProviderFqcn( "com.foo.BarNtlmProvider"
        // );
        mechanismHandlerMap.put(SupportedSaslMechanisms.NTLM,
                ntlmMechanismHandler);
        mechanismHandlerMap.put(SupportedSaslMechanisms.GSS_SPNEGO,
                ntlmMechanismHandler);

        ldapService.setSaslMechanismHandlers(mechanismHandlerMap);
        ldapService.setSaslHost("localhost");

        // List<String> realms = new ArrayList<String>();
        // realms.add("system");
        // ldapService.setSaslRealms(realms);
    }

    private void setUpPartition() throws Exception
    {
        // Add partition 'sevenSeas'

        Partition pcfg = new JdbmPartition();
        // FIXME name
        pcfg.setId("sevenSeas");
        // dn o=sevenseas
        pcfg.setSuffix("o=sevenseas");

        directoryService.addPartition(pcfg);

        logger.debug("suffix:" + pcfg.getSuffix());

        LdapDN suffixDn = new LdapDN("o=sevenseas");
        suffixDn.normalize(directoryService.getRegistries()
                .getAttributeTypeRegistry().getNormalizerMapping());
        logger.debug(suffixDn.toString());
        ServerEntry ctxEntry = new DefaultServerEntry(directoryService
                .getRegistries(), suffixDn);
        ctxEntry.put("objectClass", "top");
        ctxEntry.get("objectClass").add("organizationalUnit");
        ctxEntry.put("o", "sevenseas");

        logger.debug(ctxEntry);
        pcfg.add(new AddOperationContext(directoryService.getAdminSession(),
                ctxEntry));

        pcfg = new JdbmPartition();
        // FIXME name
        pcfg.setId("example");
        // dn o=sevenseas
        pcfg.setSuffix("dc=example,dc=com");

        directoryService.addPartition(pcfg);

        logger.debug("suffix:" + pcfg.getSuffix());

        suffixDn = new LdapDN("dc=example,dc=com");
        suffixDn.normalize(directoryService.getRegistries()
                .getAttributeTypeRegistry().getNormalizerMapping());
        logger.debug(suffixDn.toString());
        ctxEntry = new DefaultServerEntry(directoryService.getRegistries(),
                suffixDn);
        ctxEntry.put("dc", "example");
        ctxEntry.put("objectClass", "top");
        ctxEntry.get("objectClass").add("domain");
        // ctxEntry.put( "ou", "users" );
        // ctxEntry.put( "objectClass", "organizationalUntit" );

        logger.debug(ctxEntry);
        pcfg.add(new AddOperationContext(directoryService.getAdminSession(),
                ctxEntry));
    }

    /*
     * Set<Partition> partitions = new HashSet<Partition>(); JdbmPartition
     * partition = new JdbmPartition(); partition.setId( "example" );
     * partition.setSuffix( "dc=example,dc=com" );
     * 
     * Set<Index<?,ServerEntry>> indexedAttrs = new HashSet<Index<?,ServerEntry>>();
     * indexedAttrs.add( new JdbmIndex<String,ServerEntry>( "ou" ) );
     * indexedAttrs.add( new JdbmIndex<String,ServerEntry>( "dc" ) );
     * indexedAttrs.add( new JdbmIndex<String,ServerEntry>( "objectClass" ) );
     * partition.setIndexedAttributes( indexedAttrs );
     * 
     * 
     * suffixDn = new LdapDN( "dc=example,dc=com" ); suffixDn.normalize(
     * directoryService.getRegistries().getAttributeTypeRegistry().getNormalizerMapping() );
     * logger.debug(suffixDn.toString()); ctxEntry = new DefaultServerEntry(
     * directoryService.getRegistries()); ctxEntry.put( "dc", "example" );
     * ctxEntry.put( "objectClass", "top" ); ctxEntry.get( "objectClass" ).add(
     * "domain" ); //ctxEntry.put( "ou", "users" ); //ctxEntry.put(
     * "objectClass", "organizationalUntit" );
     * 
     * logger.debug(ctxEntry);
     * 
     * 
     * 
     * 
     * partitions.add( partition ); directoryService.setPartitions( partitions );
     * 
     * partition.add( new AddOperationContext(
     * directoryService.getAdminSession(), ctxEntry ) ); ( }
     * 
     * /** Deletes the Eve working directory.
     */
    protected void doDelete(final File wkdir) throws IOException
    {
        if (doDelete)
        {
            if (wkdir.exists())
            {
                FileUtils.deleteDirectory(wkdir);
            }
            if (wkdir.exists())
            {
                throw new IOException("Failed to delete: " + wkdir);
            }
        }
    }

    protected void setContexts(final Hashtable < String, Object > env)
            throws Exception
    {
        final Hashtable < String, Object > envFinal = new Hashtable < String, Object >(
                env);
        // envFinal.put( Context.PROVIDER_URL, "dc=example,dc=com" );
        // exampleRoot = new InitialLdapContext( envFinal, null );

        // Hashtable<String, Object> envFinal = new Hashtable<String, Object>(
        // env );
        envFinal.put(Context.PROVIDER_URL, ServerDNConstants.SYSTEM_DN);
        sysRoot = new InitialLdapContext(envFinal, null);

        envFinal.put(Context.PROVIDER_URL, "");
        rootDSE = directoryService.getAdminSession();
        root = new InitialLdapContext(envFinal, null);

        envFinal.put(Context.PROVIDER_URL, ServerDNConstants.OU_SCHEMA_DN);
        schemaRoot = new InitialLdapContext(envFinal, null);
    }

    protected void setContexts(final String user, final String passwd)
            throws Exception
    {
        final Hashtable < String, Object > env = new Hashtable < String, Object >();
        env.put(DirectoryService.JNDI_KEY, directoryService);
        env.put(Context.SECURITY_PRINCIPAL, user);
        env.put(Context.SECURITY_CREDENTIALS, passwd);
        env.put(Context.SECURITY_AUTHENTICATION, "none");
        env.put(Context.INITIAL_CONTEXT_FACTORY, CoreContextFactory.class
                .getName());
        setContexts(env);
    }

    /*
     * protected LdapContext getWiredContext( String bindPrincipalDn, String
     * password ) throws Exception { // if ( ! apacheDS.isStarted() ) // { //
     * throw new ConfigurationException( "The server is not online! Cannot
     * connect to it." ); // }
     * 
     * Hashtable<String, String> env = new Hashtable<String, String>();
     * env.put( Context.INITIAL_CONTEXT_FACTORY, CTX_FACTORY ); env.put(
     * Context.PROVIDER_URL, "ldap://localhost:" + port ); env.put(
     * Context.SECURITY_PRINCIPAL, bindPrincipalDn ); env.put(
     * Context.SECURITY_CREDENTIALS, password ); env.put(
     * Context.SECURITY_AUTHENTICATION, "simple" ); return new
     * InitialLdapContext( env, null ); }
     * 
     * protected LdapContext getWiredContext() throws Exception { return
     * getWiredContext( ServerDNConstants.ADMIN_SYSTEM_DN, "secret" ); }
     */
    /**
     * Sets the system context root to null.
     * 
     * @see junit.framework.TestCase#tearDown()
     */
    public synchronized void stop() throws Exception
    {
        logger.debug("DS is stopping ...");

        if (!running)
        {
            logger.debug("stop() called while is not running");

            if (checkSocketNotConnected())
            {
                return;
            }
            else
            {
                logger.debug("stop() forced");
            }
        }

        ldapService.stop();
        ldapSService.stop();

        directoryService.shutdown();

        doDelete(directoryService.getWorkingDirectory());

        sysRoot = null;
        this.rootDSE = null;

        logger.debug("DS waiting for socket release ...");

        // wait for shutdown
        int i = 0;

        while ((i < 20) && !checkSocketNotConnected())
        {
            Thread.sleep(2000);
            i++;
            logger.debug("Try " + i);
        }

        if (!checkSocketNotConnected())
        {
            throw new Exception(
                    "Shutdown of DS not successfull, server socket was not freed");
        }

        logger.debug("DS now stopped!");
        running = false;

    }

    /**
     * Imports the LDIF entries packaged with the Eve JNDI provider jar into the
     * newly created system partition to prime it up for operation. Note that
     * only ou=system entries will be added - entries for other partitions
     * cannot be imported and will blow chunks.
     * 
     * @throws NamingException
     *             if there are problems reading the ldif file and adding those
     *             entries to the system partition
     */
    protected void importLdif(final InputStream in) throws NamingException
    {
        if (in == null)
        {
            throw new NullPointerException("in must not be null");
        }

        try
        {
            final Iterator < LdifEntry > iterator = new LdifReader(in)
                    .iterator();

            while (iterator.hasNext())
            {
                final LdifEntry entry = iterator.next();

                final LdapDN dn = new LdapDN(entry.getDn());
                // dn.remove(0);
                // dn.remove(0);

                final Attributes attr = new BasicAttributes();

                final Iterator < EntryAttribute > iea = entry.getEntry()
                        .iterator();

                for (; iea.hasNext();)
                {
                    final EntryAttribute ea = iea.next();

                    attr.put(ea.getId(), ea.get().get());

                    logger.debug("id " + ea.getId() + "//" + ea.get().get());
                    logger.debug("size:" + ea.size());
                }

                logger.debug(" for dn:" + dn);

                root.createSubcontext(dn, attr);
            }
        }
        catch (final Exception e)
        {
            final String msg = "failed while trying to parse system ldif file";
            final NamingException ne = new LdapConfigurationException(msg);
            ne.setRootCause(e);
            throw ne;
        }
    }

    public static boolean checkSocketNotConnected()
    {

        try
        {
            final Socket s = new Socket("localhost", 10389);

            if (s.isConnected())
            {
                logger
                        .debug("client socket is connected (server socket bound)");
            }
            s.close();
            return false;
        }
        catch (final Exception e)
        {

            logger.debug("client Socket not connected " + e.toString());

        }

        try
        {
            final ServerSocket s = new ServerSocket(10389);
            if (s.isBound())
            {
                logger.debug("server socket is bound (=was therefore free)");
            }
            s.close();
            return true;
        }
        catch (final Exception e)
        {
            logger.debug("Server socket already bound " + e.toString());
            // e.printStackTrace();
            return false;
        }

    }

    public static void main(final String[] args) throws Exception
    {
        final DSManager m = DSManager.getInstance();
        m.start(true);

        System.out.println("Enter s and Enter to stop: ");

        while (System.in.read() != 115)
        {
            ;
            // logger.debug.println(i);
        }

        m.stop();

        System.out.println("main() finished");
    }

    public int getPort()
    {
        return port;
    }

    public static IoFilterChainBuilder init(final KeyStore ks)
            throws NamingException
    {
        SSLContext sslCtx;
        try
        {
            // Set up key manager factory to use our key store
            String algorithm = Security
                    .getProperty("ssl.KeyManagerFactory.algorithm");
            if (algorithm == null)
            {
                algorithm = "SunX509";
            }
            final KeyManagerFactory kmf = KeyManagerFactory
                    .getInstance(algorithm);
            kmf.init(ks, "changeit".toCharArray());

            // Initialize the SSLContext to work with our key managers.
            sslCtx = SSLContext.getInstance("TLS");
            sslCtx.init(kmf.getKeyManagers(), new TrustManager[]
            {new ServerX509TrustManager()}, new SecureRandom());

            logger.debug("ssl set");
        }
        catch (final Exception e)
        {
            throw (NamingException) new NamingException(
                    "Failed to create a SSL context.").initCause(e);
        }

        final DefaultIoFilterChainBuilder chain = new DefaultIoFilterChainBuilder();
        chain.addLast("sslFilter", new SSLFilter(sslCtx));
        return chain;
    }

}
