package com.lombardrisk.ignis.spark.config.interceptor;

import com.google.common.collect.ImmutableMap;
import okhttp3.Interceptor;
import okhttp3.Request;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HeaderInterceptorTest {

    private final HeaderInterceptor headerInterceptor = new HeaderInterceptor(ImmutableMap.of(
            "header1", "value1",
            "header2", "value2"));

    @Mock
    private Interceptor.Chain chain;

    @Captor
    private ArgumentCaptor<Request> requestArgumentCaptor;

    @Test
    public void intercept_AddsHeaderToRequest() throws Exception {
        Request request = new Request.Builder()
                .url("http://google.com")
                .build();

        when(chain.request())
                .thenReturn(request);

        headerInterceptor.intercept(chain);

        verify(chain).proceed(requestArgumentCaptor.capture());

        assertThat(requestArgumentCaptor.getValue().headers().toMultimap())
                .containsExactly(
                        entry("header1", singletonList("value1")),
                        entry("header2", singletonList("value2"))
                );
    }
}
