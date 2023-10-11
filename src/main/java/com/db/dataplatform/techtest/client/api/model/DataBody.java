package com.db.dataplatform.techtest.client.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;

import javax.validation.constraints.NotNull;

@JsonSerialize(as = DataBody.class)
@JsonDeserialize(as = DataBody.class)
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DataBody {

    @NotNull
    private  String dataBody;

}
