package app.miyuki.miyukistructurepattern.workload;

@FunctionalInterface
public interface ScheduledWorkload extends Workload {

    default boolean shouldBeRescheduled() {
        return true;
    }

}
