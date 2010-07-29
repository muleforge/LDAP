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
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mule.api.MuleContext;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.config.i18n.CoreMessages;
import org.mule.transport.AbstractConnector;
import org.mule.transport.ConnectException;
import org.mule.transport.ldap.util.EndpointURIExpressionEvaluator;

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
import com.novell.ldap.LDAPUrl;
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

    private final List < String > attributes = new ArrayList < String >();

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

    private Map < String, String > queries = null;

    private LDAPSearchConstraints constraints = null;

    // referrals
    private boolean doReferrals = false;

    // ps
    private final PsearchEventSource source = new PsearchEventSource();
    private boolean enablePersistentSearch = false;
    private final List < String > psFilters = new ArrayList < String >();
    private boolean psChangeonly = true;
    private int psEventchangetype = EventConstant.LDAP_PSEARCH_ANY;

    public LdapConnector (MuleContext context)
    {
        super(context);
    }

    @Override
    protected void doInitialise() throws InitialisationException
    {

        if (!muleContext.getExpressionManager().isEvaluatorRegistered(
                EndpointURIExpressionEvaluator.NAME))
        {
            muleContext.getExpressionManager().registerEvaluator(
                    new EndpointURIExpressionEvaluator());
        }

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
       
    }

    protected final void ensureConnected() throws ConnectException
    {


        if (ldapConnection != null)
        {
            logger.debug("connected?:" + ldapConnection.isConnected());
            
        }

        if ((ldapConnection == null) || !ldapConnection.isConnected()
                || !ldapConnection.isConnectionAlive())
        {
            logger.warn("ensureConnected() failed, try to reconnect");
            
            try
            {
                this.doConnect();
            }
            catch (Exception e)
            {
                throw (new ConnectException(
                        CoreMessages.connectorCausedError(this), e, this));
            }
            
            
            
        }

    }

    protected LDAPConnection createLDAPConnection()
    {
        return new LDAPConnection();
    }

    protected void doBind(LDAPConnection lc) throws Exception
    {

        lc.bind(ldapVersion, loginDN, password.getBytes("UTF8"));
    }

    protected boolean isAnonymousBindSupported()
    {
        return true;
    }

    @Override
    public final void doConnect() throws Exception
    {
        
        logger.debug("try connect to " + ldapHost + ":" + ldapPort + " with retry policy template "+getRetryPolicyTemplate());

        if (org.apache.commons.lang.StringUtils.isEmpty(ldapHost))
        {
            throw new IllegalArgumentException("ldapHost must not be empty");
        }

        if (password == null)
        {
            password = "";
        }

        ldapConnection = createLDAPConnection();

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
            doBind(ldapConnection);
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
            final List paramNames, final MuleMessage message, final String query)
            throws Exception
    {

        final Object[] params = new Object[paramNames.size()];
        for (int i = 0; i < paramNames.size(); i++)
        {
            final String param = (String) paramNames.get(i);
            Object value = null;
            // If we find a value and it happens to be null, thats acceptable
            boolean foundValue = false;
            boolean validExpression = muleContext.getExpressionManager()
                    .isValidExpression(param);
            // There must be an expression namespace to use the
            // ExpresionEvaluator i.e. header:type
            if ((message != null) && validExpression)
            {
                value = muleContext.getExpressionManager().evaluate(param,
                        message);
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

    public void setAttributes(final String attributes)
    {

        this.attributes.addAll(Arrays.asList(attributes.split(",")));

    }

    public String getAttributes()
    {
        return attributes.toString();
    }

    public String[] getAttributesAsArray()
    {

        return attributes.toArray(new String[attributes.size()]);
    }

    public String[] getpsFiltersAsArray()
    {

        return psFilters.toArray(new String[psFilters.size()]);
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
        // empty

    }

    @Override
    protected final void doStop() throws MuleException
    {
     // empty

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

        for (final Iterator < String > iterator = psFilters.iterator(); iterator
                .hasNext();)
        {
            final String filter = iterator.next();

            source.registerforEvent(this.ldapConnection, this.searchBase,
                    this.searchScope, filter, getAttributesAsArray(),
                    this.typesOnly, this.constraints, this.psEventchangetype,
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

    public String getPsFilters()
    {
        return psFilters.toString();
    }

    public void setPsFilters(final String psFilters)
    {
        this.psFilters.addAll(Arrays.asList(psFilters.split(",")));
    }

    public LDAPSearchConstraints getConstraints()
    {
        return constraints;
    }

    // only for referrals
    public LDAPConnection bind(final String[] ldapurl, final LDAPConnection conn)
            throws LDAPReferralException
    {
        
        if(ldapurl == null || ldapurl.length == 0)
        {        
            throw new LDAPReferralException("Not referral URLs given ("+ldapurl+")");
        }
        
        if(conn != this.ldapConnection)
        {        
            throw new LDAPReferralException("LDAPConnection mismatch");
        }
        
        //FIXME iterate
        String urlS = ldapurl[0];
        
        logger.debug("referral bind requested, try to connect and bind to "+urlS);
        
        
        LDAPConnection referralCon = createLDAPConnection();
        
        LDAPUrl url = null;
        try
        {
            url = new LDAPUrl(urlS);
        }
        catch (MalformedURLException e)
        {
            throw new LDAPReferralException("Invalid LDAP Url "+urlS,e);            
        }
        
        try
        {
            referralCon.connect(url.getHost(), url.getPort());
            logger.debug("connect to " + url.getHost() + " successful (as referral)");
        }
        catch (LDAPException e)
        {
            throw new LDAPReferralException("Unable to connect ldap server "+urlS,e);   
        }

        constraints = new LDAPSearchConstraints(this.timeLimit * 1000, // client
                // timeout,
                // ms
                this.timeLimit, // serverTimeLimit sec
                this.dereference, this.maxResults, doReferrals,// boolean
                // doReferrals
                1,// batchsize
                this, 10); // int hop_limit

        referralCon.setConstraints(constraints);

        logger.debug("connected to " + ldapHost + ":" + ldapPort+" as referral");

        // lc.isBound()
        // note: an anonymous bind returns false - not bound
        // but do not work correct

        if (isAnonymousBindSupported()
                && org.apache.commons.lang.StringUtils.isEmpty(loginDN)
                && isAnonBind())
        {
            logger.debug("anonymous bind to " + urlS + " successful");
        }
        else if (!org.apache.commons.lang.StringUtils.isEmpty(loginDN))
        {
            try
            {
                doBind(referralCon);
                logger.debug("non-anonymous bind of " + loginDN + " successful (as referral)");
            }
            catch (Exception e)
            {
                throw new LDAPReferralException("Unable to bind as '"+loginDN+"' to ldap server "+urlS,e);   
            }
            
            
        }
        else
        {
            throw new LDAPReferralException(
                    "Unable to bind anonymous (either failed or not supported (SSL/SASL)");
        }

        return referralCon;
        
     }

    // only for referrals
    public LDAPAuthProvider getAuthProvider(final String host, final int port)
    {
        
        logger.debug("referral authentication requested for ldap server "+host+":"+port);
        
        try
        {
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
