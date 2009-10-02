package com.novell.ldap;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SaslLDAPConnection extends LDAPConnection
{

    protected final Log logger = LogFactory.getLog(getClass());

    public SaslLDAPConnection()
    {
        super();
        // TODO Auto-generated constructor stub
    }

    public SaslLDAPConnection(final int timeout)
    {
        super(timeout);
        // TODO Auto-generated constructor stub
    }

    public SaslLDAPConnection(final LDAPSocketFactory factory)
    {
        super(factory);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void bind(final String dn, final String authzId,
            final String[] mechanisms, final Map props, final Object cbh,
            final LDAPConstraints cons) throws LDAPException
    {
        // TODO Auto-generated method stub
        super.bind(dn, authzId, mechanisms, props, cbh, cons);

        try
        {

            this.getConnection().freeWriteSemaphore(
                    this.getConnection().getBindSemId());
        }
        catch (final RuntimeException e)
        {
            logger
                    .warn("Due to a bug of Novell JLDAP 4.3 while doing SASL bind this message is caused by a hot fix. You can safely ignore it if your SASL bind works fine. Have a look here for more info: http://www.openldap.org/its/index.cgi?findid=6051 Underlying message is: "
                            + e.toString());
        }

    }

}
