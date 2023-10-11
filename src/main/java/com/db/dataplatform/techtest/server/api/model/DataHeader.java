package com.db.dataplatform.techtest.server.api.model;

import com.db.dataplatform.techtest.server.persistence.BlockTypeEnum;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;

import javax.validation.constraints.NotBlank;

@JsonSerialize(as = DataHeader.class)
@JsonDeserialize(as = DataHeader.class)

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@ToString
public class DataHeader {

    private  String checksum;
    @NotBlank
    private  String name;

    private  BlockTypeEnum blockType;

}
