package com.tonberry.tonbot.modules.time

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.http.client.HttpClient
import org.apache.http.impl.client.HttpClients
import spock.lang.Specification

class WolframAlphaClientIntegrationTest extends Specification {

    WolframAlphaClient client

    def setup() {
        String wolframAlphaAppId = System.getProperty("wolframAlphaAppId")
        HttpClient httpClient  = HttpClients.createDefault();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        this.client = new WolframAlphaClient(wolframAlphaAppId, httpClient, objectMapper);
    }

    def "time conversion query"() {
        when:
        WolframAlphaQueryResponse response = client.query("10PM PST in London")

        then:
        response != null
        response.getQueryResult().isSuccess()
        !response.getQueryResult().isError()
    }
}
