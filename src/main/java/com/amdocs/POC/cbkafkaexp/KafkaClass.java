package com.amdocs.POC.cbkafkaexp;

//import com.couchbase.client.deps.com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.ArrayList;

//@JsonSerialize(using = KafkaClassSerializer.class)
public class KafkaClass {
    public String fullname;
    public int yearofbirth;
    public ArrayList<ProductClass> products; // An array of product object (will be taken from the product documents).
}
