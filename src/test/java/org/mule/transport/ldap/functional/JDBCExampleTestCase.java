package org.mule.transport.ldap.functional;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.mule.module.client.MuleClient;
import org.mule.tck.TestCaseWatchdog;
import org.mule.transport.jdbc.JdbcConnector;
import org.mule.transport.jdbc.util.MuleDerbyUtils;
import org.mule.transport.ldap.util.TestHelper;

public class JDBCExampleTestCase extends AbstractLdapFunctionalTestCase
{

    @Override
    protected String getConfigResources()
    {
        super.getConfigResources();
        return "ldap-example-jdbc.xml";
    }

    public void testAdd() throws Exception
    {
        createTable();

        final MuleClient client = new MuleClient();

        final int addCount = 4;

        for (int i = 0; i < addCount; i++)
        {
            client.send("ldap://ldap.out", TestHelper
                    .getRandomEntryAddRequest(), null);
        }

        Thread.sleep(5000);

        final MyResultSetHandler h = new MyResultSetHandler();

        final JdbcConnector jdbcConnector = (JdbcConnector) muleContext
                .getRegistry().lookupConnector("jdbcConnector");
        final QueryRunner qr = jdbcConnector.getQueryRunner();
        qr.query(jdbcConnector.getConnection(), "SELECT COUNT(*) FROM TEST", h);

        assertTrue("expected: " + addCount + ",was: " + h.getSqlcount(), h
                .getSqlcount() > 1);

    }

    protected void createTable() throws Exception
    {
        final JdbcConnector jdbcConnector = (JdbcConnector) muleContext
                .getRegistry().lookupConnector("jdbcConnector");
        final QueryRunner qr = jdbcConnector.getQueryRunner();
        qr
                .update(
                        jdbcConnector.getConnection(),
                        "CREATE TABLE TEST(ID INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 0)  NOT NULL PRIMARY KEY,TYPE INTEGER,DATA VARCHAR(255))");
        logger.debug("Table created");
    }

    @Override
    protected void suitePreSetUp() throws Exception
    {
        MuleDerbyUtils.defaultDerbyCleanAndInit("derby.properties",
                "database.name");
        super.suitePreSetUp();
    }

    @Override
    protected TestCaseWatchdog createWatchdog()
    {
        // TODO Auto-generated method stub
        return new TestCaseWatchdog(
                10,
                edu.emory.mathcs.backport.java.util.concurrent.TimeUnit.MINUTES,
                this);
    }

    private static class MyResultSetHandler implements ResultSetHandler
    {

        int sqlcount = -1;

        public Object handle(final ResultSet arg0) throws SQLException
        {
            arg0.next();
            sqlcount = arg0.getInt(1);
            return null;
        }

        public int getSqlcount()
        {
            return sqlcount;
        }

    };

}
