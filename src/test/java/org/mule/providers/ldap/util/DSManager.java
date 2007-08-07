package org.mule.providers.ldap.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.dgc.VMID;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.apache.commons.io.FileUtils;
import org.apache.directory.server.configuration.MutableServerStartupConfiguration;
import org.apache.directory.server.core.configuration.MutablePartitionConfiguration;
import org.apache.directory.server.core.configuration.ShutdownConfiguration;
import org.apache.directory.server.jndi.ServerContextFactory;
import org.apache.directory.shared.ldap.exception.LdapConfigurationException;
import org.apache.directory.shared.ldap.ldif.Entry;
import org.apache.directory.shared.ldap.ldif.LdifReader;
import org.apache.directory.shared.ldap.name.LdapDN;

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

    static
    {
        
        final String vmid = new VMID().toString();
        
        System.out.println("DSManager loaded: VMID: " + vmid );

        /*Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
        {

            public void run()
            {
                System.out.println("JVM shutdown: "+vmid );
            }
        }));*/

    }

    /**
     * A simple testcase for testing JNDI provider functionality.
     * 
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory
     *         Project</a>
     * @version $Rev: 502690 $
     */

    private static final DSManager instance = new DSManager();

    /** the context root for the system partition */
    protected LdapContext sysRoot;

    /** the context root for the rootDSE */
    protected LdapContext rootDSE;

    /** flag whether to delete database files for each test or not */
    protected boolean doDelete = true;

    protected MutableServerStartupConfiguration configuration = new MutableServerStartupConfiguration();

    protected int port = -1;

    protected volatile boolean running;

    public boolean isRunning()
    {
        return running;
    }

    private DSManager()
    {
        System.out.println("DSManager instantiated");
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

    public synchronized void start(boolean allowAnon) throws Exception
    {
        if (running)
        {

            System.out.println("start() called while already running");

            if (checkSocketNotConnected())
            {
                System.out.println("start() forced");
            } else
            {
                throw new IllegalStateException("DS already running on port "
                        + port);
            }

        }

        System.out.println("DS is starting ...");

        configuration.setWorkingDirectory(new File(".mule-ldap-ds-tmp/"));

        doDelete(configuration.getWorkingDirectory());
        port = 10389;// AvailablePortFinder.getNextAvailable(1024);
        configuration.setLdapPort(port);
        // cfg.setEnableNetworking(true);
        configuration.setAccessControlEnabled(false);
        configuration.setShutdownHookEnabled(false);
        configuration.setAllowAnonymousAccess(allowAnon);
        /*configuration.setEnableLdaps(true);
        configuration.setLdapsPort(10636);
        configuration.setLdapsCertificateFile(new File(
                "src/test/resources/ldaps-server-cert.jks"));
         */
        setUpPartition(configuration);

        setContexts("uid=admin,ou=system", "secret");

        running = true;

        System.out.println("DS now started!");
    }

    private void setUpPartition(MutableServerStartupConfiguration configuration)
            throws NamingException
    {
        // Add partition 'sevenSeas'
        MutablePartitionConfiguration pcfg = new MutablePartitionConfiguration();
        pcfg.setName("sevenSeas");
        pcfg.setSuffix("o=sevenseas");

        // Create some indices
        java.util.Set indexedAttrs = new HashSet();
        indexedAttrs.add("objectClass");
        indexedAttrs.add("o");
        pcfg.setIndexedAttributes(indexedAttrs);

        // Create a first entry associated to the partition
        Attributes attrs = new BasicAttributes(true);

        // First, the objectClass attribute
        Attribute attr = new BasicAttribute("objectClass");
        attr.add("top");
        attr.add("organization");
        attrs.put(attr);

        // The the 'Organization' attribute
        attr = new BasicAttribute("o");
        attr.add("sevenseas");
        attrs.put(attr);

        // Associate this entry to the partition
        pcfg.setContextEntry(attrs);

        // As we can create more than one partition, we must store
        // each created partition in a Set before initialization
        Set pcfgs = new HashSet();
        pcfgs.add(pcfg);

        configuration.setContextPartitionConfigurations(pcfgs);

        // Create a working directory
        // File workingDirectory = new File( "server-work" );
        // configuration.setWorkingDirectory( workingDirectory );

    }

    /**
     * Deletes the Eve working directory.
     */
    protected void doDelete(File wkdir) throws IOException
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

    /**
     * Sets the contexts for this base class. Values of user and password used
     * to set the respective JNDI properties. These values can be overriden by
     * the overrides properties.
     * 
     * @param user
     *            the username for authenticating as this user
     * @param passwd
     *            the password of the user
     * @throws NamingException
     *             if there is a failure of any kind
     */
    protected void setContexts(String user, String passwd)
            throws NamingException
    {
        Hashtable env = new Hashtable(configuration.toJndiEnvironment());
        env.put(Context.SECURITY_PRINCIPAL, user);
        env.put(Context.SECURITY_CREDENTIALS, passwd);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.INITIAL_CONTEXT_FACTORY, ServerContextFactory.class
                .getName());
        setContexts(env);
    }

    /**
     * Sets the contexts of this class taking into account the extras and
     * overrides properties.
     * 
     * @param env
     *            an environment to use while setting up the system root.
     * @throws NamingException
     *             if there is a failure of any kind
     */
    protected void setContexts(Hashtable env) throws NamingException
    {
        Hashtable envFinal = new Hashtable(env);
        envFinal.put(Context.PROVIDER_URL, "ou=system");
        sysRoot = new InitialLdapContext(envFinal, null);

        envFinal.put(Context.PROVIDER_URL, "");
        rootDSE = new InitialLdapContext(envFinal, null);
    }

    /**
     * Sets the system context root to null.
     * 
     * @see junit.framework.TestCase#tearDown()
     */
    public synchronized void stop() throws Exception
    {
        System.out.println("DS is stopping ...");

        if (!running)
        {
            System.out.println("stop() called while is not running");

            if (checkSocketNotConnected())
            {
                return;
            } else
            {
                System.out.println("stop() forced");
            }
        }

        // super.tearDown();
        Hashtable env = new Hashtable();
        env.put(Context.PROVIDER_URL, "ou=system");
        env.put(Context.INITIAL_CONTEXT_FACTORY,
                "org.apache.directory.server.jndi.ServerContextFactory");
        env.putAll(new ShutdownConfiguration().toJndiEnvironment());
        env.put(Context.SECURITY_PRINCIPAL, "uid=admin,ou=system");
        env.put(Context.SECURITY_CREDENTIALS, "secret");
        
        new InitialContext(env);
       

        sysRoot = null;
        doDelete(configuration.getWorkingDirectory());
        configuration = new MutableServerStartupConfiguration();
        running = false;

        System.out.println("DS now stopped!");
        
        if(!checkSocketNotConnected())
        {
            throw new Exception("Shutdown of DS not successfull, server socket was not freed");
        }
        
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
    protected void importLdif(InputStream in) throws NamingException
    {
        try
        {
            Iterator iterator = new LdifReader(in);

            while (iterator.hasNext())
            {
                Entry entry = (Entry) iterator.next();

                LdapDN dn = new LdapDN(entry.getDn());

                rootDSE.createSubcontext(dn, entry.getAttributes());
            }
        } catch (Exception e)
        {
            String msg = "failed while trying to parse system ldif file";
            NamingException ne = new LdapConfigurationException(msg);
            ne.setRootCause(e);
            throw ne;
        }
    }

    public static boolean checkSocketNotConnected()
    {

        try
        {
            Socket s = new Socket("localhost", 10389);

            if (s.isConnected())
                System.out
                        .println("client socket is connected (server socket bound)");

            s.close();
            return false;
        } catch (Exception e)
        {

            System.out.println("client Socket not connected " + e.toString());

        }

        try
        {
            ServerSocket s = new ServerSocket(10389);
            if (s.isBound())
                System.out
                        .println("server socket is bound (=was therefore free)");
            s.close();
            return true;
        } catch (Exception e)
        {
            System.out.println("Server socket already bound " + e.toString());
            // e.printStackTrace();
            return false;
        }

    }

    public static void main(String[] args) throws Exception
    {
        DSManager m = DSManager.getInstance();
        m.start();
        
        System.out.print("Enter s and Enter to stop: ");
               
        while(System.in.read() != 115);
        //System.out.println(i);
        
        m.stop();
 

        System.out.println("main() finished");
    }

    public int getPort()
    {
        return port;
    }
    
 

}
