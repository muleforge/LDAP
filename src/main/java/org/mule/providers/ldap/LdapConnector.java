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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mule.config.i18n.CoreMessages;
import org.mule.providers.AbstractConnector;
import org.mule.providers.jdbc.NowPropertyExtractor;
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
import com.novell.ldap.LDAPJSSESecureSocketFactory;
import com.novell.ldap.LDAPSearchResults;
import com.novell.ldap.LDAPUnsolicitedNotificationListener;

/**
 * <code>LdapConnector</code> TODO document
 */
public class LdapConnector extends AbstractConnector {

	public LdapConnector() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public LdapConnector(boolean sslConnection) {
		super();
		ssl=true;
		this.ldapPort = LDAPConnection.DEFAULT_SSL_PORT;
		
	}

	public static final String PROPERTY_LDAP_HOST = "ldapHost";
	public static final String PROPERTY_LDAP_PORT = "ldapPort";
	public static final String PROPERTY_SEARCH_SCOPE = "searchScope";
	public static final String PROPERTY_LOGIN_DN = "loginDN";
	public static final String PROPERTY_PASSWORD = "password";
	public static final String PROPERTY_SEARCH_BASE = "searchBase";
	public static final String PROPERTY_POLLING_FREQUENCY = "pollingFrequency";
	public static final String PROPERTY_START_UNSOLICITED_NOTIFICATION_LISTENER = "startUnsolicitedNotificationListener";
	private static final Pattern STATEMENT_ARGS = Pattern
			.compile("\\$\\{[^\\}]*\\}");

	public static final long DEFAULT_POLLING_FREQUENCY = 1000;

	protected boolean ssl= false; 
	
	protected LDAPJSSESecureSocketFactory ssf;
	
	protected long pollingFrequency;

	protected int ldapPort = LDAPConnection.DEFAULT_PORT;

	protected int searchScope = LDAPConnection.SCOPE_SUB;

	protected final int ldapVersion = LDAPConnection.LDAP_V3;

	protected String ldapHost = null;

	protected String loginDN = null;

	protected String password = null;

	protected String searchBase = "";

	protected boolean startUnsolicitedNotificationListener = false;

	protected LDAPConnection lc = null;

	protected Map queries;

	protected Set propertyExtractors;

	protected Set queryValueExtractors;

	protected void doInitialise() throws InitialisationException {

		try {

			// setup property Extractors for queries
			if (queryValueExtractors == null) {
				// Add defaults
				queryValueExtractors = new HashSet();
				queryValueExtractors.add(MessagePropertyExtractor.class
						.getName());
				queryValueExtractors.add(NowPropertyExtractor.class.getName());
				queryValueExtractors.add(PayloadPropertyExtractor.class
						.getName());
				queryValueExtractors.add(MapPropertyExtractor.class.getName());
				queryValueExtractors.add(BeanPropertyExtractor.class.getName());

				if (ClassUtils.isClassOnPath(
						"org.mule.util.properties.Dom4jPropertyExtractor",
						getClass())) {
					queryValueExtractors
							.add("org.mule.util.properties.Dom4jPropertyExtractor");
				}

				if (ClassUtils.isClassOnPath(
						"org.mule.util.properties.JDomPropertyExtractor",
						getClass())) {
					queryValueExtractors
							.add("org.mule.util.properties.JDomPropertyExtractor");
				}
			}

			propertyExtractors = new HashSet();
			for (Iterator iterator = queryValueExtractors.iterator(); iterator
					.hasNext();) {
				String s = (String) iterator.next();
				propertyExtractors.add(ClassUtils.instanciateClass(s,
						ClassUtils.NO_ARGS));
			}
		} catch (Exception e) {
			throw new InitialisationException(CoreMessages
					.failedToCreate("Jdbc Connector"), e, this);
		}
	}

	protected void ensureConnected() throws Exception {
		if (!lc.isConnected() || !lc.isConnectionAlive())
		{
			logger.warn("not connected, trying reconnect ...");
			reConnect();
		}
		// TODO
		// reconnection strategiy

		if (!lc.isConnected() || !lc.isConnectionAlive())
			throw new Exception("Unable to reconnect");

	}

	protected void reConnect() throws Exception {
		doDisconnect();
		doConnect();
	}
	
	

	public void doConnect() throws Exception {

		
		logger.debug("connection stategie: "
				+ this.getConnectionStrategy().getClass());
		
		logger.debug("try connect to " + ldapHost + ":" + ldapPort+" ...");

		if (StringUtils.isEmpty(ldapHost))
			throw new IllegalArgumentException("ldapHost must not be empty");
		if (password == null)
			password = "";

		if(ssl)
			lc = new LDAPConnection(ssf);
		else
			lc = new LDAPConnection();
		
		lc.connect(ldapHost, ldapPort);
		
		logger.debug("connected to " + ldapHost + ":" + ldapPort);

		if (StringUtils.isEmpty(loginDN) && lc.isBound()) {
			logger.debug("anonymous bind to "+ldapHost+" successful");
		} else if (!StringUtils.isEmpty(loginDN)) {
			lc.bind(ldapVersion, loginDN, password.getBytes("UTF8"));
			logger.debug("non-anonymous bind of " + loginDN + " successful");
		} else {
			throw new Exception("Unable to bind anonymous");
		}
		// TODO
		// UrlSearch.java
		// async
	}

	protected void addLDAPListener(LDAPUnsolicitedNotificationListener listener) {
		if (listener != null && isStartUnsolicitedNotificationListener()) {
			lc.addUnsolicitedNotificationListener(listener);
			logger.debug(listener
					+ " registered as UnsolicitedNotificationListener");
		}
	}

	public void doDisconnect() throws Exception {

		logger.debug("doDisconnect()");
		
		if (lc != null)
			try {
				lc.disconnect();
			} catch (LDAPException e) {
				// ignored
			}

	}

	public String getProtocol() {
		return "ldap";
	}

	protected LDAPSearchResults doSearch(String searchFilter) throws Exception {

		ensureConnected();

		return lc.search(searchBase,

		searchScope,

		searchFilter,

		// TODO
				// make configurable

				null, // return all attributes

				false); // return attrs and values

	}

	protected List<LDAPEntry> extractEntriesFromResults(
			LDAPSearchResults searchResults) {

		List<LDAPEntry> resultList = new ArrayList<LDAPEntry>();

		while (searchResults.hasMore()) {

			LDAPEntry nextEntry = null;

			try {

				nextEntry = searchResults.next();
				resultList.add(nextEntry);

			}

			catch (LDAPException e) {

				logger.error("Unable to get entry", e);

				// Exception is thrown, go for next entry

				if (e.getResultCode() == LDAPException.LDAP_TIMEOUT
						|| e.getResultCode() == LDAPException.CONNECT_ERROR)

					break;

				else

					continue;

			}

		}

		return resultList;
	}

	public int getLdapPort() {
		return ldapPort;
	}

	public void setLdapPort(int ldapPort) {
		this.ldapPort = ldapPort;
	}

	public int getSearchScope() {
		return searchScope;
	}

	public void setSearchScope(int searchScope) {
		this.searchScope = searchScope;
	}

	public int getLdapVersion() {
		return ldapVersion;
	}

	public String getLdapHost() {
		return ldapHost;
	}

	public void setLdapHost(String ldapHost) {
		this.ldapHost = ldapHost;
	}

	public String getLoginDN() {
		return loginDN;
	}

	public void setLoginDN(String loginDN) {
		this.loginDN = loginDN;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSearchBase() {
		return searchBase;
	}

	public void setSearchBase(String searchBase) {
		this.searchBase = searchBase;
	}

	public long getPollingFrequency() {
		return pollingFrequency;
	}

	public void setPollingFrequency(long pollingFrequency) {
		this.pollingFrequency = pollingFrequency;
	}

	@Override
	protected UMOMessageReceiver createReceiver(UMOComponent component,
			UMOEndpoint endpoint) throws Exception {

		long polling = pollingFrequency;
		Map props = endpoint.getProperties();
		if (props != null) {
			// Override properties on the endpoint for the specific endpoint
			String tempPolling = (String) props.get(PROPERTY_POLLING_FREQUENCY);
			if (tempPolling != null) {
				polling = Long.parseLong(tempPolling);
			}
		}
		if (polling <= 0) {
			polling = DEFAULT_POLLING_FREQUENCY;
		}
		logger.debug("set polling frequency to " + polling);

		try {

			return serviceDescriptor.createMessageReceiver(this, component,
					endpoint, new Object[] { new Long(polling) });

		} catch (Exception e) {
			throw new InitialisationException(CoreMessages
					.failedToCreateObjectWith("Message Receiver",
							serviceDescriptor.getMessageReceiver()), e, this);
		}
	}

	public boolean isStartUnsolicitedNotificationListener() {
		return startUnsolicitedNotificationListener;
	}

	public void setStartUnsolicitedNotificationListener(
			boolean startUnsolicitedNotificationListener) {
		this.startUnsolicitedNotificationListener = startUnsolicitedNotificationListener;
	}

	public String getQuery(UMOImmutableEndpoint endpoint, String stmt) {
		Object query = null;
		if (endpoint != null && endpoint.getProperties() != null) {
			Object queries = endpoint.getProperties().get("queries");
			if (queries instanceof Map) {
				query = ((Map) queries).get(stmt);
			}
		}
		if (query == null) {
			if (this.queries != null) {
				query = this.queries.get(stmt);
			}
		}
		return query == null ? null : query.toString();
	}

	/**
	 * Parse the given statement filling the parameter list and return the ready
	 * to use statement.
	 * 
	 * @param stmt
	 * @param params
	 * @return
	 */
	public String parseStatement(String stmt, List params) {
		if (stmt == null) {
			return stmt;
		}
		Matcher m = STATEMENT_ARGS.matcher(stmt);
		StringBuffer sb = new StringBuffer(200);
		while (m.find()) {
			String key = m.group();
			m.appendReplacement(sb, "?");
			params.add(key);
		}
		m.appendTail(sb);
		return sb.toString();
	}

	public String parseStatement2(String stmt, Object[] values) {
		if (stmt == null) {
			return stmt;
		}
		Matcher m = STATEMENT_ARGS.matcher(stmt);
		StringBuffer sb = new StringBuffer(200);
		int i = 0;
		while (m.find()) {
			
			m.appendReplacement(sb, values[i] == null ? "null" : values[i]
					.toString());
			i++;
		}
		m.appendTail(sb);
		return sb.toString();
	}

	public Object[] getParams(UMOImmutableEndpoint endpoint, List paramNames,
			Object message) throws Exception {
		Object[] params = new Object[paramNames.size()];
		for (int i = 0; i < paramNames.size(); i++) {
			String param = (String) paramNames.get(i);
			String name = param.substring(2, param.length() - 1);
			Object value = null;
			// If we find a value and it happens to be null, thats acceptable
			boolean foundValue = false;
			if (message != null) {
				for (Iterator iterator = propertyExtractors.iterator(); iterator
						.hasNext();) {
					PropertyExtractor pe = (PropertyExtractor) iterator.next();
					value = pe.getProperty(name, message);
					if (value != null) {
						if (value.equals(StringUtils.EMPTY)
								&& pe instanceof BeanPropertyExtractor) {
							value = null;
						}
						foundValue = true;
						break;
					}
				}
			}
			if (!foundValue) {
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
	protected void doDispose() {
		// do nothing
		logger.debug("doDispose()");

	}

	@Override
	protected void doStart() throws UMOException {
		// do nothing
		logger.debug("doStart()");
	}

	@Override
	protected void doStop() throws UMOException {
		// do nothing
		logger.debug("doStart()");
	}

	protected LDAPJSSESecureSocketFactory getSsf() {
		return ssf;
	}

	protected void setSsf(LDAPJSSESecureSocketFactory ssf) {
		this.ssf = ssf;
	}
}
