package org.mule.providers.ldap.transformers;

import org.mule.umo.transformer.TransformerException;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPModification;
import com.novell.ldap.LDAPModifyRequest;

public class JavaBeanToModifiyRequestTransformerFailTestCase extends
        JavaBeanToModifyRequestTransformerTestCase
{

    public Object getResultData()
    {

        JavaBeanClass bean = new JavaBeanClass();

        try
        {

            LDAPModification[] mods = new LDAPModification[2];
            LDAPModification mod = new LDAPModification(
                    LDAPModification.REPLACE, new LDAPAttribute("mail", bean
                            .getMail()));
            mods[0] = mod;

            mod = new LDAPModification(LDAPModification.REPLACE,
                    new LDAPAttribute("age", String.valueOf(bean.getAge())));

            mods[1] = mod;

            return new LDAPModifyRequest(bean.getDn(), mods, null);
        }
        catch (LDAPException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    public void testTransform() throws Exception
    {
        Object result = this.getTransformer().transform(getTestData());
        assertNotNull(result);

        Object expectedResult = this.getResultData();
        assertNotNull(expectedResult);

        assertFalse(this.compareResults(expectedResult, result));
    }

}
