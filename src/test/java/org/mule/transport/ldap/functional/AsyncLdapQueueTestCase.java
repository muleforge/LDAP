package org.mule.transport.ldap.functional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.transport.ldap.AbstractLdapDSTestCase;
import org.mule.transport.ldap.util.LDAPUtils;
import org.mule.transport.ldap.util.TestHelper;

import com.novell.ldap.LDAPAddRequest;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPDeleteRequest;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPMessage;
import com.novell.ldap.LDAPMessageQueue;
import com.novell.ldap.LDAPResponse;
import com.novell.ldap.LDAPSearchConstraints;
import com.novell.ldap.LDAPSearchRequest;
import com.novell.ldap.LDAPSearchResult;

public class AsyncLdapQueueTestCase extends AbstractLdapDSTestCase
{

    /**
     * logger used by this class
     */
    protected final Log logger = LogFactory.getLog(getClass());

    public AsyncLdapQueueTestCase()
    {
        super(true);

    }

    public void testQueue() throws Exception
    {

        final LDAPConnection lc = new LDAPConnection();
        lc.connect("localhost", 10389);

        final LDAPMessage msg = TestHelper.getRandomEntryAddRequest();
        final int id = msg.getMessageID();
        msg.setTag("test_tag");

        final LDAPMessageQueue queue = lc.sendRequest(msg, null);

        final LDAPMessage res = queue.getResponse();

        assertNotNull(res);
        assertEquals(res.getType(), LDAPMessage.ADD_RESPONSE);
        assertTrue(res instanceof LDAPResponse);
        assertEquals(((LDAPResponse) res).getResultCode(),
                LDAPException.SUCCESS);
        assertEquals(res.getTag(), "test_tag");
        assertEquals(res.getMessageID(), id);

    }

    public void testQueueAddOrder() throws Exception
    {

        final int count = 5; // TODO ungerade arrayindexoutofbounds

        class QueueListener implements Runnable
        {

            LDAPMessageQueue queue = null;

            public QueueListener(final LDAPMessageQueue queue)
            {
                super();
                this.queue = queue;
            }

            public synchronized void run()
            {
                LDAPMessage msg = null;
                int i = 0;

                try
                {
                    while (queue.getMessageIDs().length > 0)
                    {

                        try
                        {
                            logger.debug("resp: " + queue.isResponseReceived());
                        }
                        catch (final RuntimeException e1)
                        {
                            logger.debug("error response received");

                        }
                        logger.debug("size: " + queue.getMessageIDs().length);

                        try
                        {
                            Thread.sleep(700);

                            msg = queue.getResponse();

                            if (msg == null)
                            {
                                continue;
                            }

                            if (msg instanceof LDAPResponse)
                            {
                                final LDAPResponse response = (LDAPResponse) msg;
                                logger.debug(msg.getClass().getName()
                                        + "//Type: "
                                        + LDAPUtils
                                                .evaluateMessageType(response)
                                        + "//Id: " + response.getMessageID()
                                        + "//Tag: " + response.getTag()
                                        + "//MDN: " + response.getMatchedDN());
                            }
                            else if (msg instanceof LDAPSearchResult)
                            {
                                final LDAPSearchResult response = (LDAPSearchResult) msg;
                                logger.debug(" --> "
                                        + msg.getClass().getName()
                                        + "//Type: "
                                        + LDAPUtils
                                                .evaluateMessageType(response)
                                        + "//Id: " + response.getMessageID()
                                        + "//Tag: " + response.getTag()
                                        + "//found: "
                                        + response.getEntry().getDN());
                            }
                            else
                            {
                                fail();
                            }

                            i++;

                        }
                        catch (final Exception e)
                        {
                            e.printStackTrace();
                            fail(e.toString());

                        }
                    }// end while
                }
                catch (final Exception e)
                {
                    e.printStackTrace();
                    fail(e.toString());

                }

                assertNotNull(msg);
                assertTrue(msg instanceof LDAPResponse);
                assertTrue(i >= 2 * count);

            }// end run()

        }// end-class

        final LDAPConnection lc = new LDAPConnection();
        lc.connect("localhost", 10389);

        LDAPAddRequest msg = null;
        LDAPMessageQueue queue = null;

        // Map map = new HashMap();
        QueueListener listener = null;

        Thread queueListenerThread = null;

        for (int i = 0; i < count; i++)
        {
            msg = TestHelper.getRandomEntryAddRequest();
            // final int id = msg.getMessageID();
            msg.setTag("test_tag_(" + msg.getEntry().getDN() + ")_" + i);
            // map.put(msg.getTag(), new Integer(id));
            queue = lc.sendRequest(msg, queue);
            // logger.debug("send "+msg.getMessageID());

            Thread.sleep(200);

            if (listener == null)
            {
                listener = new QueueListener(queue);
                queueListenerThread = new Thread(listener);
                queueListenerThread.start();
            }

            if (i % 5 == 0)
            {
                Thread.yield();
            }

            if (i % 2 == 0)
            {
                final LDAPDeleteRequest delreq = new LDAPDeleteRequest(msg
                        .getEntry().getDN(), null);
                delreq.setTag("delete " + i);
                queue = lc.sendRequest(delreq, queue);
                // logger.debug("send "+delreq.getMessageID());
            }

            Thread.sleep(300);

            final LDAPSearchRequest sreq = new LDAPSearchRequest("o=sevenseas",
                    2, "(cn=*)", null, LDAPSearchConstraints.DEREF_NEVER,
                    Integer.MAX_VALUE, 0, false, null);
            sreq.setTag("search while " + i);
            queue = lc.sendRequest(sreq, queue);
            // logger.debug("send "+sreq.getMessageID());

        }

        queueListenerThread.join();

    }
}
