package lt.esdc.tunnel.util;

import java.util.concurrent.atomic.AtomicInteger;

public class TrainIdGenerator {

    private final AtomicInteger nextId = new AtomicInteger(1);

    private TrainIdGenerator() {
    }

    public static TrainIdGenerator getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public int getNextId() {
        return nextId.getAndIncrement();
    }

    private static class SingletonHolder {
        public static final TrainIdGenerator INSTANCE = new TrainIdGenerator();
    }
}