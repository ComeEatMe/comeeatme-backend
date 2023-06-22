package com.comeeatme.batch.service.juso;

import com.comeeatme.batch.config.ClientLog;
import com.comeeatme.batch.service.exception.RequestErrorException;
import com.comeeatme.batch.service.exception.RequestFrequentInShortException;
import com.comeeatme.batch.service.juso.dto.JusoAddressDto;
import com.comeeatme.batch.service.juso.dto.JusoCommonDto;
import com.comeeatme.batch.service.juso.dto.JusoResultDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;

import static java.util.Objects.isNull;

@Service
@ClientLog
@Slf4j
public class JusoService {

    private final String baseUrl;

    private final String jusoRoadAddressPath;

    private final String jusoRoadAddressKey;

    private final ObjectMapper objectMapper;

    private final HttpClient httpClient;

    public JusoService(
            @Value("${open-api.business-juso.base-url}") String baseUrl,
            @Value("${open-api.business-juso.road-address.path}") String jusoRoadAddressPath,
            @Value("${open-api.business-juso.road-address.key}") String jusoRoadAddressKey) {
        this.baseUrl = baseUrl;
        this.jusoRoadAddressPath = jusoRoadAddressPath;
        this.jusoRoadAddressKey = jusoRoadAddressKey;
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(2000L))
                .build();
    }

    public JusoAddressDto searchAddress(String keyword) {
        URI uri = URI.create(baseUrl + jusoRoadAddressPath + "?"
                + "confmKey=" + jusoRoadAddressKey + "&"
                + "resultType=json&"
                + "keyword=" + URLEncoder.encode(keyword, StandardCharsets.UTF_8)
        );
        HttpRequest httpRequest = HttpRequest.newBuilder(uri)
                .GET()
                .build();
        try {
            JusoAddressDto jusoAddressDto = objectMapper.readValue(
                    httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString()).body(),
                    JusoResultDto.class
            ).getResults();
            validateResult(jusoAddressDto.getCommon());
            return jusoAddressDto;
        } catch (IOException | InterruptedException e) {
            throw new RequestErrorException(e);
        }
    }

    private void validateResult(JusoCommonDto jusoCommon) {
        if (isNull(jusoCommon)) {
            throw new RequestErrorException("응답에 문제가 있습니다.");
        } else if (Objects.equals("E007", jusoCommon.getErrorCode())) {
            log.warn("짧은 시간 내에 API가 너무 많이 호출되었습니다.");
            throw new RequestFrequentInShortException(jusoCommon.getErrorMessage());
        } else if (!Objects.equals("0", jusoCommon.getErrorCode())) {
            throw new RequestErrorException(
                    jusoCommon.getErrorCode(), jusoCommon.getErrorMessage());
        }
    }
}

