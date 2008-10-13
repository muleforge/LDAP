package org.mule.providers.ldap;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import junit.framework.TestCase;

import org.mule.providers.ldap.util.DSManager;

public class SaslTest extends TestCase {

	
	
	
	public void testSaslDigestMd5Bind() throws Exception
    {
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put( Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory" );
        env.put( Context.PROVIDER_URL, "ldap://localhost:10389" );

        env.put( Context.SECURITY_AUTHENTICATION, "DIGEST-MD5" );
        env.put( Context.SECURITY_PRINCIPAL, "hsaly" );
        env.put( Context.SECURITY_CREDENTIALS, "secret1" );

        // Specify realm
        env.put( "java.naming.security.sasl.realm", "example.com" );

        // Request privacy protection
        env.put( "javax.security.sasl.qop", "auth-conf" );

        DirContext context = new InitialDirContext( env );
        
        assertNotNull(context);

        String[] attrIDs =
            { "uid" };

        Attributes attrs = context.getAttributes( "uid=hsaly,ou=users,dc=example,dc=com", attrIDs );

        String uid = null;

        if ( attrs.get( "uid" ) != null )
        {
            uid = ( String ) attrs.get( "uid" ).get();
        }

        assertEquals( uid, "hsaly" );
    }

	protected void setUp() throws Exception {
		// TODO Auto-generated method stub
		super.setUp();
		DSManager.getInstance().start();
	}

	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		DSManager.getInstance().stop();
		super.tearDown();
		
	}

}
