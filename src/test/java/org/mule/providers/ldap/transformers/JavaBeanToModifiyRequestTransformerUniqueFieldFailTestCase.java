package org.mule.providers.ldap.transformers;

import org.mule.umo.transformer.TransformerException;

public class JavaBeanToModifiyRequestTransformerUniqueFieldFailTestCase extends
        JavaBeanToModifiyRequestTransformerUniqueFieldTestCase
{

    protected String getUniqueField()
    {

        return "";
    }

    public void testTransform() throws Exception
    {

        try
        {
            super.testTransform();
            fail();
        }
        catch (TransformerException e)
        {
            // expected
        }
    }

}
