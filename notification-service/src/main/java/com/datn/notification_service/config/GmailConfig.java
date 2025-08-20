package com.datn.notification_service.config;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.io.File;
import java.io.InputStreamReader;
import java.util.Collections;

@Configuration
@Slf4j
public class GmailConfig {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    @Value("${spring.google.client-secret-path}")
    private String clientSecretPath;

    @Value("${spring.google.redirect-uri}")
    private String redirectUri;

    @Value("${spring.google.scopes}")
    private String scopes;

    @Bean
    @Lazy
    public Gmail gmailService(CredentialConfig credentialConfig) throws Exception {
        Credential credential = credentialConfig.getCredential();
        if (credential == null) {
            log.info("Credential is null, creating a new one.");
        }
        return new Gmail.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                credential)
                .setApplicationName("Spring Boot Gmail API")
                .build();
    }

    @Bean
    public GoogleAuthorizationCodeFlow googleAuthorizationCodeFlow(DataStoreFactory dataStoreFactory) throws Exception {
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                JSON_FACTORY,
                new InputStreamReader(getClass().getClassLoader().getResourceAsStream(clientSecretPath))
        );

        return new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                clientSecrets,
                Collections.singletonList(scopes))
                .setDataStoreFactory(dataStoreFactory)
                .setAccessType("offline")
                .setApprovalPrompt("force")
                .build();
    }

    @Bean
    public DataStoreFactory dataStoreFactory() throws Exception {
        return new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH));
    }
}