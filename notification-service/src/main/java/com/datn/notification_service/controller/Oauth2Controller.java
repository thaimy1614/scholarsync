package com.datn.notification_service.controller;

import com.datn.notification_service.config.CredentialConfig;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/notification/oauth2")
public class Oauth2Controller {

    @Autowired
    private GoogleAuthorizationCodeFlow flow;

    @Autowired
    private CredentialConfig credentialConfig;

    @Value("${spring.google.redirect-uri}")
    private String redirectUri;

    @GetMapping("/authorize")
    public RedirectView startOAuthFlow() throws Exception {
        String authUrl = flow.newAuthorizationUrl()
                .setRedirectUri(redirectUri)
                .build();
        return new RedirectView(authUrl);
    }

    @GetMapping("/oauth2callback")
    public String handleOAuthCallback(@RequestParam("code") String code) throws Exception {
        GoogleTokenResponse tokenResponse = flow.newTokenRequest(code)
                .setRedirectUri(redirectUri)
                .execute();

        Credential credential = flow.createAndStoreCredential(tokenResponse, "user");
        credentialConfig.setCredential(credential);
        return "Authorization successful! Token stored. You can now use the application.";
    }
}