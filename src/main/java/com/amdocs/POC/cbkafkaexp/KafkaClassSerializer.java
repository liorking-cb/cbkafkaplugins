package com.amdocs.POC.cbkafkaexp;


import com.couchbase.client.deps.com.fasterxml.jackson.core.JsonGenerator;
import com.couchbase.client.deps.com.fasterxml.jackson.databind.SerializerProvider;
import com.couchbase.client.deps.com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.Calendar;

/*
public class KafkaClassSerializer extends StdSerializer<KafkaClass> {

    public KafkaClassSerializer() {
        this(null);
    }

    public KafkaClassSerializer(Class<KafkaClass> t) {
        super(t);
    }

    @Override
    public void serialize(KafkaClass doc, JsonGenerator jsonGenerator, SerializerProvider serializer)
            throws IOException {

        int year = Calendar.getInstance().get(Calendar.YEAR);

        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("full name", doc.fullname);
        jsonGenerator.writeNumberField("yearofbirth", doc.yearofbirth);
        jsonGenerator.writeEndObject();
    }
}
*/