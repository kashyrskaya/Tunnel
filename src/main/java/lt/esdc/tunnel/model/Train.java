package lt.esdc.tunnel.model;

import lt.esdc.tunnel.resource.Tunnel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class Train implements Callable<Boolean> {
    private static final Logger LOGGER = LogManager.getLogger(Train.class.getName());

    private final int trainId;
    private final Direction direction;
    private final Tunnel tunnel;
    private final int travelTimeSeconds;

    public Train(int trainId, Direction direction, Tunnel tunnel, int travelTimeSeconds) {
        this.trainId = trainId;
        this.direction = direction;
        this.tunnel = tunnel;
        this.travelTimeSeconds = travelTimeSeconds;
    }

    public Direction getDirection() {
        return direction;
    }

    @Override
    public Boolean call() throws Exception {
        LOGGER.info("Train {} heading {} is requesting to enter the tunnel.", trainId, direction);
        boolean entered = tunnel.enterTunnel(this);

        if (entered) {
            try {
                LOGGER.info("Train {} heading {} has entered the tunnel. Traveling for {} seconds.", trainId, direction, travelTimeSeconds);
                TimeUnit.SECONDS.sleep(travelTimeSeconds);
                LOGGER.info("Train {} heading {} has exited. The tunnel is now free.", trainId, direction);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.warn("The train {} heading {} was interrupted during travel.", trainId, direction);
                return false;
            } finally {
                tunnel.exitTunnel(this);
            }
        }
        return entered;
    }

    @Override
    public String toString() {
        return String.format("Train{trainId=%d, direction=%s}", trainId, direction);
    }
}
