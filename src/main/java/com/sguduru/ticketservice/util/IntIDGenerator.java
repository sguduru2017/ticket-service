package com.sguduru.ticketservice.util;

import java.util.concurrent.atomic.AtomicInteger;

public final class IntIDGenerator {

    private static final AtomicInteger sequence = new AtomicInteger(1);

    private IntIDGenerator() {}

    public static int generate(){
        return sequence.getAndIncrement();
    }

}