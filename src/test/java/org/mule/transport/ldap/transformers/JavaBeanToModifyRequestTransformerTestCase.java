package org.mule.transport.ldap.transformers;

import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.api.transformer.Transformer;
import org.mule.transformer.AbstractTransformerTestCase;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPModification;
import com.novell.ldap.LDAPModifyRequest;

public class JavaBeanToModifyRequestTransformerTestCase extends
        AbstractTransformerTestCase
{

    protected final Log logger = LogFactory.getLog(getClass());

    @Override
    public Object getResultData()
    {

        final JavaBeanClass bean = new JavaBeanClass();

        try
        {

            final LDAPModification[] mods = new LDAPModification[3];
            LDAPModification mod = new LDAPModification(
                    LDAPModification.REPLACE, new LDAPAttribute("mail", bean
                            .getMail()));
            mods[0] = mod;

            mod = new LDAPModification(LDAPModification.REPLACE,
                    new LDAPAttribute("field12", bean.getField12()));
            mods[1] = mod;

            mod = new LDAPModification(LDAPModification.REPLACE,
                    new LDAPAttribute("age", String.valueOf(bean.getAge())));
            mods[2] = mod;

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
    public Object getTestData()
    {

        return new JavaBeanClass();

    }

    @Override
    public Transformer getTransformer() throws Exception
    {

        return new JavaBeanToModifyRequest();
    }

    @Override
    public Transformer getRoundTripTransformer() throws Exception
    {

        return null;
    }

    public static class JavaBeanClass
    {
        private String dn = "ou=system";
        private String mail = "mail@mail.com";
        private int age = 34;
        private String field12 = "field12";

        public String getDn()
        {
            return dn;
        }

        public void setDn(final String dn)
        {
            this.dn = dn;
        }

        public String getMail()
        {
            return mail;
        }

        public void setMail(final String mail)
        {
            this.mail = mail;
        }

        public int getAge()
        {
            return age;
        }

        public void setAge(final int age)
        {
            this.age = age;
        }

        public String getField12()
        {
            return field12;
        }

        public void setField12(final String field12)
        {
            this.field12 = field12;
        }

    }

    @Override
    public boolean compareResults(final Object expected, final Object result)
    {

        final LDAPModifyRequest expectedReq = ((LDAPModifyRequest) expected);
        final LDAPModifyRequest resultReq = ((LDAPModifyRequest) result);

        if (!expectedReq.getDN().equals(resultReq.getDN()))
        {
            return false;
        }

        final LDAPModification[] expectedMods = expectedReq.getModifications();

        final LDAPModification[] resultMods = resultReq.getModifications();

        if ((expectedMods == null) && (resultMods == null))
        {
            return true;
        }

        if ((expectedMods == null) && (resultMods != null))
        {
            return false;
        }

        if ((expectedMods != null) && (resultMods == null))
        {
            return false;
        }

        if (expectedMods.length != resultMods.length)
        {
            return false;
        }
        final Comparator comp = new LDAPModificationComparator();

        Arrays.sort(expectedMods, comp);
        Arrays.sort(resultMods, comp);

        for (int i = 0; i < resultMods.length; i++)
        {
            final LDAPModification modificationR = resultMods[i];
            final LDAPModification modificationE = expectedMods[i];

            if (!modificationE.getAttribute().getName().equals(
                    modificationR.getAttribute().getName()))
            {
                return false;

            }

            if (!modificationE.getAttribute().getStringValue().equals(
                    modificationR.getAttribute().getStringValue()))
            {
                return false;

            }
        }

        return true;

        /*
         * ByteArrayOutputStream out = new ByteArrayOutputStream();
         * ByteArrayOutputStream out1 = new ByteArrayOutputStream();
         * 
         * String s1 = out1.toString(); String s2 = out.toString(); // crop
         * requestID which is always different s1 = cropTillDn(s1); s2 =
         * cropTillDn(s2);
         * 
         * logger.debug(s1); logger.debug(s2);
         * 
         * return s1.equals(s2);
         */
    }

    /*
     * private static String cropTillDn(String str) {
     * 
     * int index = str.indexOf("dn="); return str.substring(index); }
     */

    private static class LDAPModificationComparator implements Comparator
    {

        public int compare(final Object o1, final Object o2)
        {
            final LDAPModification expectedMods = (LDAPModification) o1;
            final LDAPModification resultMods = (LDAPModification) o2;

            return expectedMods.getAttribute().getName().compareTo(
                    resultMods.getAttribute().getName());
        }

    }

}
