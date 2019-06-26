package com.amdocs.POC.cbkafkaexp;


import com.couchbase.client.deps.io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;


public class customfilter implements com.couchbase.connect.kafka.filter.Filter {

    @Override
    public boolean pass(final ByteBuf message) {
        String actualString = message.toString(0, message.readableBytes(), StandardCharsets.UTF_8);
        if (actualString.contains("\"type\": \"customer\""))
            return true;
        return false;
    }

}

