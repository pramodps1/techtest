package com.db.dataplatform.techtest.client.component.impl;

import com.db.dataplatform.techtest.client.api.model.DataEnvelope;
import com.db.dataplatform.techtest.client.component.Client;
import com.db.dataplatform.techtest.client.exception.ServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;

import java.net.URI;
import java.util.List;

/**
 * Client code does not require any test coverage
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class ClientImpl implements Client {

    public static final String URI_PUSHDATA = "http://localhost:8090/dataserver/pushdata";
    public static final UriTemplate URI_GETDATA = new UriTemplate("http://localhost:8090/dataserver/data/{blockType}");
    public static final UriTemplate URI_PATCHDATA = new UriTemplate("http://localhost:8090/dataserver/update/{name}/{newBlockType}");


    private final RestTemplate restTemplate;

    @Override
    public void pushData(DataEnvelope dataEnvelope) {
        log.info("Pushing data {} to {}", dataEnvelope.getDataHeader().getName(), URI_PUSHDATA);
        ResponseEntity<Boolean> response = restTemplate.postForEntity(URI_PUSHDATA, dataEnvelope, Boolean.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("Error in pushing data");
            throw new ServiceUnavailableException("Error in pushing data");
        }
    }

    @Override
    public List<DataEnvelope> getData(String blockType) {
        log.info("Query for data with header block type {}", blockType);
        URI uriWithBlockType = URI_GETDATA.expand(blockType);
        try {
            ResponseEntity<List<DataEnvelope>> response = restTemplate.exchange(
                    uriWithBlockType,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<DataEnvelope>>() {
                    }
            );
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                log.error("Error in getting data, Response code:-" + response.getStatusCode());
                throw new ServiceUnavailableException("Error in getting data");
            }
        } catch (HttpClientErrorException e) {
            log.error("Error in getting data, Exception:-" + e);
            throw new ServiceUnavailableException("Error in getting data error:-" + e);
        }

    }

    @Override
    public boolean updateData(String blockName, String newBlockType) {
        log.info("Updating blocktype to {} for block with name {}", newBlockType, blockName);
        URI uriWithBlockType = URI_PATCHDATA.expand(blockName, newBlockType);
        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    uriWithBlockType,
                    HttpMethod.PUT,
                    null,
                    Void.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return true;
            } else {
                log.error("Error in updating data , Response code:-" + response.getStatusCode());
                throw new ServiceUnavailableException("Error in updating data");
            }
        } catch (HttpClientErrorException e) {
            log.error("Error in updating data, Exception:-" + e);
            throw new ServiceUnavailableException("Error in updating data");
        }

    }


}
