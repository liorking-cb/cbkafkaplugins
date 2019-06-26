package com.amdocs.POC.cbkafkaexp;

import com.couchbase.client.deps.com.fasterxml.jackson.core.JsonParser;
import com.couchbase.client.deps.com.fasterxml.jackson.core.JsonProcessingException;
import com.couchbase.client.deps.com.fasterxml.jackson.databind.DeserializationContext;
import com.couchbase.client.deps.com.fasterxml.jackson.databind.JsonNode;
import com.couchbase.client.deps.com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.couchbase.client.deps.com.fasterxml.jackson.databind.node.IntNode;

import java.io.IOException;
/*
public class CBClassDeserializer extends StdDeserializer<CBClass> {

    public CBClassDeserializer() {
        this(null);
    }

    public CBClassDeserializer(Class<CBClass> vc) {
        super(vc);
    }

    @Override
    public CBClass deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {

        JsonNode node = jp.getCodec().readTree(jp);
        CBClass cb = new CBClass();

        cb.firstname  = node.get("firstname").asText();
        cb.lastname = node.get("lastname").asText();
        cb.age = (Integer) ((IntNode) node.get("age")).numberValue();

        return cb;

    }
}

*/