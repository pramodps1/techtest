package com.db.dataplatform.techtest.server.api.controller;

import com.db.dataplatform.techtest.server.api.model.DataEnvelope;
import com.db.dataplatform.techtest.server.component.Server;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/dataserver")
@RequiredArgsConstructor
public class ServerController {

    private final Server server;

    @PostMapping(value = "/pushdata", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> pushData(@Valid @RequestBody DataEnvelope dataEnvelope) throws IOException, NoSuchAlgorithmException {

        log.info("Data envelope received: {}", dataEnvelope.getDataHeader().getName());
        String calculatedChecksum = DigestUtils.md5Hex(dataEnvelope.getDataBody().getDataBody());
        if (calculatedChecksum.equals(dataEnvelope.getDataHeader().getChecksum())) {
            boolean checksumPass = server.saveDataEnvelope(dataEnvelope);
            log.info("Data envelope persisted. Attribute name: {}", dataEnvelope.getDataHeader().getName());
            server.pushBigData(dataEnvelope.getDataBody().getDataBody());
            return ResponseEntity.ok(checksumPass);
        } else {
            log.error("Data envelope checksum not matching. Attribute name: {}", dataEnvelope.getDataHeader().getName());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);
        }
    }

    @GetMapping(value = "/data/{blockType}")
    public List<DataEnvelope> getdata(@PathVariable("blockType") String blockType) {
        return server.getDataEnvelope(blockType);

    }

    @PutMapping(value = "/update/{name}/{newBlockType}")
    public void updateData(@PathVariable("name") String name, @PathVariable("newBlockType") String newBlockType) {
        server.updateDataEnvelope(name, newBlockType);
    }


}
