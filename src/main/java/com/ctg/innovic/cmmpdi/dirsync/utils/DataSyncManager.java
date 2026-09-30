package com.ctg.innovic.cmmpdi.dirsync.utils;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class DataSyncManager {

    private static final Path STATE_FILE = Paths.get("data_sync_state.txt");
    private static final Instant DEFAULT_INITIAL_START = Instant.parse("2026-01-01T00:00:00Z");

    public Instant getLastSyncTime() {

        if (!Files.exists(STATE_FILE)) {
            return DEFAULT_INITIAL_START;
        }

        try {

            String content = Files.readString(STATE_FILE, StandardCharsets.UTF_8).trim();
            return Instant.parse(content).minus(2, ChronoUnit.MINUTES);
        }
        catch (Exception e) {
            System.err.println("Could not read state file, using default start: " + e.getMessage());
            return DEFAULT_INITIAL_START;
        }
    }

    public void saveLastSyncTime(Instant timestamp) {

        try {

            Files.writeString(
                    STATE_FILE,
                    timestamp.toString(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        }
        catch (IOException e) {
            throw new IllegalStateException("Failed to persist sync timestamp", e);
        }
    }
}