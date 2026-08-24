package com.bantads.entity;

@Document(collection = "authors")
class Auth {
    @Id(name = "user_id")
    private UUID userId;

    @Field(name = "user_cpf")
    private String cpf;

    @Field(name = "user_type")
    private String type;

    @Field(name = "login")
    private String login;

    @Field(name = "password")
    private String password;

    @Field(name = "is_active")
    private Boolean active;

}