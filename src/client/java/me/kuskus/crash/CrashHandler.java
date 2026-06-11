package me.kuskus.crash;

import me.kuskus.KusKusKlient;
import me.kuskus.module.Module;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

public final class CrashHandler {
    private CrashHandler() {
    }

    public static void install() {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            save(thread, throwable);
            KusKusKlient.LOGGER.error("Uncaught exception in {}", thread.getName(), throwable);
        });
    }

    private static void save(Thread thread, Throwable throwable) {
        try {
            Path dir = KusKusKlient.CONFIG.root().resolve("crashes");
            Files.createDirectories(dir);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            Path file = dir.resolve("crash-" + timestamp + ".txt");
            StringWriter stack = new StringWriter();
            throwable.printStackTrace(new PrintWriter(stack));
            StringBuilder builder = new StringBuilder();
            builder.append("Kus Kus Klient ").append(KusKusKlient.VERSION).append('\n');
            builder.append("Thread: ").append(thread.getName()).append('\n');
            builder.append("Active modules:\n");
            for (Module module : KusKusKlient.MODULES.enabled()) {
                builder.append("- ").append(module.name()).append('\n');
            }
            builder.append("\nStacktrace:\n").append(stack);
            Files.writeString(file, builder.toString());
            try (var stream = Files.list(dir)) {
                var files = stream.sorted(Comparator.comparing((Path path) -> path.toFile().lastModified()).reversed()).skip(20).toList();
                for (Path old : files) {
                    Files.deleteIfExists(old);
                }
            }
        } catch (Exception exception) {
            KusKusKlient.LOGGER.warn("Failed to save crash report", exception);
        }
    }
}
