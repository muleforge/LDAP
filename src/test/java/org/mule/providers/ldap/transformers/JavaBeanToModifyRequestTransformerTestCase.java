package org.mule.providers.ldap.transformers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.umo.transformer.UMOTransformer;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPModification;
import com.novell.ldap.LDAPModifyRequest;

public class JavaBeanToModifyRequestTransformerTestCase extends
		org.mule.tck.AbstractTransformerTestCase {

    protected final Log logger = LogFactory.getLog(getClass());
    
	public Object getResultData() {

		JavaBeanClass bean = new JavaBeanClass();

		try {

			LDAPModification[] mods = new LDAPModification[3];
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
		} catch (LDAPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public Object getTestData() {

		return new JavaBeanClass();

	}

	public UMOTransformer getTransformer() throws Exception {

		return new JavaBeanToModifyRequest();
	}

	public UMOTransformer getRoundTripTransformer() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public static class JavaBeanClass {
		private String dn = "ou=system";
		private String mail = "mail@mail.com";
		private int age = 34;
		private String field12 = "field12";

		public String getDn() {
			return dn;
		}

		public void setDn(String dn) {
			this.dn = dn;
		}

		public String getMail() {
			return mail;
		}

		public void setMail(String mail) {
			this.mail = mail;
		}

		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}

		public String getField12() {
			return field12;
		}

		public void setField12(String field12) {
			this.field12 = field12;
		}

	}

	public boolean compareResults(Object expected, Object result) {

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayOutputStream out1 = new ByteArrayOutputStream();

		try {
			((LDAPModifyRequest) expected).writeDSML(out);

			((LDAPModifyRequest) result).writeDSML(out1);
		} catch (IOException e) {

			logger.error(e.toString(), e);
			return false;
		}

		String s1 = out1.toString();
		String s2 = out.toString();

		// crop requestID which is always different
		s1 = cropTillDn(s1);
		s2 = cropTillDn(s2);

		logger.debug(s1);
		logger.debug(s2);

		return s1.equals(s2);
	}
	
	private static String cropTillDn(String str)
	{
	    
	    int index = str.indexOf("dn=");
	    return str.substring(index);
	    
	    
	}

}
