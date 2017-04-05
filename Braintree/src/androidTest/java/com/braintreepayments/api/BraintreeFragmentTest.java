package com.braintreepayments.api;

import android.app.Activity;
import android.support.test.runner.AndroidJUnit4;

import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.interfaces.BraintreeErrorListener;
import com.braintreepayments.api.interfaces.BraintreeResponseListener;
import com.braintreepayments.api.interfaces.ConfigurationListener;
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener;
import com.braintreepayments.api.models.CardBuilder;
import com.braintreepayments.api.models.Configuration;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.braintreepayments.api.test.BraintreeActivityTestRule;
import com.braintreepayments.api.test.TestActivity;
import com.braintreepayments.api.test.TestClientTokenBuilder;
import com.braintreepayments.testutils.TestClientTokenKey;
import com.google.android.gms.common.api.GoogleApiClient;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

import static com.braintreepayments.api.BraintreeFragmentTestUtils.getFragmentWithAuthorization;
import static com.braintreepayments.testutils.TestTokenizationKey.TOKENIZATION_KEY;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class BraintreeFragmentTest {

    @Rule
    public final BraintreeActivityTestRule<TestActivity> mActivityTestRule =
            new BraintreeActivityTestRule<>(TestActivity.class);

    private Activity mActivity;
    private String mClientToken;
    private CountDownLatch mCountDownLatch;

    @Before
    public void setUp() {
        mActivity = mActivityTestRule.getActivity();
        mClientToken = ""; //new TestClientTokenBuilder().build();
        mCountDownLatch = new CountDownLatch(1);
    }

    @Test(timeout = 10000)
    public void fetchConfiguration_worksWithATokenizationKey() throws InterruptedException {
        final BraintreeFragment fragment = getFragmentWithAuthorization(mActivity, TOKENIZATION_KEY);
        fragment.waitForConfiguration(new ConfigurationListener() {
            @Override
            public void onConfigurationFetched(Configuration configuration) {
                assertNotNull(configuration);
                mCountDownLatch.countDown();
            }
        });

        mCountDownLatch.await();
    }

    @Test(timeout = 10000)
    public void fetchConfiguration_worksWithAClientToken() throws InterruptedException {
        final BraintreeFragment fragment = getFragmentWithAuthorization(mActivity, mClientToken);
        fragment.waitForConfiguration(new ConfigurationListener() {
            @Override
            public void onConfigurationFetched(Configuration configuration) {
                assertNotNull(configuration);
                mCountDownLatch.countDown();
            }
        });

        mCountDownLatch.await();
    }

    @Test(timeout = 1000)
    public void newInstance_returnsSingleton() throws InvalidArgumentException, InterruptedException {
        final BraintreeFragment[] fragments = new BraintreeFragment[2];
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    fragments[0] = BraintreeFragment.newInstance(mActivity, TOKENIZATION_KEY);
                    fragments[1] = BraintreeFragment.newInstance(mActivity, TOKENIZATION_KEY);
                } catch (InvalidArgumentException e) {
                    fail(e.getMessage());
                }

                mCountDownLatch.countDown();
            }
        });

        mCountDownLatch.await();
        assertNotNull(fragments[0]);
        assertNotNull(fragments[1]);
        assertEquals(fragments[0], fragments[1]);
    }

    @Test(timeout = 1000)
    public void getGoogleApiClient_returnsGoogleApiClient() throws InterruptedException {
        BraintreeFragment fragment = getFragmentWithAuthorization(mActivity, mClientToken);

        fragment.getGoogleApiClient(new BraintreeResponseListener<GoogleApiClient>() {
            @Override
            public void onResponse(GoogleApiClient googleApiClient) {
                assertNotNull(googleApiClient);
                mCountDownLatch.countDown();
            }
        });

        mCountDownLatch.await();
    }

    @Test(timeout = 5000)
    public void fetchConfiguration_worksWithVersionFromClientToken() throws InterruptedException {
        mClientToken = TestClientTokenKey.CLIENT_TOKEN_WITH_VERSIONED_CONFIG;
        final BraintreeFragment fragment = getFragmentWithAuthorization(mActivity, mClientToken);
        fragment.waitForConfiguration(new ConfigurationListener() {
            @Override
            public void onConfigurationFetched(Configuration configuration) {
                assertNotNull(configuration);
                mCountDownLatch.countDown();
            }
        });

        mCountDownLatch.await();
    }
}
