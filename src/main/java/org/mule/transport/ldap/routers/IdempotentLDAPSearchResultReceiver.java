package org.mule.transport.ldap.routers;

import org.mule.api.MessagingException;
import org.mule.api.MuleEvent;
import org.mule.api.routing.RoutingException;
import org.mule.routing.inbound.IdempotentReceiver;
import org.mule.transport.ldap.LdapConnector;

import com.novell.ldap.LDAPSearchResult;

public class IdempotentLDAPSearchResultReceiver extends IdempotentReceiver
{

    @Override
    protected String getIdForEvent(final MuleEvent event)
            throws MessagingException
    {
        try
        {

            final LDAPSearchResult sr = (LDAPSearchResult) event.getMessage()
                    .getPayload();

            final LdapConnector c = (LdapConnector) event.getEndpoint()
                    .getConnector();

            return c.getLdapHost() + ":" + c.getLdapPort() + "=["
                    + sr.getEntry().toString() + "]";

        }

        catch (final Exception te)
        {
            throw new RoutingException(event.getMessage(), event.getEndpoint(),
                    te);
        }
    }

}
