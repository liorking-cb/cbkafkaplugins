package com.amdocs.POC.cbkafkaexp;

import com.couchbase.client.deps.com.fasterxml.jackson.core.JsonParser;
import com.couchbase.client.deps.com.fasterxml.jackson.core.JsonProcessingException;
import com.couchbase.client.deps.com.fasterxml.jackson.databind.DeserializationContext;
import com.couchbase.client.deps.com.fasterxml.jackson.databind.JsonNode;
import com.couchbase.client.deps.com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.couchbase.client.deps.com.fasterxml.jackson.databind.node.IntNode;

import java.io.IOException;

public class ProductDeserializer extends StdDeserializer<ProductClass> {

    public ProductDeserializer() {
        this(null);
    }

    public ProductDeserializer(Class<ProductClass> vc) {
        super(vc);
    }

    @Override
    public ProductClass deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {

        JsonNode node = jp.getCodec().readTree(jp);
        ProductClass prod = new ProductClass();

        prod.name  = node.get("name").asText();
        prod.productid = (Integer) ((IntNode) node.get("productid")).numberValue();

        return prod;
    }
}
