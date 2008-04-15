package org.mule.providers.ldap.sasl;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;

import javax.security.auth.callback.CallbackHandler;
import javax.security.sasl.Sasl;

import org.mule.util.ArrayUtils;

import com.novell.security.sasl.SaslClient;
import com.novell.security.sasl.SaslClientFactory;
import com.novell.security.sasl.SaslException;

public class SaslBridgeClientFactory implements SaslClientFactory
{

    public SaslClient createSaslClient(String[] arg0, String arg1, String arg2,
            String arg3, Map arg4, CallbackHandler arg5) throws SaslException
    {
        try
        {
            return new SaslBridgeClient(Sasl.createSaslClient(arg0, arg1, arg2,
                    arg3, arg4, arg5));
        }
        catch (javax.security.sasl.SaslException e)
        {
            throw new SaslException("", e);
        }
    }

    public String[] getMechanismNames(Map arg0)
    {
        Enumeration en = Sasl.getSaslClientFactories();
        String[] result = new String[0];

        while (en.hasMoreElements())
        {
            javax.security.sasl.SaslClientFactory scf = (javax.security.sasl.SaslClientFactory) en
                    .nextElement();
            result = (String[]) ArrayUtils.addAll(result, scf
                    .getMechanismNames(arg0));

            System.out.println(Arrays.toString(result));

        }

        return result;
    }
}
