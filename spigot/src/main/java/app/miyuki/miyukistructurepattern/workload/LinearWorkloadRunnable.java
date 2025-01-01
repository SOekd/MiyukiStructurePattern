package app.miyuki.miyukistructurepattern.workload;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Deque;

public class LinearWorkloadRunnable implements WorkloadRunnable {

    private final Deque<Workload> workloadDeque = new ArrayDeque<>();

    @Override
    public void addWorkload(@NotNull Workload workload) {
        this.workloadDeque.add(workload);
    }

    @Override
    public void run() {
        long stopTime = System.nanoTime() + MAX_NANOS_PER_TICK;

        Workload nextLoad;
        while (System.nanoTime() <= stopTime && (nextLoad = this.workloadDeque.poll()) != null) {
            nextLoad.compute();
        }
    }

}
