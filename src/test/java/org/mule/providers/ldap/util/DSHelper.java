package org.mule.providers.ldap.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.InitialDirContext;

import org.apache.directory.server.configuration.MutableServerStartupConfiguration;
import org.apache.directory.server.core.configuration.MutablePartitionConfiguration;
import org.apache.directory.server.core.configuration.ShutdownConfiguration;
import org.mule.util.FileUtils;
import org.mule.util.IOUtils;

public class DSHelper
{
    
    private static int count=0;
    private static File workingDir = new File(".mule-ldap-ds-tmp/");

    /*public static void main(String[] args) throws Exception
    {
        startDS();
        
        System.in.read();
        
        stopDS();
    }*/

    public static synchronized void startDS() throws NamingException
    {
        startDS(false);
    }

    public static synchronized void startDS(boolean allowAnonymousBind)
            throws NamingException
    {
        //stopDS();
        
        checkSocketNotConnected();
        
        count++;
        System.out.println("start DS Nr."+(count)+ " by thread "+Thread.currentThread().getName());
        cleanUp();
        workingDir.mkdirs();
        System.out.println("workingDir: "+workingDir.getAbsolutePath());
        MutableServerStartupConfiguration cfg = new MutableServerStartupConfiguration();
        cfg.setWorkingDirectory(workingDir);

	cfg.setShutdownHookEnabled( false );



        // System.out.println(workingDir);

        // Setup LDAP networking
        cfg.setEnableNetworking(true);

        cfg.setEnableLdaps(true);
        cfg.setLdapsPort(10636);
        cfg.setLdapsCertificateFile(new File(
              "src/test/resources/ldaps-server-cert.jks"));

       
        cfg.setLdapPort(10389);

        cfg.setAllowAnonymousAccess(allowAnonymousBind);
        cfg.setAccessControlEnabled(false);

        setUpPartition(cfg);

        // Start the Server
        Hashtable env = createEnv();
        env.putAll(cfg.toJndiEnvironment());
        System.out.println(""+env);
        new InitialDirContext(env);
        System.out.println("DS started");

    }

    public static synchronized void setUpPartition(
            MutableServerStartupConfiguration configuration)
            throws NamingException
    {
        // Add partition 'sevenSeas'
        MutablePartitionConfiguration pcfg = new MutablePartitionConfiguration();
        pcfg.setName("sevenSeas");
        pcfg.setSuffix("o=sevenseas");

        // Create some indices
        Set indexedAttrs = new HashSet();
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

    public static synchronized void stopDS() throws NamingException
    {
        System.out.println("stopping DS Nr. "+count);
        Hashtable env = createEnv();
        ShutdownConfiguration cfg = new ShutdownConfiguration();
        env.putAll(cfg.toJndiEnvironment());
        new InitialDirContext(env);
        cleanUp();
        System.out.println("DS stopped");
        count--;
        
        checkSocketNotConnected();
        //System.exit(0);
               
    }
    
    
    private static void checkSocketNotConnected(){
        
        try
        {
            Socket s = new Socket("localhost", 10389);
            if(s.isConnected()) System.out.println("socket is connected, but its not expected");
            s.close();
        } catch (Exception e)
        {
            
            System.out.println("OK "+e.toString());

           

        }
        
        try
        {
            ServerSocket s = new ServerSocket(10389);
            if(s.isBound()) System.err.println("server socket is bound");
            s.close();
        } catch (Exception e)
        {
             System.out.println("ERR "+e.toString());
             e.printStackTrace();
        }

        
    }
    
    

    public static synchronized void cleanUp()
    {
        System.out.println("cleaning up temp ds workingdir");
        FileUtils.deleteTree(workingDir);
    }

    public static synchronized Hashtable createEnv()
    {
        Hashtable env = new Properties();

        env.put(Context.PROVIDER_URL, "ou=system");
        env.put(Context.INITIAL_CONTEXT_FACTORY,
                "org.apache.directory.server.jndi.ServerContextFactory");

        env.put(Context.SECURITY_PRINCIPAL, "uid=admin,ou=system");
        env.put(Context.SECURITY_CREDENTIALS, "secret");
        env.put(Context.SECURITY_AUTHENTICATION, "simple");

        return env;
    }

}
