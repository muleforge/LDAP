package org.mule.transport.ldap.util;

import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPSocketFactory;
import com.novell.ldap.connectionpool.PoolManager;

public class EnhancedConnectionPool extends PoolManager
{

    public EnhancedConnectionPool(final String host, final int port,
            final int maxConns, final int maxSharedConns,
            final LDAPSocketFactory factory) throws LDAPException
    {
        super(host, port, maxConns, maxSharedConns, factory);
        // TODO Auto-generated constructor stub
    }

}
