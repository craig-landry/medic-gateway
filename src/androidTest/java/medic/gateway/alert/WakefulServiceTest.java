package medic.gateway.alert;

import android.test.AndroidTestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import android.content.Intent;
import com.commonsware.cwac.wakeful.WakefulIntentService;

import medic.gateway.alert.test.DbTestHelper;
import medic.gateway.alert.test.HttpTestHelper;

import static medic.gateway.alert.test.DbTestHelper.cols;
import static medic.gateway.alert.test.DbTestHelper.vals;
import static medic.gateway.alert.test.TestUtils.ANY_NUMBER;
import static medic.gateway.alert.test.TestUtils.A_PHONE_NUMBER;
import static medic.gateway.alert.test.TestUtils.SOME_CONTENT;

@SuppressWarnings({"PMD.SignatureDeclareThrowsException", "PMD.JUnitTestsShouldIncludeAssert"})
public class WakefulServiceTest extends AndroidTestCase {
	private DbTestHelper db;
	private HttpTestHelper http;
	@Before
	public void setUp() throws Exception {
		super.setUp();

		db = new DbTestHelper(getContext());
		http = new HttpTestHelper();
		http.configureAppSettings(getContext());
	}

	@After
	public void tearDown() throws Exception {
		super.tearDown();

		http.tearDown();
		db.tearDown();

		// http.assertNoMoreRequests();

	}

	@Test
	public void test_doWakefulWork_unsentMessagesShouldSendMessages() throws Exception {
		// given
		db.insert("wt_message",
				cols("_id",        "status",                 "last_action", "_from",        "content",    "sms_sent", "sms_received"),
				vals("message-0001", WtMessage.Status.WAITING, 0,             A_PHONE_NUMBER, SOME_CONTENT, 0,          0),
				vals("message-0002", WtMessage.Status.WAITING, 0,             A_PHONE_NUMBER, SOME_CONTENT, 0,          0),
				vals("message-0003", WtMessage.Status.WAITING, 0,             A_PHONE_NUMBER, SOME_CONTENT, 0,          0));
		http.nextResponseJson("{}");

		// when
		Intent i = new Intent(getContext(), WakefulIntentService.class);
		WakefulService wfs = new WakefulService(getContext());
		wfs.doWakefulWork(i);

		//then
		db.assertTable("wt_message",
				"message-0001", "WAITING", ANY_NUMBER, A_PHONE_NUMBER, SOME_CONTENT, ANY_NUMBER, ANY_NUMBER,
				"message-0002", "WAITING", ANY_NUMBER, A_PHONE_NUMBER, SOME_CONTENT, ANY_NUMBER, ANY_NUMBER,
				"message-0003", "WAITING", ANY_NUMBER, A_PHONE_NUMBER, SOME_CONTENT, ANY_NUMBER, ANY_NUMBER);
	}

	// @Test
	// public void test_doWakefulWork_unsentMessagesShouldSendMultipleBatches() throws Exception {
	// 	// given
	// 	db.insert("wt_message",
	// 			cols("_id",        "status",                 "last_action", "_from",        "content",    "sms_sent", "sms_received"),
	// 			vals(randomUuid(), WtMessage.Status.WAITING, 0,             A_PHONE_NUMBER, SOME_CONTENT, 0,          0),
	// 			vals(randomUuid(), WtMessage.Status.WAITING, 0,             A_PHONE_NUMBER, SOME_CONTENT, 0,          0),
	// 			vals(randomUuid(), WtMessage.Status.WAITING, 0,             A_PHONE_NUMBER, SOME_CONTENT, 0,          0),
	// 			vals(randomUuid(), WtMessage.Status.WAITING, 0,             A_PHONE_NUMBER, SOME_CONTENT, 0,          0),
	// 			vals(randomUuid(), WtMessage.Status.WAITING, 0,             A_PHONE_NUMBER, SOME_CONTENT, 0,          0),
	// 			vals(randomUuid(), WtMessage.Status.WAITING, 0,             A_PHONE_NUMBER, SOME_CONTENT, 0,          0),
	// 			vals(randomUuid(), WtMessage.Status.WAITING, 0,             A_PHONE_NUMBER, SOME_CONTENT, 0,          0),
	// 			vals(randomUuid(), WtMessage.Status.WAITING, 0,             A_PHONE_NUMBER, SOME_CONTENT, 0,          0),
	// 			vals(randomUuid(), WtMessage.Status.WAITING, 0,             A_PHONE_NUMBER, SOME_CONTENT, 0,          0),
	// 			vals(randomUuid(), WtMessage.Status.WAITING, 0,             A_PHONE_NUMBER, SOME_CONTENT, 0,          0),
	// 			vals(randomUuid(), WtMessage.Status.WAITING, 0,             A_PHONE_NUMBER, SOME_CONTENT, 0,          0),
	// 			vals(randomUuid(), WtMessage.Status.WAITING, 0,             A_PHONE_NUMBER, SOME_CONTENT, 0,          0),
	// 			vals(randomUuid(), WtMessage.Status.WAITING, 0,             A_PHONE_NUMBER, SOME_CONTENT, 0,          0),
	// 			vals(randomUuid(), WtMessage.Status.WAITING, 0,             A_PHONE_NUMBER, SOME_CONTENT, 0,          0),
	// 			vals(randomUuid(), WtMessage.Status.WAITING, 0,             A_PHONE_NUMBER, SOME_CONTENT, 0,          0),
	// 			vals(randomUuid(), WtMessage.Status.WAITING, 0,             A_PHONE_NUMBER, SOME_CONTENT, 0,          0));
	// 	http.nextResponseJson("{}");
	// 	http.nextResponseJson("{}");

	// 	// when
	// 	Intent i = new Intent(getContext(), WakefulIntentService.class);
	// 	WakefulService wfs = new WakefulService(getContext());
	// 	wfs.doWakefulWork(i);

	// 	//then
	// 	db.assertEmpty("wt_message");
	// }
}
