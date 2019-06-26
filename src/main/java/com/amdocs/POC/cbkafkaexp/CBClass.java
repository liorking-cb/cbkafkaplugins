package com.amdocs.POC.cbkafkaexp;


//import com.couchbase.client.deps.com.fasterxml.jackson.databind.annotation.JsonDeserialize;

//@JsonDeserialize(using = CBClassDeserializer.class)
public class CBClass {

    public String firstname;
    public String lastname;
    public int age;
    public String[] products; // array of product IDs that are document keys.
    public String type = "customer";
}
