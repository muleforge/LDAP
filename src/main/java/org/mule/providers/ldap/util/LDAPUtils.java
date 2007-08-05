package org.mule.providers.ldap.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.impl.MuleMessage;
import org.mule.providers.ldap.LdapConnector;
import org.mule.umo.endpoint.UMOImmutableEndpoint;
import org.mule.util.StringUtils;

import com.novell.ldap.LDAPControl;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPMessage;
import com.novell.ldap.LDAPSearchRequest;

public class LDAPUtils
{

    protected static final Log logger = LogFactory.getLog(LDAPUtils.class);

    public static LDAPSearchRequest createSearchRequest(
            LdapConnector ldapConnector, String searchString)
            throws LDAPException
    {

        List attrs = ldapConnector.getAttributes();
        String[] attributes = (attrs == null ? null : (String[]) attrs
                .toArray(new String[0]));

        // TODO LDAPCOntrols
        return new LDAPSearchRequest(ldapConnector.getSearchBase(), // base
                ldapConnector.getSearchScope(), // scope
                searchString, // filter
                attributes, // attributes
                ldapConnector.getDereference(), // dereference
                ldapConnector.getMaxResults(), // maxresults
                ldapConnector.getTimeLimit(), // Timelimit
                ldapConnector.isTypesOnly(), // types only
                (LDAPControl[]) null // controls
        );
    }

    public static String getSearchStringFromEndpoint(
            UMOImmutableEndpoint endpoint, Object transformedMessage)
            throws Exception
    {

        String searchStr = endpoint.getEndpointURI().getPath();

        // if (searchStr == null || "".equals(searchStr))
        // searchStr = endpoint.getEndpointURI().getAddress();

        if (searchStr.startsWith("/"))
            searchStr = searchStr.substring(1);

        logger.debug("path: " + endpoint.getEndpointURI().getPath());
        logger.debug("adress: " + endpoint.getEndpointURI().getAddress());
        // logger.debug("query: " + endpoint.getEndpointURI().getQuery());
        // logger.debug("ressource info: "
        // + endpoint.getEndpointURI().getResourceInfo());
        // logger.debug("userinfo: " + endpoint.getEndpointURI().getUserInfo());
        // logger.debug("user params: "
        // + endpoint.getEndpointURI().getUserParams());

        // logger.debug("searchStr0: " + searchStr);

        LdapConnector ldapConnector = (LdapConnector) endpoint.getConnector();

        String str;
        if ((str = ldapConnector.getQuery(endpoint, searchStr)) != null)
        {
            searchStr = str;
        }
        searchStr = StringUtils.trimToEmpty(searchStr);

        if (StringUtils.isBlank(searchStr))
        {
            searchStr = transformedMessage.toString();
        }

        searchStr = StringUtils.trimToEmpty(searchStr);

        if (StringUtils.isBlank(searchStr))
        {
            throw new IllegalArgumentException("Missing a search statement");
        }

        // logger.debug("searchStr1: " + searchStr);

        List paramNames = new ArrayList();

        if (transformedMessage != null)
            logger.debug("type of transformed msg: "
                    + transformedMessage.getClass());

        logger.debug("transformed msg: " + transformedMessage);

        ldapConnector.parseStatement(searchStr, paramNames);

        Object[] paramValues = ldapConnector.getParams(endpoint, paramNames,
                new MuleMessage(transformedMessage));

        logger.debug("paramValues: " + Arrays.asList(paramValues).toString());
        logger.debug("searchStr before parsing: " + searchStr);

        searchStr = ldapConnector.parseQuery(searchStr, paramValues);

        logger.debug("searchStr: " + searchStr);

        return searchStr;

    }

    public static String dumpLDAPMessage(LDAPMessage ldapMsg)
    {
        if (ldapMsg == null)
            return "null (LDAPMessage)";

        StringBuffer sb = new StringBuffer();
        sb.append("LDAPMessage" + "\n");
        sb.append("Class: " + ldapMsg.getClass() + "\n");
        sb.append("Type: " + evaluateMessageType(ldapMsg) + "\n");
        sb.append("ID: " + ldapMsg.getMessageID() + "\n");
        sb.append("Tag: " + ldapMsg.getTag() + "\n");
        sb.append("toString(): " + ldapMsg.toString() + "\n");
        
        return sb.toString();
    }

    public static String dumpLDAPEntry(LDAPEntry ldapMsg)
    {
        if (ldapMsg == null)
            return "null (LDAPEntry)";

        StringBuffer sb = new StringBuffer();
        sb.append("LDAPEntry" + "\n");
        sb.append("Class: " + ldapMsg.getClass() + "\n");
        sb.append("DN: " + ldapMsg.getDN() + "\n");
        sb.append("toString(): " + ldapMsg.toString() + "\n");
        return sb.toString();
    }

    public static String dumpLDAPMessage(Object msg)
    {

        if (msg == null)
            return "null (Object)";

        StringBuffer sb = new StringBuffer("\n");

        sb.append("Class: " + msg.getClass() + "\n");

        if (msg instanceof LDAPMessage)
        {
            sb.append(dumpLDAPMessage((LDAPMessage) msg));
        }

        if (msg instanceof LDAPEntry)
        {
            sb.append(dumpLDAPEntry((LDAPEntry) msg));
        }

        if (msg instanceof String)
        {

            sb.append("String" + "\n");
            sb.append("toString(): " + msg + "\n");
        }

        if (msg instanceof Collection)
        {
            Collection ldapMsg = (Collection) msg;

            sb.append("Collection" + "\n");
            sb.append("toString(): " + msg + "\n");
            sb.append("Details:\n");

            for (Iterator iterator = ldapMsg.iterator(); iterator.hasNext();)
            {
                Object obj = iterator.next();

                if (obj instanceof LDAPMessage)
                {
                    sb.append("   " + dumpLDAPMessage((LDAPMessage) obj));
                } else if (obj instanceof LDAPEntry)
                {
                    sb.append("   " + dumpLDAPEntry((LDAPEntry) obj));
                } else
                {
                    sb.append("Unknown\n");
                    sb.append("Class: " + obj.getClass() + "\n");
                    sb.append("toString(): " + obj + "\n");
                }

            }

        }

        return sb.toString();
    }

    public static String evaluateMessageType(LDAPMessage msg)
    {

        String name;

        switch (msg.getType())
        {
        case LDAPMessage.SEARCH_RESPONSE:
            name = "LDAPSearchResponse";
            break;
        case LDAPMessage.SEARCH_RESULT:
            name = "LDAPSearchResult";
            break;
        case LDAPMessage.SEARCH_REQUEST:
            name = "LDAPSearchRequest";
            break;
        case LDAPMessage.MODIFY_REQUEST:
            name = "LDAPModifyRequest";
            break;
        case LDAPMessage.MODIFY_RESPONSE:
            name = "LDAPModifyResponse";
            break;
        case LDAPMessage.ADD_REQUEST:
            name = "LDAPAddRequest";
            break;
        case LDAPMessage.ADD_RESPONSE:
            name = "LDAPAddResponse";
            break;
        case LDAPMessage.DEL_REQUEST:
            name = "LDAPDelRequest";
            break;
        case LDAPMessage.DEL_RESPONSE:
            name = "LDAPDelResponse";
            break;
        case LDAPMessage.MODIFY_RDN_REQUEST:
            name = "LDAPModifyRDNRequest";
            break;
        case LDAPMessage.MODIFY_RDN_RESPONSE:
            name = "LDAPModifyRDNResponse";
            break;
        case LDAPMessage.COMPARE_REQUEST:
            name = "LDAPCompareRequest";
            break;
        case LDAPMessage.COMPARE_RESPONSE:
            name = "LDAPCompareResponse";
            break;
        case LDAPMessage.BIND_REQUEST:
            name = "LDAPBindRequest";
            break;
        case LDAPMessage.BIND_RESPONSE:
            name = "LDAPBindResponse";
            break;
        case LDAPMessage.UNBIND_REQUEST:
            name = "LDAPUnbindRequest";
            break;
        case LDAPMessage.ABANDON_REQUEST:
            name = "LDAPAbandonRequest";
            break;
        case LDAPMessage.SEARCH_RESULT_REFERENCE:
            name = "LDAPSearchResultReference";
            break;
        case LDAPMessage.EXTENDED_REQUEST:
            name = "LDAPExtendedRequest";
            break;
        case LDAPMessage.EXTENDED_RESPONSE:
            name = "LDAPExtendedResponse";
            break;
        case LDAPMessage.INTERMEDIATE_RESPONSE:
            name = "LDAPIntermediateResponse";
            break;
        default:
            return "Unknown";
        }
        return name + " (" + msg.getType() + ")";
    }

}