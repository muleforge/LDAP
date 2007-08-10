/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.ldap;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mule.config.i18n.CoreMessages;
import org.mule.providers.AbstractConnector;
import org.mule.providers.ConnectException;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOException;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.endpoint.UMOImmutableEndpoint;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.provider.UMOMessageReceiver;
import org.mule.util.ClassUtils;
import org.mule.util.StringUtils;
import org.mule.util.properties.BeanPropertyExtractor;
import org.mule.util.properties.MapPropertyExtractor;
import org.mule.util.properties.MessagePropertyExtractor;
import org.mule.util.properties.PayloadPropertyExtractor;
import org.mule.util.properties.PropertyExtractor;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPMessage;
import com.novell.ldap.LDAPMessageQueue;
import com.novell.ldap.LDAPSearchConstraints;
import com.novell.ldap.LDAPUnsolicitedNotificationListener;

/**
 * <code>LdapConnector</code> TODO document
 */
public class LdapConnector extends AbstractConnector
{

    private static final int ldapVersion = LDAPConnection.LDAP_V3;
    private static final int DEFAULT_STRINGBUFFER_SIZE = 200;
    public static final String PROPERTY_POLLING_FREQUENCY = "pollingFrequency";
    public static final String PROPERTY_START_UNSOLICITED_NOTIFICATION_LISTENER = "startUnsolicitedNotificationListener";
    private static final Pattern STATEMENT_ARGS = Pattern
            .compile("\\$\\{[^\\}]*\\}");

    public static final long DEFAULT_POLLING_FREQUENCY = 1000;

    private LDAPMessageQueue messageQueue = null;

    private long pollingFrequency = DEFAULT_POLLING_FREQUENCY;

    private int ldapPort = LDAPConnection.DEFAULT_PORT;

    private int searchScope = LDAPConnection.SCOPE_SUB;

    private String ldapHost = null;

    private String loginDN = null;

    private String password = null;

    private String searchBase = null;

    private boolean startUnsolicitedNotificationListener = false;

    private List attributes = null; // attributes, default all

    private int dereference = LDAPSearchConstraints.DEREF_NEVER; // dereference,
    // default
    // never

    private int maxResults = Integer.MAX_VALUE; // maxresults, default
    // Integer.MAX_VALUE

    private int timeLimit = 0; // Timelimit, default 0 (no time limit)

    private boolean typesOnly = false; // types only, default false;

    private LDAPConnection ldapConnection = null;

    private Map queries = null;

    private Set propertyExtractors = null;

    private Set queryValueExtractors = null;

    protected void doInitialise() throws InitialisationException
    {

        try
        {
            // setup property Extractors for queries
            if (queryValueExtractors == null)
            {
                // Add defaults
                queryValueExtractors = new HashSet();
                queryValueExtractors.add(MessagePropertyExtractor.class
                        .getName());
                // queryValueExtractors.add(NowPropertyExtractor.class.getName());
                queryValueExtractors.add(PayloadPropertyExtractor.class
                        .getName());
                queryValueExtractors.add(MapPropertyExtractor.class.getName());
                queryValueExtractors.add(BeanPropertyExtractor.class.getName());

                if (ClassUtils.isClassOnPath(
                        "org.mule.util.properties.Dom4jPropertyExtractor",
                        getClass()))
                {
                    queryValueExtractors
                            .add("org.mule.util.properties.Dom4jPropertyExtractor");
                }

                if (ClassUtils.isClassOnPath(
                        "org.mule.util.properties.JDomPropertyExtractor",
                        getClass()))
                {
                    queryValueExtractors
                            .add("org.mule.util.properties.JDomPropertyExtractor");
                }
            }

            propertyExtractors = new HashSet();
            for (Iterator iterator = queryValueExtractors.iterator(); iterator
                    .hasNext();)
            {
                String s = (String) iterator.next();
                propertyExtractors.add(ClassUtils.instanciateClass(s,
                        ClassUtils.NO_ARGS));
            }
        }
        catch (SecurityException e)
        {
            throw new InitialisationException(CoreMessages
                    .failedToCreate("LDAP Connector"), e, this);
        }
        catch (IllegalArgumentException e)
        {
            throw new InitialisationException(CoreMessages
                    .failedToCreate("LDAP Connector"), e, this);
        }
        catch (ClassNotFoundException e)
        {
            throw new InitialisationException(CoreMessages
                    .failedToCreate("LDAP Connector"), e, this);
        }
        catch (NoSuchMethodException e)
        {
            throw new InitialisationException(CoreMessages
                    .failedToCreate("LDAP Connector"), e, this);
        }
        catch (InstantiationException e)
        {
            throw new InitialisationException(CoreMessages
                    .failedToCreate("LDAP Connector"), e, this);
        }
        catch (IllegalAccessException e)
        {
            throw new InitialisationException(CoreMessages
                    .failedToCreate("LDAP Connector"), e, this);
        }
        catch (InvocationTargetException e)
        {
            throw new InitialisationException(CoreMessages
                    .failedToCreate("LDAP Connector"), e, this);
        }

    }

    protected void ensureConnected() throws ConnectException
    {

        if (this.isDisposing())
        {
            return;
        }

        if (ldapConnection == null || !ldapConnection.isConnected()
                || !ldapConnection.isConnectionAlive())
        {
            logger.warn("ensureConnected() failed");
            throw (new ConnectException(
                    CoreMessages.connectorCausedError(this), this));
        }

    }

    protected void setLDAPConnection()
    {
        ldapConnection = new LDAPConnection();
    }

    protected void doBind() throws Exception
    {

        ldapConnection.bind(ldapVersion, loginDN, password.getBytes("UTF8"));
    }

    protected boolean isAnonymousBindSupported()
    {
        return true;
    }

    public final void doConnect() throws Exception
    {

        logger.debug("try connect to " + ldapHost + ":" + ldapPort + " ...");

        if (StringUtils.isEmpty(ldapHost))
        {
            throw new IllegalArgumentException("ldapHost must not be empty");
        }

        if (password == null)
        {
            password = "";
        }

        setLDAPConnection();

        ldapConnection.connect(ldapHost, ldapPort);

        logger.debug("connected to " + ldapHost + ":" + ldapPort);

        // lc.isBound()
        // note: an anonymous bind returns false - not bound
        // but do not work correct

        if (isAnonymousBindSupported() && StringUtils.isEmpty(loginDN)
                && isAnonBind())
        {
            logger.debug("anonymous bind to " + ldapHost + " successful");
        }
        else if (!StringUtils.isEmpty(loginDN))
        {
            doBind();
            logger.debug("non-anonymous bind of " + loginDN + " successful");
        }
        else
        {
            throw new Exception(
                    "Unable to bind anonymous (either failed or not supported (SSL/SASL)");
        }

        logger.debug("ldap constraints " + ldapConnection.getConstraints());

    }

    protected boolean isAnonBind()
    {
        try
        {
            LDAPEntry result = ldapConnection
                    .read("o=XddTz6544inv-II-test-UUI");
            result.getDN();
            return true;
        }
        catch (LDAPException e)
        {

            // excpected

            int resultCode = e.getResultCode();

            if (resultCode == LDAPException.NO_SUCH_OBJECT)
            {
                return true;
            }

            if (resultCode == LDAPException.INSUFFICIENT_ACCESS_RIGHTS)
            {
                return false;
            }

            // e.printStackTrace();

            return false;

        }
        catch (Exception e)
        {
            logger.error(e);
            return false;
        }

    }

    protected void addLDAPListener(LDAPUnsolicitedNotificationListener listener)
    {
        if (listener != null && isStartUnsolicitedNotificationListener())
        {
            ldapConnection.addUnsolicitedNotificationListener(listener);
            logger.debug(listener
                    + " registered as UnsolicitedNotificationListener");
        }
    }

    public void doDisconnect() throws Exception
    {

        if (ldapConnection != null)
        {
            try
            {
                ldapConnection.disconnect();
            }
            catch (LDAPException e)
            {
                // ignored
                ldapConnection = null;
            }
        }
        else
        {

            ldapConnection = null;

        }

    }

    public String getProtocol()
    {
        return "ldap";
    }

    protected void doAsyncRequest(LDAPMessage request) throws LDAPException
    {
        messageQueue = ldapConnection.sendRequest(request, messageQueue);
    }

    public int getLdapPort()
    {
        return ldapPort;
    }

    public void setLdapPort(int ldapPort)
    {
        this.ldapPort = ldapPort;
    }

    public int getSearchScope()
    {
        return searchScope;
    }

    public void setSearchScope(int searchScope)
    {
        this.searchScope = searchScope;
    }

    public int getLdapVersion()
    {
        return ldapVersion;
    }

    public String getLdapHost()
    {
        return ldapHost;
    }

    public void setLdapHost(String ldapHost)
    {
        this.ldapHost = ldapHost;
    }

    public String getLoginDN()
    {
        return loginDN;
    }

    public void setLoginDN(String loginDN)
    {
        this.loginDN = loginDN;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getSearchBase()
    {
        return searchBase;
    }

    public void setSearchBase(String searchBase)
    {
        this.searchBase = searchBase;
    }

    public long getPollingFrequency()
    {
        return pollingFrequency;
    }

    public void setPollingFrequency(long pollingFrequency)
    {
        this.pollingFrequency = pollingFrequency;
    }

    // @Override
    protected UMOMessageReceiver createReceiver(UMOComponent component,
            UMOEndpoint endpoint) throws Exception
    {

        long polling = pollingFrequency;
        Map props = endpoint.getProperties();
        if (props != null)
        {
            // Override properties on the endpoint for the specific endpoint
            String tempPolling = (String) props.get(PROPERTY_POLLING_FREQUENCY);
            if (tempPolling != null)
            {
                polling = Long.parseLong(tempPolling);
            }
        }
        if (polling <= 0)
        {
            polling = DEFAULT_POLLING_FREQUENCY;
        }
        logger.debug("set polling frequency to " + polling);

        try
        {

            return serviceDescriptor.createMessageReceiver(this, component,
                    endpoint, new Object[]
                    {new Long(polling)});

        }
        catch (Exception e)
        {
            throw new InitialisationException(CoreMessages
                    .failedToCreateObjectWith("Message Receiver",
                            serviceDescriptor.getMessageReceiver()), e, this);
        }
    }

    public boolean isStartUnsolicitedNotificationListener()
    {
        return startUnsolicitedNotificationListener;
    }

    public void setStartUnsolicitedNotificationListener(
            boolean startUnsolicitedNotificationListener)
    {
        this.startUnsolicitedNotificationListener = startUnsolicitedNotificationListener;
    }

    public String getQuery(UMOImmutableEndpoint endpoint, String stmt)
    {
        Object query = null;

        if (endpoint != null && endpoint.getProperties() != null)
        {
            Object queries = endpoint.getProperties().get("queries");
            if (queries instanceof Map)
            {
                query = ((Map) queries).get(stmt);

            }
        }
        if (query == null && this.queries != null)
        {

            query = this.queries.get(stmt);

        }

        return query == null ? null : query.toString();
    }

    /**
     * Parse the given statement filling the parameter list and return the ready
     * to use statement.
     * 
     * @param query
     * @param params
     * @return
     */

    public String parseQuery(String query, Object[] values)
    {

        logger.debug(query);
        logger.debug(Arrays.asList(values).toString());

        if (query == null)
        {
            return query;
        }
        Matcher m = STATEMENT_ARGS.matcher(query);
        StringBuffer sb = new StringBuffer(DEFAULT_STRINGBUFFER_SIZE);
        int i = 0;
        while (m.find())
        {

            m.appendReplacement(sb, values[i] == null ? "null" : values[i]
                    .toString());
            i++;
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public Object[] getParams(UMOImmutableEndpoint endpoint, List paramNames,
            Object message) throws Exception
    {
        Object[] params = new Object[paramNames.size()];
        for (int i = 0; i < paramNames.size(); i++)
        {
            String param = (String) paramNames.get(i);
            String name = param.substring(2, param.length() - 1);
            Object value = null;
            // If we find a value and it happens to be null, thats acceptable
            boolean foundValue = false;
            if (message != null)
            {
                for (Iterator iterator = propertyExtractors.iterator(); iterator
                        .hasNext();)
                {
                    PropertyExtractor pe = (PropertyExtractor) iterator.next();
                    value = pe.getProperty(name, message);
                    if (value != null)
                    {
                        if (value.equals(StringUtils.EMPTY)
                                && pe instanceof BeanPropertyExtractor)
                        {
                            value = null;
                        }
                        foundValue = true;
                        break;
                    }
                }
            }
            if (!foundValue)
            {
                value = endpoint.getProperty(name);
            }

            // Allow null values which may be acceptable to the user
            // Why shouldn't nulls be allowed? Otherwise every null parameter
            // has to
            // be defined
            // if (value == null && !foundValue)
            // {
            // throw new IllegalArgumentException("Can not retrieve argument " +
            // name);
            // }
            params[i] = value;
        }
        return params;
    }

    // @Override
    protected void doDispose()
    {

    }

    // @Override
    protected void doStart() throws UMOException
    {

    }

    // @Override
    protected void doStop() throws UMOException
    {

    }

    public LDAPMessageQueue getMessageQueue()
    {
        return messageQueue;
    }

    public void setMessageQueue(LDAPMessageQueue messageQueue)
    {
        this.messageQueue = messageQueue;
    }

    public Map getQueries()
    {
        return queries;
    }

    public void setQueries(Map queries)
    {
        this.queries = queries;
    }

    public List getAttributes()
    {
        return attributes;
    }

    public void setAttributes(List attributes)
    {
        this.attributes = attributes;
    }

    public int getDereference()
    {
        return dereference;
    }

    public void setDereference(int dereference)
    {
        this.dereference = dereference;
    }

    public int getMaxResults()
    {
        return maxResults;
    }

    public void setMaxResults(int maxResults)
    {
        this.maxResults = maxResults;
    }

    public int getTimeLimit()
    {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit)
    {
        this.timeLimit = timeLimit;
    }

    public boolean isTypesOnly()
    {
        return typesOnly;
    }

    public void setTypesOnly(boolean typesOnly)
    {
        this.typesOnly = typesOnly;
    }

    public String parseStatement(String stmt, List params)
    {
        if (stmt == null)
        {
            return stmt;
        }
        Matcher m = STATEMENT_ARGS.matcher(stmt);
        StringBuffer sb = new StringBuffer(DEFAULT_STRINGBUFFER_SIZE);
        while (m.find())
        {
            String key = m.group();
            m.appendReplacement(sb, "?");
            params.add(key);
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public LDAPConnection getLdapConnection()
    {
        return ldapConnection;
    }

    protected void setLdapConnection(LDAPConnection ldapConnection)
    {
        this.ldapConnection = ldapConnection;
    }
}
