package org.mule.transport.ldap.util;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPSocketFactory;
import com.novell.ldap.connectionpool.PoolManager;

public class EnhancedConnectionPool extends PoolManager
{

    @Override
    public LDAPConnection getBoundConnection(final String DN, final byte[] PW)
            throws LDAPException, InterruptedException
    {
        // TODO Auto-generated method stub
        return super.getBoundConnection(DN, PW);
    }

    @Override
    public void makeConnectionAvailable(final LDAPConnection conn)
    {
        // TODO Auto-generated method stub
        super.makeConnectionAvailable(conn);
    }

    public EnhancedConnectionPool(final String host, final int port,
            final int maxConns, final int maxSharedConns,
            final LDAPSocketFactory factory) throws LDAPException
    {
        super(host, port, maxConns, maxSharedConns, factory);
        // TODO Auto-generated constructor stub
    }

}
