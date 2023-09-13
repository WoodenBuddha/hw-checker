package com.webdorphin.bot.homeworkchecker;

import com.webdorphin.bot.homeworkchecker.dto.remote.CodeXExecutionRequest;
import com.webdorphin.bot.homeworkchecker.dto.remote.CodeXExecutionResponse;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class HttpClientService {

    private static final String REMOTE_CPP_EXECUTION_ENDPOINT = "https://api.codex.jaagrav.in";

    public CodeXExecutionResponse executeRemotely(CodeXExecutionRequest request) {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.postForObject(REMOTE_CPP_EXECUTION_ENDPOINT, request, CodeXExecutionResponse.class);
    }

}
