package manager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author ☂️☂️Duy Coder 💖💖
 */
public class ExecutorVirtualThread {

    private static final ExecutorService excutorSession = Executors.newVirtualThreadPerTaskExecutor();
    private static final ExecutorService excutorMap = Executors.newVirtualThreadPerTaskExecutor();
    private static final ExecutorService excutorPlayer = Executors.newVirtualThreadPerTaskExecutor();
    private static final ExecutorService executorServer = Executors.newVirtualThreadPerTaskExecutor();

    public static void shutdownServer() {
        executorServer.shutdown();
    }

    public static void submitServer(Runnable runnable) {
        executorServer.submit(runnable);
    }

    public static void submitThreadPlayer(Runnable runnable) {
        excutorPlayer.submit(runnable);
    }

    public static void submitThreadSession(Runnable runnable) {
        excutorSession.submit(runnable);
    }

    public static void submitThreadMap(Runnable runnable) {
        excutorMap.submit(runnable);
    }
}
