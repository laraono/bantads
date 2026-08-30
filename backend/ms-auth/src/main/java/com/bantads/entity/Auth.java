package com.bantads.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.UUID;

@Data
@Document(collection = "authors")
public class Auth 
{

    @Id
    private UUID userId;

    @Field("user_cpf")
    private String cpf;

    @Field("user_type")
    private String type;

    @Indexed(unique = true)
    @Field("login")
    private String login;

    @Field("password")
    private String password;

    @Field("is_active")
    private Boolean active;
}