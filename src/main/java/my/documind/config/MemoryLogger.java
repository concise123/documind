package my.documind.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class MemoryLogger {
    private static final int BYTES_PER_MB = 1024 * 1024;

    public void logMemory(String stage) {
        Runtime runtime = Runtime.getRuntime();
        long used = (runtime.totalMemory() - runtime.freeMemory()) / BYTES_PER_MB;
        long total = runtime.totalMemory() / BYTES_PER_MB;
        long max = runtime.maxMemory() / BYTES_PER_MB;
        log.info("{} (Memory - used={}MB, total={}MB, max={}MB)", stage, used, total,max);
    }
}
