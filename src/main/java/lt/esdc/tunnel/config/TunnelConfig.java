package lt.esdc.tunnel.config;

public final class TunnelConfig {
    private int capacity;
    private int maxTrainsInDirection;
    private int trainTravelTimeSeconds;

    public int getCapacity() {
        return capacity;
    }

    public int getMaxTrainsInDirection() {
        return maxTrainsInDirection;
    }

    public int getTrainTravelTimeSeconds() {
        return trainTravelTimeSeconds;
    }
}
