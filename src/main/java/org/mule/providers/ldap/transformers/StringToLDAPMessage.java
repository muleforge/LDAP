package org.mule.providers.ldap.transformers;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.mule.providers.ldap.util.LDAPUtils;
import org.mule.transformers.AbstractTransformer;
import org.mule.umo.transformer.TransformerException;
import org.mule.util.IOUtils;

import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPMessage;
import com.novell.ldap.util.DSMLReader;
import com.novell.ldap.util.LDAPReader;
import com.novell.ldap.util.LDIFReader;

public class StringToLDAPMessage extends AbstractTransformer
{

    public static final String PROPERTY_LDAP_HOST = "format";

    protected String format;

    // DSML(default), LDIF

    // @Override
    protected Object doTransform(Object src, String encoding)
            throws TransformerException
    {

        if (src == null)
        {
            throw new TransformerException(this, new IllegalArgumentException(
                    "src must not be null"));
        }

        if (!(src instanceof String))
        {
            throw new TransformerException(this, new IllegalArgumentException(
                    "wrong type " + src.getClass()
                            + ", java.lang.String excpeted"));
        }

        String msg = ((String) src);

        // TODO ldifcheck
        if ("ldif".equalsIgnoreCase(format))
        {
            return getOut(false, msg);
        }

        return getOut(true, msg);

    }

    private List getOut(boolean dsml, String msg)
            throws TransformerException
    {
        InputStream in = IOUtils.toInputStream(msg);

        try
        {
            LDAPReader reader = dsml ? (LDAPReader) new DSMLReader(in)
                    : (LDAPReader) new LDIFReader(in);

                      
            LDAPMessage result = null;//reader.readMessage();
            List res = new ArrayList();
            
            while((result = reader.readMessage()) != null)
            {
                logger.debug(LDAPUtils.dumpLDAPMessage(result));
                res.add(result);
            }
            return res;

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
