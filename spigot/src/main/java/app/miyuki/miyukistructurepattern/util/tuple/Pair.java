package app.miyuki.miyukistructurepattern.util.tuple;

import java.util.Objects;

public class Pair<L, R> {

    private final L t;
    private final R r;

    public static <L, R> Pair<L, R> of(L t, R r) {
        return new Pair<>(t, r);
    }

    private Pair(L t, R r) {

        this.t = t;
        this.r = r;
    }

    public L left() {
        return t;
    }

    public R right() {
        return r;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(t, pair.t) && Objects.equals(r, pair.r);
    }

    @Override
    public int hashCode() {
        return Objects.hash(t, r);
    }

    @Override
    public String toString() {
        return "Pair{" +
                "t=" + t +
                ", r=" + r +
                '}';
    }
}
