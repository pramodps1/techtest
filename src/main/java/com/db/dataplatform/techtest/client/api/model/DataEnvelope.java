package com.db.dataplatform.techtest.client.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;

import javax.validation.constraints.NotNull;

@JsonSerialize(as = DataEnvelope.class)
@JsonDeserialize(as = DataEnvelope.class)
@Getter
@Builder

@AllArgsConstructor
@NoArgsConstructor
public class DataEnvelope {

    @NotNull
    private  DataHeader dataHeader;

    @NotNull
    private  DataBody dataBody;
}
