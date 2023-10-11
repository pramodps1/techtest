package com.db.dataplatform.techtest.server.component;

import com.db.dataplatform.techtest.server.api.model.DataEnvelope;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public interface Server {
    boolean saveDataEnvelope(DataEnvelope envelope) throws IOException, NoSuchAlgorithmException;

    List<DataEnvelope> getDataEnvelope(String blockType);

    void updateDataEnvelope(String name, String newBlockType);
    Boolean pushBigData(String payload);
}
