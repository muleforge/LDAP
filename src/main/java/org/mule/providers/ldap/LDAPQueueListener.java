package org.mule.providers.ldap;

import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOImmutableEndpoint;

public interface LDAPQueueListener
{

    void onMessage(UMOMessage message, UMOImmutableEndpoint endpoint)
            throws UMOException;

}
