package com.bantads.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Value("${spring.data.mongodb.host:localhost}") private String host;
    @Value("${spring.data.mongodb.port:27017}") private int port;
    @Value("${spring.data.mongodb.database:auth_db}") private String database;
    @Value("${spring.data.mongodb.username:}") private String username;
    @Value("${spring.data.mongodb.password:}") private String password;

    @Override
    protected String getDatabaseName() { return database; }

    @Override
    public MongoClient mongoClient() {
        String auth = (username != null && !username.isEmpty())
                ? username + ":" + password + "@"
                : "";
        String uri = String.format("mongodb://%s%s:%d/%s?authSource=admin", auth, host, port, database);
        return MongoClients.create(MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(uri)).build());
    }
}
