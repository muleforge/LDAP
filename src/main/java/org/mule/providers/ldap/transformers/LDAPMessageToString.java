package org.mule.providers.ldap.transformers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.mule.transformers.AbstractTransformer;
import org.mule.umo.transformer.TransformerException;

import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPMessage;
import com.novell.ldap.util.DSMLWriter;
import com.novell.ldap.util.LDAPWriter;
import com.novell.ldap.util.LDIFWriter;

public class LDAPMessageToString extends AbstractTransformer
{

    public static final String PROPERTY_LDAP_HOST = "format";

    protected String format;

    // DSML(default), LDIF, toString

    // @Override
    protected Object doTransform(Object src, String encoding)
            throws TransformerException
    {

        if (src == null)
        {
            throw new TransformerException(this, new IllegalArgumentException(
                    "src must not be null"));
        }

        if (!(src instanceof LDAPMessage))
        {
            throw new TransformerException(this, new IllegalArgumentException(
                    "wrong type " + src.getClass() + ", LDAPMessage excpeted"));
        }

        LDAPMessage msg = ((LDAPMessage) src);

        if ("tostring".equalsIgnoreCase(format))
        {

            return msg.toString();

        } else if ("ldif".equalsIgnoreCase(format))
        {

            // TODO ldifcheck
            // if(dsml || msg.getType() != LDAPMessage.SEARCH_RESULT);

            return getOut(false, msg);
        } else
        {
            return getOut(true, msg);
        }

    }

    private String getOut(boolean dsml, LDAPMessage msg)
            throws TransformerException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try
        {
            LDAPWriter writer = dsml ? (LDAPWriter) new DSMLWriter(out)
                    : (LDAPWriter) new LDIFWriter(out);
            writer.writeMessage(msg);
            writer.finish();
            out.close();
            return out.toString();
        } catch (IOException e)
        {
            throw new TransformerException(this, e);
        } catch (LDAPException e)
        {
            throw new TransformerException(this, e);
        }

    }

    public String getFormat()
    {
        return format;
    }

    public void setFormat(String format)
    {
        this.format = format;
    }
}
