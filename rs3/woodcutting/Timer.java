package dbscripts.rs3.woodcutting;

public class Timer {

    private long end;
    private final long start;
    private final long period;

    public Timer(final long period) {
        this.period = period;
        start = System.currentTimeMillis();
        end = start + period;
    }

    public long getElapsed() {
        return System.currentTimeMillis() - start;
    }

    public long getRemaining() {
        if (isRunning()) {
            return end - System.currentTimeMillis();
        }
        return 0;
    }


    public boolean isRunning() {
        return System.currentTimeMillis() < end;
    }


    public void reset() {
        end = System.currentTimeMillis() + period;
    }

    public long setEndIn(final long ms) {
        end = System.currentTimeMillis() + ms;
        return end;
    }

    public static String format(final long time) {
        final StringBuilder t = new StringBuilder();
        final long total_secs = time / 1000;
        final long total_mins = total_secs / 60;
        final long total_hrs = total_mins / 60;
        final int secs = (int) total_secs % 60;
        final int mins = (int) total_mins % 60;
        final int hrs = (int) total_hrs % 60;
        if (hrs < 10) {
            t.append("0");
        }
        t.append(hrs);
        t.append(":");
        if (mins < 10) {
            t.append("0");
        }
        t.append(mins);
        t.append(":");
        if (secs < 10) {
            t.append("0");
        }
        t.append(secs);
        return t.toString();
    }
}