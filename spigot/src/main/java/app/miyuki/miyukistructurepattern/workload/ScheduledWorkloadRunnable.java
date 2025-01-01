package app.miyuki.miyukistructurepattern.workload;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Deque;

public class ScheduledWorkloadRunnable implements WorkloadRunnable {

    private final Deque<ScheduledWorkload> workloadDeque = new ArrayDeque<>();

    @Override
    public void addWorkload(@NotNull Workload workload) {
        if (!(workload instanceof ScheduledWorkload)) {
            throw new IllegalArgumentException("Workload must be an instance of ScheduledWorkload");
        }

        this.workloadDeque.add((ScheduledWorkload) workload);
    }

    @Override
    public void run() {
        long stopTime = System.nanoTime() + MAX_NANOS_PER_TICK;

        ScheduledWorkload lastElement = this.workloadDeque.peekLast();
        ScheduledWorkload nextLoad = null;

        while (System.nanoTime() <= stopTime && !this.workloadDeque.isEmpty() && lastElement != nextLoad) {
            nextLoad = this.workloadDeque.poll();
            nextLoad.compute();
            if (nextLoad.shouldBeRescheduled()) {
                this.addWorkload(nextLoad);
            }
        }
    }

}
