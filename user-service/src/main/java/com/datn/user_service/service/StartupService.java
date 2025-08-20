package com.datn.user_service.service;

import com.datn.user_service.config.SequenceInitializer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StartupService {
    private final SequenceInitializer sequenceInitializer;

    @PostConstruct
    public void init() {
        sequenceInitializer.createSequences();
    }
}

