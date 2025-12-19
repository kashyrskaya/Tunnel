package lt.esdc.tunnel.resource;

import lt.esdc.tunnel.train.Train;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Tunnel {
    private static final Logger LOGGER = LogManager.getLogger(Tunnel.class);

    private final int maxCapacity;
    private final int maxInRow;

    private final ReentrantLock stateLock = new ReentrantLock();
    private final Condition waitNorth = stateLock.newCondition();
    private final Condition waitSouth = stateLock.newCondition();

    private TunnelState currentState;

    private int currentOccupancy = 0;
    private int consecutiveCount = 0;

    public Tunnel(int maxCapacity, int maxInRow) {
        this.maxCapacity = maxCapacity;
        this.maxInRow = maxInRow;
        this.currentState = new FreeState();
    }

    public boolean enterTunnel(Train train) throws InterruptedException {
        stateLock.lock();
        try {
            while (true) {
                if (currentState.enter(train)) {
                    return true;
                }
            }
        } finally {
            stateLock.unlock();
        }
    }

    public void exitTunnel(Train train) {
        stateLock.lock();
        try {
            currentState.exit(train);
        } finally {
            stateLock.unlock();
        }
    }

    private void commonExitLogic(Train train, Direction currentDir) {
        LOGGER.info("{} exiting tunnel (direction {}).", train, currentDir);

        currentOccupancy--;

        if (currentOccupancy == 0) {
            currentState = new FreeState();
            consecutiveCount = 0;
            LOGGER.info("State changed: {} -> FREE", currentDir);

            waitNorth.signalAll();
            waitSouth.signalAll();
        } else {
            if (consecutiveCount < maxInRow) {
                if (currentDir == Direction.NORTH) waitNorth.signalAll();
                else waitSouth.signalAll();
            }
        }
    }

    private interface TunnelState {
        boolean enter(Train train) throws InterruptedException;

        void exit(Train train);
    }

    private class FreeState implements TunnelState {
        @Override
        public boolean enter(Train train) {
            currentOccupancy++;
            consecutiveCount++;

            if (train.getDirection() == Direction.NORTH) {
                currentState = new NorthState();
                LOGGER.info("State changed: FREE -> NORTH_ACTIVE");
            } else {
                currentState = new SouthState();
                LOGGER.info("State changed: FREE -> SOUTH_ACTIVE");
            }
            return true;
        }

        @Override
        public void exit(Train train) {
            LOGGER.error("Logic error: Exit called on FreeState");
        }
    }

    private class NorthState implements TunnelState {
        @Override
        public boolean enter(Train train) throws InterruptedException {
            while (train.getDirection() != Direction.NORTH || consecutiveCount >= maxInRow || currentOccupancy >= maxCapacity) {
                waitNorth.await();

                if (!(currentState instanceof NorthState)) {
                    return false;
                }
            }

            currentOccupancy++;
            consecutiveCount++;
            return true;
        }

        @Override
        public void exit(Train train) {
            commonExitLogic(train, Direction.NORTH);
        }
    }

    private class SouthState implements TunnelState {
        @Override
        public boolean enter(Train train) throws InterruptedException {
            while (train.getDirection() != Direction.SOUTH || consecutiveCount >= maxInRow || currentOccupancy >= maxCapacity) {
                waitSouth.await();

                if (!(currentState instanceof SouthState)) {
                    return false;
                }
            }

            currentOccupancy++;
            consecutiveCount++;
            return true;
        }

        @Override
        public void exit(Train train) {
            commonExitLogic(train, Direction.SOUTH);
        }
    }
}