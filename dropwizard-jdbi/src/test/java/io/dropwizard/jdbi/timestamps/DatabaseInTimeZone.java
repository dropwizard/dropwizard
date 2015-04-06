package io.dropwizard.jdbi.timestamps;

import org.h2.tools.Server;
import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Run an instance of the H2 database in an another time zone
 */
public class DatabaseInTimeZone extends ExternalResource {

    private final TemporaryFolder temporaryFolder;
    private final TimeZone timeZone;

    private Process process;

    public DatabaseInTimeZone(TemporaryFolder temporaryFolder, TimeZone timeZone) {
        this.temporaryFolder = temporaryFolder;
        this.timeZone = timeZone;
    }

    @Override
    protected void before() throws Throwable {
        String java = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        File h2jar = new File(Server.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        String vmArguments = "-Duser.timezone=" + timeZone.getID();

        ProcessBuilder pb = new ProcessBuilder(java, vmArguments, "-cp", h2jar.getAbsolutePath(), Server.class.getName(),
                "-tcp", "-baseDir", temporaryFolder.newFolder().getAbsolutePath());
        process = pb.start();
    }

    @Override
    protected void after() {
        try {
            // Graceful shutdown of the database
            Server.shutdownTcpServer("tcp://localhost:9092", "", true, false);
            boolean exited = waitFor(process, 1, TimeUnit.SECONDS);
            if (!exited) {
                process.destroy();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Unable shutdown DB", e);
        }
    }

    private static boolean waitFor(Process process, long timeout, TimeUnit unit) throws InterruptedException {
        long startTime = System.nanoTime();
        while (true) {
            try {
                process.exitValue();
                return true;
            } catch (IllegalThreadStateException ex) {
                Thread.sleep(100);
            }
            if (System.nanoTime() - startTime > unit.toNanos(timeout)) {
                return false;
            }
        }
    }
}