package lt.esdc.tunnel.config;

import java.util.List;

public final class AppConfig {
    private TunnelConfig tunnel;
    private List<TrainConfig> trains;

    public TunnelConfig getTunnel() {
        return tunnel;
    }

    public List<TrainConfig> getTrains() {
        return trains;
    }
}