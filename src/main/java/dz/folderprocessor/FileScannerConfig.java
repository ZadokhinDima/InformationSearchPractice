package dz.folderprocessor;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.dsl.Files;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.File;


@Configuration
@EnableIntegration
@RequiredArgsConstructor
public class FileScannerConfig {

    private final PathProps props;

    @Bean
    public ThreadPoolTaskExecutor fileProcessingExecutor() {
        var exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(4);
        exec.setMaxPoolSize(16);
        exec.setQueueCapacity(100);
        exec.setThreadNamePrefix("file-worker-");
        exec.initialize();
        return exec;
    }

    @Bean
    public IntegrationFlow folderInboundFlow(FileProcessor processor, ThreadPoolTaskExecutor fileProcessingExecutor) {
        return IntegrationFlow
                .from(Files.inboundAdapter(props.scan().toFile())
                        .autoCreateDirectory(true)
                        .preventDuplicates(true)
                        .useWatchService(true)
                        .watchEvents(
                                FileReadingMessageSource.WatchEventType.CREATE,
                                FileReadingMessageSource.WatchEventType.MODIFY),
                        e -> e.poller(Pollers.fixedDelay(0).receiveTimeout(0).maxMessagesPerPoll(10))
                )
                .channel(c -> c.executor(fileProcessingExecutor))
                .transform(File::toPath)
                .handle(processor, "processFile")
                .get();
    }
}
