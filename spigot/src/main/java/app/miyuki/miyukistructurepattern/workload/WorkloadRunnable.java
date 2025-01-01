package app.miyuki.miyukistructurepattern.workload;

import org.jetbrains.annotations.NotNull;

public interface WorkloadRunnable extends Runnable {

    double MAX_MILLIS_PER_TICK = 2.5;
    int MAX_NANOS_PER_TICK = (int) (MAX_MILLIS_PER_TICK * 1E6);

    void addWorkload(@NotNull Workload workload);

}
