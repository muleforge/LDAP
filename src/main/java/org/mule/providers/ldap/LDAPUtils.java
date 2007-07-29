package org.mule.providers.ldap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.impl.MuleMessage;
import org.mule.umo.endpoint.UMOImmutableEndpoint;

public class LDAPUtils {

	
	protected static final Log logger = LogFactory.getLog(LDAPUtils.class);
	
	protected static String getSearchStringFromEndpoint(UMOImmutableEndpoint endpoint, Object transformedMessage)
    throws Exception
	{
		
		String searchStr = endpoint.getEndpointURI().getPath();
		
		if(searchStr==null || "".equals(searchStr))
			   searchStr = endpoint.getEndpointURI().getAddress();
		
		if(searchStr.startsWith("/")) searchStr = searchStr.substring(1);
		
		logger.debug("path: "+endpoint.getEndpointURI().getPath());
		logger.debug("adress: "+endpoint.getEndpointURI().getAddress());
		logger.debug("query: "+endpoint.getEndpointURI().getQuery());
		logger.debug("ressource info: "+endpoint.getEndpointURI().getResourceInfo());
		logger.debug("userinfo: "+endpoint.getEndpointURI().getUserInfo());
		logger.debug("user params: "+endpoint.getEndpointURI().getUserParams());
		
		logger.debug("searchStr0: "+searchStr);
		
		LdapConnector ldapConnector = (LdapConnector) endpoint.getConnector();
		
		String str;
        if ((str = ldapConnector.getQuery(endpoint, searchStr)) != null)
        {
        	searchStr = str;
        }
        searchStr = StringUtils.trimToEmpty(searchStr);
        if (StringUtils.isBlank(searchStr))
        {
            throw new IllegalArgumentException("Missing a search statement");
        }
		
        logger.debug("searchStr1: "+searchStr);
        
        List paramNames = new ArrayList();
        String searchStrTMP = ldapConnector.parseStatement(searchStr, paramNames);
        
        logger.debug("searchStrTMP: "+searchStrTMP);

        if(transformedMessage !=null)
        	logger.debug("type of transformed msg: "+transformedMessage.getClass());
        
        Object[] paramValues = ldapConnector.getParams(endpoint, paramNames, new MuleMessage(
            transformedMessage));
        
        
        logger.debug("paramValues: "+Arrays.toString(paramValues));
        
        searchStr = ldapConnector.parseStatement2(searchStr, paramValues);
		
		logger.debug("searchStr final: "+searchStr);
		
		return searchStr;

		
	}
	
	
}
