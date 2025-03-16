package com.chasion.controller.advice;

import com.alibaba.fastjson2.JSON;
import com.chasion.exc.BaseException;
import com.chasion.resp.ResultData;
import feign.Response;
import feign.codec.ErrorDecoder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class FeignErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String s, Response response) {
        String errorBody = extractResponseBody(response);
        ResultData<?> apiResponse = JSON.parseObject(errorBody, ResultData.class);
        return new BaseException(apiResponse.getCode(), apiResponse.getMessage());
    }
    // 提取响应体的工具方法
    private String extractResponseBody(Response response) {
        if (response.body() == null) {
            return "";
        }
        try (InputStream inputStream = response.body().asInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read response body", e);
        }
    }
}
