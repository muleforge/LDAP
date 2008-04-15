package org.mule.providers.ldap.sasl;

import com.novell.security.sasl.SaslClient;
import com.novell.security.sasl.SaslException;

public class SaslBridgeClient implements SaslClient
{
    private javax.security.sasl.SaslClient sunSaslClient;

    public SaslBridgeClient(javax.security.sasl.SaslClient sunSaslClien)
    {
        super();

        if (sunSaslClien == null)
        {
            throw new IllegalArgumentException("must not be null");
        }

        this.sunSaslClient = sunSaslClien;
    }

    public void dispose() throws SaslException
    {
        try
        {
            sunSaslClient.dispose();
        }
        catch (javax.security.sasl.SaslException e)
        {
            throw new SaslException("", e);
        }
    }

    public byte[] evaluateChallenge(byte[] challenge) throws SaslException
    {
        try
        {
            return sunSaslClient.evaluateChallenge(challenge);
        }
        catch (javax.security.sasl.SaslException e)
        {
            throw new SaslException("", e);
        }
    }

    public String getMechanismName()
    {
        return sunSaslClient.getMechanismName();
    }

    public Object getNegotiatedProperty(String propName)
    {
        return sunSaslClient.getNegotiatedProperty(propName);
    }

    public boolean hasInitialResponse()
    {
        return sunSaslClient.hasInitialResponse();
    }

    public boolean isComplete()
    {
        return sunSaslClient.isComplete();
    }

    public byte[] unwrap(byte[] incoming, int offset, int len)
            throws SaslException
    {
        try
        {
            return sunSaslClient.unwrap(incoming, offset, len);
        }
        catch (javax.security.sasl.SaslException e)
        {
            throw new SaslException("", e);
        }
    }

    public byte[] wrap(byte[] outgoing, int offset, int len)
            throws SaslException
    {
        try
        {
            return sunSaslClient.wrap(outgoing, offset, len);
        }
        catch (javax.security.sasl.SaslException e)
        {
            throw new SaslException("", e);
        }
    }

}
