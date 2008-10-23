package org.mule.transport.ldap.transformers;

import org.mule.api.transformer.TransformerException;

public class JavaBeanToModifiyRequestTransformerUniqueFieldFailTestCase extends
        JavaBeanToModifiyRequestTransformerUniqueFieldTestCase
{

    @Override
    protected String getUniqueField()
    {

        return "";
    }

    @Override
    public void testTransform() throws Exception
    {

        try
        {
            super.testTransform();
            fail();
        }
        catch (final TransformerException e)
        {
            // expected
        }
    }

}
