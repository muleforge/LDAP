/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.ldap;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mule.api.MuleException;
import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.config.i18n.CoreMessages;
import org.mule.transport.AbstractConnector;
import org.mule.transport.ConnectException;
import org.mule.transport.ldap.util.EndpointURIExpressionEvaluator;
import org.mule.util.expression.ExpressionEvaluatorManager;

import com.novell.ldap.LDAPAuthHandler;
import com.novell.ldap.LDAPAuthProvider;
import com.novell.ldap.LDAPBindHandler;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPMessage;
import com.novell.ldap.LDAPMessageQueue;
import com.novell.ldap.LDAPReferralException;
import com.novell.ldap.LDAPReferralHandler;
import com.novell.ldap.LDAPSearchConstraints;
import com.novell.ldap.LDAPUnsolicitedNotificationListener;
import com.novell.ldap.events.EventConstant;
import com.novell.ldap.events.PSearchEventListener;
import com.novell.ldap.events.PsearchEventSource;

/**
 * <code>LdapConnector</code> TODO document
 */
public class LdapConnector extends AbstractConnector implements
        LDAPReferralHandler, LDAPBindHandler, LDAPAuthHandler
{

    private static final int ldapVersion = LDAPConnection.LDAP_V3;

    private static final Pattern STATEMENT_ARGS = Pattern
            .compile("\\#\\[[^\\]]+\\]");

    private volatile LDAPMessageQueue messageQueue = null;

    private int ldapPort = LDAPConnection.DEFAULT_PORT;

    private int searchScope = LDAPConnection.SCOPE_SUB;

    private String ldapHost = null;

    private String loginDN = null;

    private String password = null;

    private String searchBase = null;

    private boolean startUnsolicitedNotificationListener = false;

    private List attributes = null; // attributes, default all

    // Specifies when aliases should be dereferenced. Must be either one of the
    // constants defined in LDAPConstraints, which are DEREF_NEVER,
    // DEREF_FINDING, DEREF_SEARCHING, or DEREF_ALWAYS.
    private int dereference = LDAPSearchConstraints.DEREF_NEVER; // dereference
    // aliases

    private int maxResults = Integer.MAX_VALUE; // maxresults, default
    // Integer.MAX_VALUE

    // server time limit
    private int timeLimit = 0; // Timelimit, default 0 (no time limit)

    private boolean typesOnly = false; // types only, default false;

    private LDAPConnection ldapConnection = null;

    private Map queries = null;

    private LDAPSearchConstraints constraints = null;

    // referrals
    private boolean doReferrals = false;

    // ps
    private final PsearchEventSource source = new PsearchEventSource();
    private boolean enablePersistentSearch = false;
    private List < String > psFilters = null;
    private boolean psChangeonly = true;
    private int psEventchangetype = EventConstant.LDAP_PSEARCH_ANY;

    public LdapConnector()
    {
        super();

        if (!ExpressionEvaluatorManager
                .isEvaluatorRegistered(EndpointURIExpressionEvaluator.NAME))
        {
            ExpressionEvaluatorManager
                    .registerEvaluator(new EndpointURIExpressionEvaluator());
        }

    }

    @Override
    protected void doInitialise() throws InitialisationException
    {

        /*
         * msLimit - The maximum time in milliseconds to wait for results. The
         * default is 0, which means that there is no maximum time limit. This
         * limit is enforced for an operation by the API, not by the server. The
         * operation will be abandoned and terminated by the API with an
         * LDAPException.LDAP_TIMEOUT if the operation exceeds the time limit.
         * 
         * serverTimeLimit - The maximum time in seconds that the server should
         * spend returning search results. This is a server-enforced limit. The
         * default of 0 means no time limit. The operation will be terminated by
         * the server with an LDAPException.TIME_LIMIT_EXCEEDED if the search
         * operation exceeds the time limit.
         */

        // controls are not supported
        // constraints.setControls(control)
    }

    protected final void ensureConnected() throws ConnectException
    {

        if (this.isDisposing())
        {
            throw (new ConnectException(
                    CoreMessages.connectorCausedError(this), this));
        }

        if (ldapConnection != null)
        {
            logger.debug("connected?:" + ldapConnection.isConnected());
            // logger.debug(ldapConnection.isConnectionAlive());
        }

        if ((ldapConnection == null) || !ldapConnection.isConnected()
                || !ldapConnection.isConnectionAlive())
        {
            logger.warn("ensureConnected() failed");
            throw (new ConnectException(
                    CoreMessages.connectorCausedError(this), this));
        }

    }

    /*
     * private boolean isConnectionAlive() { if (this.ldapConnection == null ||
     * isConnected() == false) return false;
     * 
     * try { LDAPEntry resp = ldapConnection.read(""); logger.debug(resp);
     * 
     * if (resp != null) return true; } catch (LDAPException e) { // TODO
     * Auto-generated catch block e.printStackTrace(); }
     * 
     * return false; }
     */

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

    @Override
    public final void doConnect() throws Exception
    {

        // if (dynamicNotification)

        // this.setDynamicNotification(dynamic);

        // this.updateCachedNotificationHandler();

        logger.debug("try connect to " + ldapHost + ":" + ldapPort + " ...");

        if (org.apache.commons.lang.StringUtils.isEmpty(ldapHost))
        {
            throw new IllegalArgumentException("ldapHost must not be empty");
        }

        if (password == null)
        {
            password = "";
        }

        setLDAPConnection();

        ldapConnection.connect(ldapHost, ldapPort);

        constraints = new LDAPSearchConstraints(this.timeLimit * 1000, // client
                // timeout,
                // ms
                this.timeLimit, // serverTimeLimit sec
                this.dereference, this.maxResults, doReferrals,// boolean
                // doReferrals
                1,// batchsize
                this, 10); // int hop_limit

        ldapConnection.setConstraints(constraints);

        logger.debug("connected to " + ldapHost + ":" + ldapPort);

        // lc.isBound()
        // note: an anonymous bind returns false - not bound
        // but do not work correct

        if (isAnonymousBindSupported()
                && org.apache.commons.lang.StringUtils.isEmpty(loginDN)
                && isAnonBind())
        {
            logger.debug("anonymous bind to " + ldapHost + " successful");
        }
        else if (!org.apache.commons.lang.StringUtils.isEmpty(loginDN))
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

    protected final boolean isAnonBind()
    {
        try
        {
            final LDAPEntry result = ldapConnection
                    .read("o=XddTz6544inv-II-test-UUI");
            result.getDN();
            return true;
        }
        catch (final LDAPException e)
        {

            // excpected

            final int resultCode = e.getResultCode();

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
        catch (final Exception e)
        {
            logger.error(e);
            return false;
        }

    }

    protected void addLDAPUnsolicitedNotificationListener(
            final LDAPUnsolicitedNotificationListener listener)
    {
        if ((listener != null) && isStartUnsolicitedNotificationListener())
        {
            ldapConnection.addUnsolicitedNotificationListener(listener);
            logger.debug(listener
                    + " registered as UnsolicitedNotificationListener");
        }
    }

    @Override
    public final void doDisconnect() throws Exception
    {

        if (ldapConnection != null)
        {
            try
            {
                ldapConnection.disconnect();
            }
            catch (final LDAPException e)
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

    protected final synchronized void doAsyncRequest(final LDAPMessage request)
            throws LDAPException
    {

        logger.debug("entering doAsyncRequest(): " + request.getTag());

        if (messageQueue == null)
        {
            messageQueue = ldapConnection.sendRequest(request, null);
            logger.debug("first async message, message queue initialised!");
            // new DumpThread(this.messageQueue).start();
        }
        else
        {
            ldapConnection.sendRequest(request, messageQueue);
        }

        logger.debug("leaving doAsyncRequest()");

    }

    public int getLdapPort()
    {
        return ldapPort;
    }

    public void setLdapPort(final int ldapPort)
    {
        this.ldapPort = ldapPort;
    }

    public int getSearchScope()
    {
        return searchScope;
    }

    public void setSearchScope(final int searchScope)
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

    public void setLdapHost(final String ldapHost)
    {
        this.ldapHost = ldapHost;
    }

    public String getLoginDN()
    {
        return loginDN;
    }

    public void setLoginDN(final String loginDN)
    {
        this.loginDN = loginDN;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(final String password)
    {
        this.password = password;
    }

    public String getSearchBase()
    {
        return searchBase;
    }

    public void setSearchBase(final String searchBase)
    {
        this.searchBase = searchBase;
    }

    /*
     * public long getPollingFrequency() { return pollingFrequency; }
     * 
     * public void setPollingFrequency(long pollingFrequency) {
     * this.pollingFrequency = pollingFrequency; }
     */

    /*
     * @Override
     * 
     * @Deprecated public MessageReceiver createReceiver(final Service service,
     *             final InboundEndpoint endpoint) throws Exception {
     * 
     * 
     * long polling = pollingFrequency; Map props = endpoint.getProperties(); if
     * (props != null) { // Override properties on the endpoint for the specific
     * endpoint String tempPolling = (String)
     * props.get(PROPERTY_POLLING_FREQUENCY); if (tempPolling != null) { polling =
     * Long.parseLong(tempPolling); } } if (polling <= 0) { polling =
     * DEFAULT_POLLING_FREQUENCY; } logger.debug("set polling frequency to " +
     * polling);
     * 
     * try {
     * 
     * return getServiceDescriptor().createMessageReceiver(this, service,
     * endpoint); } catch (final Exception e) { // TODO getServiceDescriptor()
     * maybe not correct throw new InitialisationException(CoreMessages
     * .failedToCreateObjectWith("Message Receiver", getServiceDescriptor()), e,
     * this); } }
     */
    public boolean isStartUnsolicitedNotificationListener()
    {
        return startUnsolicitedNotificationListener;
    }

    public void setStartUnsolicitedNotificationListener(
            final boolean startUnsolicitedNotificationListener)
    {
        this.startUnsolicitedNotificationListener = startUnsolicitedNotificationListener;
    }

    public String getQuery(final ImmutableEndpoint endpoint, final String stmt)
    {
        logger.debug("stmt: " + stmt);
        logger.debug("this.queries " + this.queries);

        Object query = null;

        if ((endpoint != null) && (endpoint.getProperties() != null))
        {
            final Object queries = endpoint.getProperties().get("queries");
            if (queries instanceof Map)
            {
                query = ((Map) queries).get(stmt);

            }
        }
        if ((query == null) && (this.queries != null))
        {

            query = this.queries.get(stmt);

        }

        return query == null ? null : query.toString();
    }

    public String parseQuery(final String query, final Object[] values)
    {

        logger.debug(query);
        logger.debug(Arrays.asList(values).toString());

        if (query == null)
        {
            return query;
        }
        final Matcher m = STATEMENT_ARGS.matcher(query);
        final StringBuffer sb = new StringBuffer(200);
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

    public final Object[] getParams(final ImmutableEndpoint endpoint,
            final List paramNames, final Object message, final String query)
            throws Exception
    {

        final Object[] params = new Object[paramNames.size()];
        for (int i = 0; i < paramNames.size(); i++)
        {
            final String param = (String) paramNames.get(i);
            Object value = null;
            // If we find a value and it happens to be null, thats acceptable
            boolean foundValue = false;
            boolean validExpression = ExpressionEvaluatorManager
                    .isValidExpression(param);
            // There must be an expression namespace to use the
            // ExpresionEvaluator i.e. header:type
            if ((message != null) && validExpression)
            {
                value = ExpressionEvaluatorManager.evaluate(param, message);
                foundValue = value != null;
            }
            if (!foundValue)
            {
                final String name = param.substring(2, param.length() - 1);
                // MULE-3597
                if (!validExpression)
                {
                    logger
                            .error("Config is using the legacy param format (no evaluator defined).");
                }
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

    /*
     * @Deprecated public Object[] getParams(ImmutableEndpoint endpoint, List
     *             paramNames, Object message, String query) throws Exception {
     * 
     * Object[] params = new Object[paramNames.size()]; for (int i = 0; i <
     * paramNames.size(); i++) { String param = (String)paramNames.get(i);
     * String name = param.substring(2, param.length() - 1); Object value =
     * null; // If we find a value and it happens to be null, thats acceptable
     * boolean foundValue = false; //There must be an expression namespace to
     * use the ExpresionEvaluator i.e. header:type if (message != null &&
     * ExpressionEvaluatorManager.isValidExpression(name)) { value =
     * ExpressionEvaluatorManager.evaluate(name, message); foundValue =
     * value!=null; } if (!foundValue) { value = endpoint.getProperty(name); } //
     * Allow null values which may be acceptable to the user // Why shouldn't
     * nulls be allowed? Otherwise every null parameter has to // be defined //
     * if (value == null && !foundValue) // { // throw new
     * IllegalArgumentException("Can not retrieve argument " + // name); // }
     * params[i] = value; } return params; }
     */

    @Override
    protected final void doDispose()
    {
        messageQueue = null;
        ldapConnection = null;
    }

    final synchronized LDAPMessage pollQueue() throws LDAPException
    {

        LDAPMessage message = null;

        logger.debug("entering pollQueue()");

        if (messageQueue != null)
        {
            logger.debug("polling queue");

            if (getOutstandingMessageCount() > 0)
            {

                // block
                message = messageQueue.getResponse();

                if (message == null)
                {
                    logger.error("null message polled from queue");
                }
                else
                {
                    logger.debug("polling queue ... OK");
                    logger.debug("msg: " + message);
                }

            }
            else
            {
                logger.debug("no message quequed");
            }
        }
        else
        {
            logger.debug("message queue not initalised yet");
        }

        return message;

    }

    public final synchronized int getOutstandingMessageCount()
    {
        if (messageQueue != null)
        {
            return messageQueue.getMessageIDs().length;
        }

        throw new IllegalArgumentException("message queue not initalised");

    }

    public Map getQueries()
    {
        return queries;
    }

    public void setQueries(final Map queries)
    {
        this.queries = queries;
    }

    public List getAttributes()
    {
        return attributes;
    }

    public void setAttributes(final List attributes)
    {
        this.attributes = attributes;
    }

    public int getDereference()
    {
        return dereference;
    }

    public void setDereference(final int dereference)
    {
        this.dereference = dereference;
    }

    public int getMaxResults()
    {
        return maxResults;
    }

    public void setMaxResults(final int maxResults)
    {
        this.maxResults = maxResults;
    }

    public int getTimeLimit()
    {
        return timeLimit;
    }

    public void setTimeLimit(final int timeLimit)
    {
        this.timeLimit = timeLimit;
    }

    public boolean isTypesOnly()
    {
        return typesOnly;
    }

    public void setTypesOnly(final boolean typesOnly)
    {
        this.typesOnly = typesOnly;
    }

    /*
     * public String parseStatement(String stmt, List params) { if (stmt ==
     * null) { return stmt; } Matcher m = STATEMENT_ARGS.matcher(stmt);
     * StringBuffer sb = new StringBuffer(200); while (m.find()) { String key =
     * m.group(); m.appendReplacement(sb, "?"); params.add(key); }
     * m.appendTail(sb); return sb.toString(); }
     */

    public String parseStatement(final String stmt, final List params)
    {
        if (stmt == null)
        {
            return stmt;
        }
        final Matcher m = STATEMENT_ARGS.matcher(stmt);
        final StringBuffer sb = new StringBuffer(200);
        while (m.find())
        {
            String key = m.group();
            m.appendReplacement(sb, "?");
            // Special legacy handling for #[payload]
            if (key.equals("#[payload]"))
            {
                // MULE-3597
                logger
                        .warn("invalid expression template #[payload]. It should be replaced with #[payload:] to conform with the correct expression syntax. Mule has replaced this for you, but may not in future versions.");
                key = "#[payload:]";
            }
            params.add(key);
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public LDAPConnection getLdapConnection()
    {
        return ldapConnection;
    }

    protected final void setLdapConnection(final LDAPConnection ldapConnection)
    {
        this.ldapConnection = ldapConnection;
    }

    @Override
    protected final void doStart() throws MuleException
    {
        // TODO Auto-generated method stub

    }

    @Override
    protected final void doStop() throws MuleException
    {
        // TODO Auto-generated method stub

    }

    final void registerforEvent(final PSearchEventListener alistener)
            throws LDAPException
    {
        if (!enablePersistentSearch)
        {
            logger.debug("ps not enabled");

            return;
        }

        if ((psFilters == null) || (psFilters.size() == 0))
        {
            logger.debug("no ps filter");
            return;
        }

        final List attrs = getAttributes();
        String[] attributes = new String[0];

        if (attrs != null)
        {
            attributes = (String[]) attrs.toArray(new String[attrs.size()]);
        }

        for (final Iterator < String > iterator = psFilters.iterator(); iterator
                .hasNext();)
        {
            final String filter = iterator.next();

            source.registerforEvent(this.ldapConnection, this.searchBase,
                    this.searchScope, filter, attributes, this.typesOnly,
                    this.constraints, this.psEventchangetype,
                    this.psChangeonly, alistener);

            logger.debug("register listener for ps: " + filter);

        }

    }

    final void removeListener(final PSearchEventListener alistener)
            throws LDAPException
    {
        source.removeListener(alistener);
    }

    public void setSleepTime(final long l)
    {
        source.setSleepTime(l);
    }

    public long getSleepTime()
    {
        return source.getSleepTime();
    }

    public boolean isEnablePersistentSearch()
    {
        return enablePersistentSearch;
    }

    public void setEnablePersistentSearch(final boolean enablePersistentSearch)
    {
        this.enablePersistentSearch = enablePersistentSearch;
    }

    public boolean isPsChangeonly()
    {
        return psChangeonly;
    }

    public void setPsChangeonly(final boolean psChangeonly)
    {
        this.psChangeonly = psChangeonly;
    }

    public int getPsEventchangetype()
    {
        return psEventchangetype;
    }

    public void setPsEventchangetype(final int psEventchangetype)
    {
        this.psEventchangetype = psEventchangetype;
    }

    public List < String > getPsFilters()
    {
        return psFilters;
    }

    public void setPsFilters(final List < String > psFilters)
    {
        this.psFilters = psFilters;
    }

    public LDAPSearchConstraints getConstraints()
    {
        return constraints;
    }

    // only for referrals
    public LDAPConnection bind(final String[] ldapurl, final LDAPConnection conn)
            throws LDAPReferralException
    {
        throw new LDAPReferralException("Not implemented yet");
    }

    // only for referrals
    public LDAPAuthProvider getAuthProvider(final String host, final int port)
    {
        try
        {
            // TODO Auto-generated method stub
            return new LDAPAuthProvider(this.loginDN, password.getBytes("UTF8"));
        }
        catch (final UnsupportedEncodingException e)
        {
            return null;
        }
    }

    public boolean isDoReferrals()
    {
        return doReferrals;
    }

    public void setDoReferrals(final boolean doReferrals)
    {
        this.doReferrals = doReferrals;
    }

}