package lt.esdc.tunnel.config;

public final class TrainConfig {
    private String direction;
    private long arrivalTimeDelayMs;

    public String getDirection() {
        return direction;
    }

    public long getArrivalTimeDelayMs() {
        return arrivalTimeDelayMs;
    }
}