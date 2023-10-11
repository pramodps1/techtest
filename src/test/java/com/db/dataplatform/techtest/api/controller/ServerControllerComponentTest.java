package com.db.dataplatform.techtest.api.controller;

import com.db.dataplatform.techtest.TestDataHelper;
import com.db.dataplatform.techtest.server.api.controller.ServerController;
import com.db.dataplatform.techtest.server.api.model.DataEnvelope;
import com.db.dataplatform.techtest.server.exception.HadoopClientException;
import com.db.dataplatform.techtest.server.component.Server;
import com.db.dataplatform.techtest.server.persistence.BlockTypeEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.catalina.LifecycleState;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.util.UriTemplate;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import com.fasterxml.jackson.core.type.TypeReference;

import static org.mockito.Mockito.verify;


@RunWith(MockitoJUnitRunner.class)
public class ServerControllerComponentTest {

    public static final String URI_PUSHDATA = "http://localhost:8090/dataserver/pushdata";
    public static final UriTemplate URI_GETDATA = new UriTemplate("http://localhost:8090/dataserver/data/{blockType}");
    public static final UriTemplate URI_PATCHDATA = new UriTemplate("http://localhost:8090/dataserver/update/{name}/{newBlockType}");

    public static final String HEADER_NAME = "TSLA-USDGBP-10Y";
    @Mock
    private Server serverMock;

    private DataEnvelope testDataEnvelope;
    private ObjectMapper objectMapper;
    private MockMvc mockMvc;
    private ServerController serverController;

    @Before
    public void setUp() throws HadoopClientException, NoSuchAlgorithmException, IOException {
        serverController = new ServerController(serverMock);
        mockMvc = standaloneSetup(serverController).build();
        objectMapper = Jackson2ObjectMapperBuilder
                .json()
                .build();

        testDataEnvelope = TestDataHelper.createTestDataEnvelopeApiObject();
        List<DataEnvelope> dataEnvelopes = new ArrayList<>();
        dataEnvelopes.add(testDataEnvelope);
        when(serverMock.saveDataEnvelope(any(DataEnvelope.class))).thenReturn(true);
        when(serverMock.getDataEnvelope(any(String.class))).thenReturn(dataEnvelopes);
        doNothing().when(serverMock).updateDataEnvelope(any(String.class), any(String.class));
    }

    @Test
    public void testPushDataPostCallWorksAsExpected() throws Exception {

        String testDataEnvelopeJson = objectMapper.writeValueAsString(testDataEnvelope);

        MvcResult mvcResult = mockMvc.perform(post(URI_PUSHDATA)
                        .content(testDataEnvelopeJson)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();

        boolean checksumPass = Boolean.parseBoolean(mvcResult.getResponse().getContentAsString());
        assertThat(checksumPass).isTrue();
    }

    @Test
    public void testGetDataGetCallWorksAsExpected() throws Exception {

        String testDataEnvelopeJson = objectMapper.writeValueAsString(testDataEnvelope);

        MvcResult mvcResult = mockMvc.perform(get(URI_GETDATA.expand(BlockTypeEnum.BLOCKTYPEA))
                        .content(testDataEnvelopeJson)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        List<DataEnvelope> dataEnvelopes = objectMapper.readValue(jsonResponse, new TypeReference<List<DataEnvelope>>() {
        });

        Assertions.assertThat(dataEnvelopes).hasSize(1);
        Assert.assertEquals(testDataEnvelope.toString(), dataEnvelopes.get(0).toString());
    }

    @Test
    public void testUpdateDataPutCallWorksAsExpected() throws Exception {

        String testDataEnvelopeJson = objectMapper.writeValueAsString(testDataEnvelope);

        mockMvc.perform(put(URI_PATCHDATA.expand(HEADER_NAME, BlockTypeEnum.BLOCKTYPEB.name()))
                        .content(testDataEnvelopeJson)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();
        verify(serverMock).updateDataEnvelope(HEADER_NAME, BlockTypeEnum.BLOCKTYPEB.name());
    }
}
