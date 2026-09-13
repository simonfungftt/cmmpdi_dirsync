package com.ctg.innovic.cmmpdi.dirsync.utils;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class OldestFileFinderService {

    public Optional<Path> getOldestFile(String dirPath, String fileExtension) throws IOException {
        Path path = Paths.get(dirPath);

        try (Stream<Path> stream = Files.list(path)) {
            return stream
                    .filter(Files::isRegularFile) // Exclude subdirectories
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(fileExtension)) // Filter by .ldif extension
                    .min(Comparator.comparing(p -> getCreationTime(p)));
        }
    }

    private static FileTime getCreationTime(Path path) {
        try {
            // Fetch file creation time attributes
            return Files.readAttributes(path, BasicFileAttributes.class).creationTime();
        } catch (IOException e) {
            // Fallback to last modified time if creation time fails
            return FileTime.fromMillis(path.toFile().lastModified());
        }
    }
}