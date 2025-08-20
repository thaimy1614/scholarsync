package com.datn.notification_service.config;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;

@Setter
@Getter
@Configuration
@EnableScheduling
@Slf4j
public class CredentialConfig {
    private Credential credential;

    @Autowired
    private GoogleAuthorizationCodeFlow flow;

    @Autowired
    public CredentialConfig(GoogleAuthorizationCodeFlow flow) throws Exception {
        this.flow = flow;
        loadCredential();
    }

    private void loadCredential() throws Exception {
        Credential storedCredential = flow.loadCredential("user");
        if (storedCredential != null) {
            this.credential = storedCredential;
            log.info("Loaded stored credential with access token: {}", storedCredential.getAccessToken());
            log.info("Refresh token: {}", storedCredential.getRefreshToken());
            log.info("Expires in: {} seconds", storedCredential.getExpiresInSeconds());
        } else {
            log.warn("No stored credential found in tokens directory.");
        }
    }

    public Credential getCredential() throws Exception {
        if (this.credential == null) {
            loadCredential();
        }
        return this.credential;
    }

    public void setCredential(Credential credential) {
        this.credential = credential;
        log.info("Credential updated with access token: {}", credential != null ? credential.getAccessToken() : "null");
    }

    @Scheduled(fixedRate = 10 * 60 * 1000)
    public void refreshCredentialIfNeeded() throws Exception {
        if (this.credential != null) {
            Long expiresIn = this.credential.getExpiresInSeconds();
            if (expiresIn != null && expiresIn <= 600) {
                try {
                    boolean refreshed = this.credential.refreshToken();
                    if (refreshed) {
                        log.info("Access token refreshed successfully: {}", this.credential.getAccessToken());
                    } else {
                        log.info("Failed to refresh token (returned false). Refresh token: {}", this.credential.getRefreshToken());
                        attemptReloadCredential();
                    }
                } catch (IOException e) {
                    log.info("IOException during token refresh: {}", e.getMessage(), e);
                    attemptReloadCredential();
                }
            } else {
                log.info("Token still valid, expires in {} seconds.", expiresIn);
            }
        } else {
            log.info("No credential to refresh. Attempting to reload from storage.");
            loadCredential();
        }
    }

    private void attemptReloadCredential() throws Exception {
        log.info("Attempting to reload credential from storage.");
        Credential storedCredential = flow.loadCredential("user");
        if (storedCredential != null && storedCredential.getRefreshToken() != null) {
            this.credential = storedCredential;
            log.info("Reloaded credential with access token: {}", storedCredential.getAccessToken());
            if (storedCredential.getExpiresInSeconds() != null && storedCredential.getExpiresInSeconds() <= 300) {
                boolean refreshed = storedCredential.refreshToken();
                if (refreshed) {
                    log.info("Access token refreshed after reload: {}", storedCredential.getAccessToken());
                } else {
                    log.error("Failed to refresh reloaded token. Manual reauthorization required.");
                    this.credential = null;
                }
            }
        } else {
            log.error("No valid credential found in storage or missing refresh token. Please reauthorize at /authorize.");
            this.credential = null;
        }
    }

    public boolean isCredentialInitialized() {
        return this.credential != null;
    }
}