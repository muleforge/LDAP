package org.mule.providers.ldap.transformers;

import org.mule.transformers.AbstractTransformer;
import org.mule.umo.transformer.TransformerException;
import org.mule.util.StringUtils;

import com.novell.ldap.LDAPDeleteRequest;
import com.novell.ldap.LDAPException;

public class StringToDeleteRequest extends AbstractTransformer
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

        String content = src.toString();

        if (StringUtils.isEmpty(content))
        {
            throw new TransformerException(this, new IllegalArgumentException(
                    "srctoString() must not be null or empty"));
        }

        try
        {

            // TODO LDAPCOntrols
            LDAPDeleteRequest request = new LDAPDeleteRequest(content, null);

            // System.out.println("tranf: id: " + request.getMessageID());

            return request;
        } catch (LDAPException e)
        {
            throw new TransformerException(this, e);
        }

    }
}
