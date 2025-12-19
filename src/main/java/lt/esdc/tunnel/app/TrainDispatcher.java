package lt.esdc.tunnel.app;

import lt.esdc.tunnel.config.AppConfig;
import lt.esdc.tunnel.config.ConfigLoader;
import lt.esdc.tunnel.config.TrainConfig;
import lt.esdc.tunnel.resource.Direction;
import lt.esdc.tunnel.train.Train;
import lt.esdc.tunnel.resource.Tunnel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class TrainDispatcher {
    private static final Logger LOGGER = LogManager.getLogger(TrainDispatcher.class);
    private static final int TUNNEL_COUNT = 2;

    public static void main(String[] args) {
        try {
            AppConfig config = ConfigLoader.loadConfig();
            LOGGER.info("Configuration loaded successfully.");

            Tunnel tunnel1 = new Tunnel(config.getTunnel().getCapacity(), config.getTunnel().getMaxTrainsInDirection());
            Tunnel tunnel2 = new Tunnel(config.getTunnel().getCapacity(), config.getTunnel().getMaxTrainsInDirection());
            Tunnel[] tunnels = {tunnel1, tunnel2};

            List<Future<Boolean>> results = new ArrayList<>();

            try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {

                for (int i = 0; i < config.getTrains().size(); i++) {
                    TrainConfig tc = config.getTrains().get(i);

                    if (tc.getArrivalTimeDelayMs() > 0) {
                        TimeUnit.MILLISECONDS.sleep(tc.getArrivalTimeDelayMs());
                    }

                    Tunnel assignedTunnel = tunnels[i % TUNNEL_COUNT];
                    Train train = new Train(Direction.valueOf(tc.getDirection().toUpperCase()), assignedTunnel, config.getTunnel().getTrainTravelTimeSeconds());

                    LOGGER.info("Dispatcher: Submitting train task to Tunnel {}", (i % TUNNEL_COUNT + 1));

                    Future<Boolean> result = executor.submit(train);
                    results.add(result);
                }
            }
            processResults(results);

            LOGGER.info("Simulation finished. All trains have passed.");

        } catch (Exception e) {
            LOGGER.error("Application error: ", e);
        }
    }

    private static void processResults(List<Future<Boolean>> results) {
        int successCount = 0;
        int failCount = 0;

        for (Future<Boolean> future : results) {
            try {
                if (future.get()) {
                    successCount++;
                } else {
                    failCount++;
                }
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error("Error retrieving train result", e);
                failCount++;
            }
        }

        LOGGER.info("--- Statistics ---");
        LOGGER.info("Total trains: {}", results.size());
        LOGGER.info("Successful passages: {}", successCount);
        LOGGER.info("Failed/Interrupted: {}", failCount);
    }
}