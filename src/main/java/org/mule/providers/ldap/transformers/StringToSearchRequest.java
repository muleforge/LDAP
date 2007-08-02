package org.mule.providers.ldap.transformers;

import org.mule.providers.ldap.LdapConnector;
import org.mule.providers.ldap.util.LDAPUtils;
import org.mule.transformers.AbstractTransformer;
import org.mule.umo.transformer.TransformerException;

import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPSearchRequest;

public class StringToSearchRequest extends AbstractTransformer
{

    // @Override
    protected Object doTransform(Object src, String encoding)
            throws TransformerException
    {

        if (src == null)
        {
            throw new TransformerException(this, new IllegalArgumentException(
                    "src must not be null"));
        }

        LdapConnector ldapConnector = (LdapConnector) this.endpoint
                .getConnector();

        try
        {

            LDAPSearchRequest request = LDAPUtils.createSearchRequest(
                    ldapConnector, src.toString());

            return request;
        } catch (LDAPException e)
        {
            throw new TransformerException(this, e);
        }

    }
}
