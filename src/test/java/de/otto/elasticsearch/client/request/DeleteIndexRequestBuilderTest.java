package de.otto.elasticsearch.client.request;

import com.ning.http.client.AsyncHttpClient;
import de.otto.elasticsearch.client.CompletedFuture;
import de.otto.elasticsearch.client.MockResponse;
import de.otto.elasticsearch.client.response.HttpServerErrorException;
import de.otto.elasticsearch.client.util.RoundRobinLoadBalancingHttpClient;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

public class DeleteIndexRequestBuilderTest {

    private RoundRobinLoadBalancingHttpClient httpClient;
    private DeleteIndexRequestBuilder testee;

    @BeforeMethod
    private void setup() {
        httpClient = mock(RoundRobinLoadBalancingHttpClient.class);
        testee = new DeleteIndexRequestBuilder(httpClient, "someIndexName");
    }

    @Test
    public void shouldDeleteIndex() {
        AsyncHttpClient.BoundRequestBuilder boundRequestBuilderMock = mock(AsyncHttpClient.BoundRequestBuilder.class);

        when(httpClient.prepareDelete("/someIndexName")).thenReturn(boundRequestBuilderMock);
        when(boundRequestBuilderMock.execute()).thenReturn(new CompletedFuture(new MockResponse(200, "ok", "")));
        testee.execute();
        verify(httpClient).prepareDelete("/someIndexName");
    }

    @Test(expectedExceptions = HttpServerErrorException.class)
    public void shouldThrowExceptionIfStatusCodeNotOk() {
        AsyncHttpClient.BoundRequestBuilder boundRequestBuilderMock = mock(AsyncHttpClient.BoundRequestBuilder.class);
        when(httpClient.prepareDelete("/someIndexName")).thenReturn(boundRequestBuilderMock);
        when(boundRequestBuilderMock.execute()).thenReturn(new CompletedFuture(new MockResponse(400, "not ok", "")));
        try {
            testee.execute();
        } catch (HttpServerErrorException e) {
            assertThat(e.getMessage(), is("400 not ok"));
            throw e;
        }
    }

}