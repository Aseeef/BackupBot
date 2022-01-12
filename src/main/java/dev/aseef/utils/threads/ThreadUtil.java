package dev.aseef.utils.threads;

import dev.aseef.config.Config;

import java.util.concurrent.*;

public class ThreadUtil {

    private static final ExecutorService service = Executors.newFixedThreadPool(Config.get().getThreadPool(), new ErrorCatchingThreadFactory());

    public static void runAsync(Runnable target) {
        service.submit(target);
    }

    public static ScheduledFuture<?> runTaskTimer(Runnable task, long startDelayMillis, long periodMillis) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        return executor.scheduleAtFixedRate(() -> {
            try {
                task.run();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }, startDelayMillis, periodMillis, TimeUnit.MILLISECONDS);
    }

    public static ScheduledFuture<?> runDelayedTask(Runnable task, long startDelayMillis) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        return executor.schedule(() -> {
            try {
                task.run();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }, startDelayMillis, TimeUnit.MILLISECONDS);
    }


}
