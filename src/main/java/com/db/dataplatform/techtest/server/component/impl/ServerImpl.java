package com.db.dataplatform.techtest.server.component.impl;

import com.db.dataplatform.techtest.server.api.model.DataBody;
import com.db.dataplatform.techtest.server.api.model.DataEnvelope;
import com.db.dataplatform.techtest.server.api.model.DataHeader;
import com.db.dataplatform.techtest.server.persistence.BlockTypeEnum;
import com.db.dataplatform.techtest.server.persistence.model.DataBodyEntity;
import com.db.dataplatform.techtest.server.persistence.model.DataHeaderEntity;
import com.db.dataplatform.techtest.server.service.DataBodyService;
import com.db.dataplatform.techtest.server.component.Server;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServerImpl implements Server {
    public static final String URI_HADOOP_PUSHDATA = "http://localhost:8090/hadoopserver/pushbigdata";
    private final DataBodyService dataBodyServiceImpl;
    private final ModelMapper modelMapper;
    private final RestTemplate restTemplate;

    /**
     * @param envelope
     * @return true if there is a match with the client provided checksum.
     */
    @Override
    public boolean saveDataEnvelope(DataEnvelope envelope) {
        // Save to persistence.
        persist(envelope);
        log.info("Data persisted successfully, data name: {}", envelope.getDataHeader().getName());
        return true;
    }

    /**
     * @param blockType
     * @return
     */
    @Override
    public List<DataEnvelope> getDataEnvelope(String blockType) {
        try {
            BlockTypeEnum blockTypeEnumValue = BlockTypeEnum.valueOf(blockType);
            List<DataBodyEntity> dataBodyEntities = dataBodyServiceImpl.getDataByBlockType(blockTypeEnumValue);
            List<DataEnvelope> dataEnvelopes = dataBodyEntities
                    .stream()
                    .map(dataBodyEntity -> {
                        DataHeader dataHeader = DataHeader.builder().name(dataBodyEntity.getDataHeaderEntity().getName()).blockType(dataBodyEntity.getDataHeaderEntity().getBlocktype()).build();
                        DataBody dataBody = DataBody.builder().dataBody(dataBodyEntity.getDataBody()).build();
                        return DataEnvelope.builder().dataHeader(dataHeader).dataBody(dataBody).build();
                    })
                    .collect(Collectors.toList());
            return dataEnvelopes;
        } catch (IllegalArgumentException illegalArgumentException) {
            log.info("Invalid blockType, data name: {}", blockType);
            throw new RuntimeException();
        }
    }

    @Override
    public void updateDataEnvelope(String name, String newBlockType) {
        try {
            BlockTypeEnum blockTypeEnumValue = BlockTypeEnum.valueOf(newBlockType);
            Optional<DataBodyEntity> dataBodyEntityOptional = dataBodyServiceImpl.getDataByName(name);
            if (dataBodyEntityOptional.isPresent()) {
                DataBodyEntity dataBodyEntity = dataBodyEntityOptional.get();
                dataBodyEntity.getDataHeaderEntity().setBlocktype(blockTypeEnumValue);
                dataBodyServiceImpl.saveDataBody(dataBodyEntity);
            }
        } catch (IllegalArgumentException illegalArgumentException) {
            log.info("Invalid blockType, data name: {}", newBlockType);
            throw new RuntimeException();
        }
    }

    public Boolean pushBigData(String payload) {
        try {
            ResponseEntity<Boolean> response = restTemplate.postForEntity(URI_HADOOP_PUSHDATA, payload, Boolean.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully pushed the data to Data Lake");
                return response.getBody();
            } else {
                log.error("Error in pushing the data to Data Lake " + response.getStatusCode());
                return false;
            }
        } catch (HttpServerErrorException.GatewayTimeout gatewayTimeout) {
            log.error("Error in pushing the data to Data Lake " + gatewayTimeout.getMessage());
            return false;
        }


    }

    private void persist(DataEnvelope envelope) {
        log.info("Persisting data with attribute name: {}", envelope.getDataHeader().getName());
        DataHeaderEntity dataHeaderEntity = modelMapper.map(envelope.getDataHeader(), DataHeaderEntity.class);

        DataBodyEntity dataBodyEntity = modelMapper.map(envelope.getDataBody(), DataBodyEntity.class);
        dataBodyEntity.setDataHeaderEntity(dataHeaderEntity);

        saveData(dataBodyEntity);
    }

    private void saveData(DataBodyEntity dataBodyEntity) {
        dataBodyServiceImpl.saveDataBody(dataBodyEntity);
    }

}
