/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.ldap.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.DefaultMuleMessage;
import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.transport.ldap.LdapConnector;

import com.novell.ldap.LDAPControl;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPMessage;
import com.novell.ldap.LDAPSearchRequest;
import com.novell.ldap.controls.LDAPPersistSearchControl;

public final class LDAPUtils
{

    private static final Log logger = LogFactory.getLog(LDAPUtils.class);

    private LDAPUtils()
    {
        // never instantiated
    }

    public static LDAPSearchRequest createSearchRequest(
            final LdapConnector ldapConnector, final String searchString)
            throws LDAPException
    {

        final List attrs = ldapConnector.getAttributes();
        String[] attributes = null;

        if (attrs != null)
        {
            attributes = (String[]) attrs.toArray(new String[attrs.size()]);
        }

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
            final ImmutableEndpoint endpoint, final Object transformedMessage)
            throws Exception
    {

        String searchStr = endpoint.getEndpointURI().getPath();

        if (searchStr.startsWith("/"))
        {
            searchStr = searchStr.substring(1);
        }

        logger.debug("path: " + endpoint.getEndpointURI().getPath());
        logger.debug("adress: " + endpoint.getEndpointURI().getAddress());

        final LdapConnector ldapConnector = (LdapConnector) endpoint
                .getConnector();

        final String str = ldapConnector.getQuery(endpoint, searchStr);

        logger.debug(".getQuery: " + str);

        if (str != null)
        {
            searchStr = str;
        }
        searchStr = org.apache.commons.lang.StringUtils.trimToEmpty(searchStr);

        if (org.apache.commons.lang.StringUtils.isBlank(searchStr))
        {
            searchStr = transformedMessage.toString();
        }

        searchStr = org.apache.commons.lang.StringUtils.trimToEmpty(searchStr);

        if (org.apache.commons.lang.StringUtils.isBlank(searchStr))
        {
            throw new IllegalArgumentException("Missing a search statement");
        }

        final List paramNames = new ArrayList();

        if (transformedMessage != null)
        {
            logger.debug("type of transformed msg: "
                    + transformedMessage.getClass());
        }

        // logger.debug("transformed msg: " + transformedMessage);

        logger.debug("searchStr1:" + searchStr);
        logger.debug("paramNames1:" + paramNames);

        ldapConnector.parseStatement(searchStr, paramNames);

        logger.debug("paramNames2:" + paramNames);

        // TODO "" query not ok
        final Object[] paramValues = ldapConnector.getParams(endpoint,
                paramNames, new DefaultMuleMessage(transformedMessage,
                        (Map) null), "");

        logger.debug("paramValues: "
                + java.util.Arrays.asList(paramValues).toString());
        logger.debug("searchStr before parsing: " + searchStr);

        searchStr = ldapConnector.parseQuery(searchStr, paramValues);

        logger.debug("searchStr after parsing: " + searchStr);

        return searchStr;

    }

    public static String dumpLDAPMessage(final LDAPMessage ldapMsg)
    {
        if (ldapMsg == null)
        {
            return "null (LDAPMessage)";
        }

        final StringBuffer sb = new StringBuffer();
        sb.append("LDAPMessage" + "\n");
        sb.append("Class: " + ldapMsg.getClass() + "\n");
        sb.append("Type: " + evaluateMessageType(ldapMsg) + "\n");
        sb.append("ID: " + ldapMsg.getMessageID() + "\n");
        sb.append("Tag: " + ldapMsg.getTag() + "\n");
        sb.append("toString(): " + ldapMsg.toString() + "\n");

        return sb.toString();
    }

    public static String dumpLDAPEntry(final LDAPEntry ldapMsg)
    {
        if (ldapMsg == null)
        {
            return "null (LDAPEntry)";
        }

        final StringBuffer sb = new StringBuffer();
        sb.append("LDAPEntry" + "\n");
        sb.append("Class: " + ldapMsg.getClass() + "\n");
        sb.append("DN: " + ldapMsg.getDN() + "\n");
        sb.append("toString(): " + ldapMsg.toString() + "\n");
        return sb.toString();
    }

    public static String dumpLDAPMessage(final Object msg)
    {

        if (msg == null)
        {
            return "null (Object)";
        }

        final StringBuffer sb = new StringBuffer("\n");

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
            final Collection ldapMsg = (Collection) msg;

            sb.append("Collection" + "\n");
            sb.append("toString(): " + msg + "\n");
            sb.append("Details:\n");

            for (final Iterator iterator = ldapMsg.iterator(); iterator
                    .hasNext();)
            {
                final Object obj = iterator.next();

                if (obj instanceof LDAPMessage)
                {
                    sb.append("   " + dumpLDAPMessage((LDAPMessage) obj));
                }
                else if (obj instanceof LDAPEntry)
                {
                    sb.append("   " + dumpLDAPEntry((LDAPEntry) obj));
                }
                else
                {
                    sb.append("Unknown\n");
                    sb.append("Class: " + obj.getClass() + "\n");
                    sb.append("toString(): " + obj + "\n");
                }

            }

        }

        return sb.toString();
    }

    public static String evaluateMessageType(final LDAPMessage msg)
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

    public static String getChangeTypeString(final int changeType)
    {

        String changeTypeString;

        switch (changeType)
        {

        case LDAPPersistSearchControl.ADD:

            changeTypeString = "ADD";

            break;

        case LDAPPersistSearchControl.MODIFY:

            changeTypeString = "MODIFY";

            break;

        case LDAPPersistSearchControl.MODDN:

            changeTypeString = "MODDN";

            break;

        case LDAPPersistSearchControl.DELETE:

            changeTypeString = "DELETE";

            break;

        default:

            changeTypeString =

            "Unknown change type: " + String.valueOf(changeType);

            break;

        }

        return changeTypeString;

    }

}
