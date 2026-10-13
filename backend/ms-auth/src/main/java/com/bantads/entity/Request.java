package com.bantads.entity;

import lombok.Builder;
import lombok.Data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.UUID;
import org.bson.types.ObjectId;

@Data
@Builder
@Document(collection = "requests")
public class Request
{
    @Id
    private ObjectId id;

    @Field("saga_id")
    private UUID sagaId;

    @Field("type")
    private String type;

    @Field("is_compensation")
    private boolean compensation;
}
