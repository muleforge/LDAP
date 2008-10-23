package org.mule.transport.ldap.transformers;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPModification;
import com.novell.ldap.LDAPModifyRequest;

public class JavaBeanToModifiyRequestTransformerFailTestCase extends
        JavaBeanToModifyRequestTransformerTestCase
{

    @Override
    public Object getResultData()
    {

        final JavaBeanClass bean = new JavaBeanClass();

        try
        {

            final LDAPModification[] mods = new LDAPModification[2];
            LDAPModification mod = new LDAPModification(
                    LDAPModification.REPLACE, new LDAPAttribute("mail", bean
                            .getMail()));
            mods[0] = mod;

            mod = new LDAPModification(LDAPModification.REPLACE,
                    new LDAPAttribute("age", String.valueOf(bean.getAge())));

            mods[1] = mod;

            return new LDAPModifyRequest(bean.getDn(), mods, null);
        }
        catch (final LDAPException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void testTransform() throws Exception
    {
        final Object result = this.getTransformer().transform(getTestData());
        assertNotNull(result);

        final Object expectedResult = this.getResultData();
        assertNotNull(expectedResult);

        assertFalse(this.compareResults(expectedResult, result));
    }

}
