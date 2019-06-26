package com.amdocs.POC.cbkafkaexp;

import com.couchbase.client.deps.com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = ProductDeserializer.class)
public class ProductClass {
    public int productid;
    public String name;
    public String type = "product";
}
