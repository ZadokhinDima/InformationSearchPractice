package dz.folderprocessor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

@ConfigurationProperties(prefix = "path")
public record PathProps(Path scan, Path vocabulary) {}

