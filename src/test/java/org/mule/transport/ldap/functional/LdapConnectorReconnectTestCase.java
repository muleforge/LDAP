package org.mule.transport.ldap.functional;

import java.util.Map;

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleMessage;
import org.mule.module.client.MuleClient;
import org.mule.transport.ldap.transformers.LDAPSearchResultToString;
import org.mule.transport.ldap.util.DSManager;
import org.mule.transport.ldap.util.TestHelper;

import com.novell.ldap.LDAPAddRequest;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPMessage;
import com.novell.ldap.LDAPSearchResults;
import com.novell.ldap.util.DN;

public class LdapConnectorReconnectTestCase extends
        AbstractLdapFunctionalTestCase
{

    @Override
    protected String getConfigResources()
    {

        return super.getConfigResources() + ",ldap-functional-config.xml";
    }

    
    public void testTeconnect() throws Exception
    {
        doSearchInternal();
               
        DSManager.getInstance().stop();
        
     
        
        
        DSManager.getInstance().start();
        
     
       
        doSearchInternal();
        
      
        
        
    }
    
    private void doSearchInternal() throws Exception
    {

        final MuleClient client = new MuleClient(muleContext);
        final MuleMessage msg = client.send("ldap://ldap.out/",
                new DefaultMuleMessage("(objectclass=*)", muleContext));
        assertNotNull(msg);
        assertTrue(msg.getPayload() instanceof LDAPSearchResults);

        final LDAPSearchResultToString trans = new LDAPSearchResultToString();
        final String s = (String) trans.transform(msg.getPayload());

        assertNotNull(s);
        assertTrue(s.indexOf("<batchResponse") > -1);
        assertTrue(s.indexOf("<searchResultEntry") > -1);
        assertTrue(s.indexOf("dn=\"o=sevenseas\"><attr name=\"o\">") > -1);

        

    }

   

}
