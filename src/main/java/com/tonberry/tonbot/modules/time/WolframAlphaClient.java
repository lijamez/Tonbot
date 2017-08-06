package com.tonberry.tonbot.modules.time;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

class WolframAlphaClient {

    private final String appId;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Inject
    public WolframAlphaClient(@WolframAlphaAppId String appId, HttpClient httpClient, ObjectMapper objectMapper) {
        this.appId = Preconditions.checkNotNull(appId, "appId must be non-null.");
        this.httpClient = Preconditions.checkNotNull(httpClient, "httpClient must be non-null.");
        this.objectMapper = Preconditions.checkNotNull(objectMapper, "objectMapper must be non-null.");
    }

    public WolframAlphaQueryResponse query(String input) {
        Preconditions.checkNotNull(input, "input must be non-null.");

        try {
            URI uri = new URIBuilder()
                    .setScheme("https")
                    .setHost("api.wolframalpha.com")
                    .setPath("/v2/query")
                    .setParameter("appid", appId)
                    .setParameter("format", "image,plaintext")
                    .setParameter("input", input)
                    .setParameter("output", "JSON")
                    .build();

            return get(uri, WolframAlphaQueryResponse.class);

        } catch (URISyntaxException e) {
            throw new WolframAlphaException("Couldn't search for movies.", e);
        }
    }

    private <T> T get(URI uri, Class<T> clazz) {
        try {
            HttpGet httpGet = new HttpGet(uri);

            HttpResponse response = httpClient.execute(httpGet);

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new WolframAlphaException("Wolfram Alpha returned a " + response.getStatusLine().getStatusCode() + " status code.");
            }

            T searchResult = objectMapper.readValue(response.getEntity().getContent(), clazz);

            return searchResult;
        } catch (IOException e) {
            throw new WolframAlphaException("Failed to make Wolfram Alpha call.", e);
        }
    }
}
