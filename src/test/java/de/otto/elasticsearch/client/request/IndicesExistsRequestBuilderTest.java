package de.otto.elasticsearch.client.request;

import com.google.common.collect.ImmutableList;
import com.ning.http.client.AsyncHttpClient;
import de.otto.elasticsearch.client.CompletedFuture;
import de.otto.elasticsearch.client.MockResponse;
import de.otto.elasticsearch.client.response.HttpServerErrorException;
import de.otto.elasticsearch.client.util.RoundRobinLoadBalancingHttpClient;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class IndicesExistsRequestBuilderTest {

    public static final String INDEX_NAME = "someIndexName";

    IndicesExistsRequestBuilder testee;

    @Mock
    RoundRobinLoadBalancingHttpClient httpClient;

    @Mock
    AsyncHttpClient.BoundRequestBuilder boundRequestBuilder;

    @BeforeMethod
    public void setUp() throws Exception {
        initMocks(this);
        testee = new IndicesExistsRequestBuilder(httpClient, INDEX_NAME);
    }

    @Test
    public void shouldSendIndexExistsRequestForNotExistingIndex() throws Exception {
        // given
        when(httpClient.prepareHead("/" + INDEX_NAME)).thenReturn(boundRequestBuilder);
        when(boundRequestBuilder.execute()).thenReturn(new CompletedFuture(new MockResponse(404, "ok", "someBody")));

        // when
        boolean exists = testee.execute();

        // then
        assertThat(exists, is(false));
        verify(httpClient).prepareHead("/" + INDEX_NAME);
        verify(boundRequestBuilder).execute();
    }

    @Test
    public void shouldSendIndexExistsRequestForExistingIndex() throws Exception {
        // given
        when(httpClient.prepareHead("/" + INDEX_NAME)).thenReturn(boundRequestBuilder);
        when(boundRequestBuilder.execute()).thenReturn(new CompletedFuture(new MockResponse(200, "ok", "someBody")));

        // when
        boolean exists = testee.execute();

        // then
        assertThat(exists, is(true));
        verify(httpClient).prepareHead("/" + INDEX_NAME);
        verify(boundRequestBuilder).execute();
    }

    @Test(expectedExceptions = HttpServerErrorException.class)
    public void shouldThrowWhenServerReturnsBadStatusCode() throws Exception {
        // given
        when(httpClient.prepareHead("/" + INDEX_NAME)).thenReturn(boundRequestBuilder);
        when(boundRequestBuilder.execute()).thenReturn(new CompletedFuture(new MockResponse(503, "Varnish ist ein guter Proxy", "someBodyDanceWithMe")));

        // when
        try {
            testee.execute();
        }

        // then
        catch (HttpServerErrorException e) {
            assertThat(e.getStatusCode(), is(503));
            assertThat(e.getMessage(), is("503 Varnish ist ein guter Proxy"));
            assertThat(e.getResponseBody(), is("someBodyDanceWithMe"));
            throw e;
        }
    }
}