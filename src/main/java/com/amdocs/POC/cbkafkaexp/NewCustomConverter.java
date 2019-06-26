package com.amdocs.POC.cbkafkaexp;


import com.couchbase.client.dcp.message.MessageUtil;
import com.couchbase.client.deps.com.fasterxml.jackson.databind.ObjectMapper;
import com.couchbase.client.deps.io.netty.buffer.ByteBuf;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.connect.kafka.dcp.EventType;
import com.couchbase.connect.kafka.handler.source.CouchbaseSourceRecord;
import com.couchbase.connect.kafka.handler.source.DocumentEvent;
import com.couchbase.connect.kafka.handler.source.RawJsonSourceHandler;
import com.couchbase.connect.kafka.handler.source.SourceHandlerParams;
import com.couchbase.connect.kafka.transform.DeserializeJson;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.apache.kafka.connect.data.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Year;
import java.util.ArrayList;


/**
 * This handler propagates JSON documents from Couchbase to Kafka verbatim with no schema.
 * That is, the Kafka message will be identical to the content of the Couchbase document.
 * Deletions are propagated as a message with a {@code null} value.
 * Modifications to non-JSON documents are not propagated.
 * <p>
 * The key of the Kafka message is the ID of the Couchbase document.
 * <p>
 * The value of the generated ConnectRecord is a byte array.
 * If there are no downstream transforms, configure the connector like this
 * for maximum efficiency:
 * <pre>
 * dcp.message.converter.class=com.couchbase.connect.kafka.handler.source.RawJsonSourceHandler
 * value.converter=org.apache.kafka.connect.converters.ByteArrayConverter
 * </pre>
 * If you wish to use Single Message Transforms with this handler, the first transform
 * must be {@link DeserializeJson} to convert the
 * byte array to a Map that downstream transforms can work with. Like this:
 * <pre>
 * dcp.message.converter.class=com.couchbase.connect.kafka.handler.source.RawJsonSourceHandler
 * value.converter=org.apache.kafka.connect.json.JsonConverter
 * value.converter.schemas.enable=false
 * transforms=deserializeJson,ignoreDeletes,addField
 * transforms.deserializeJson.type=com.couchbase.connect.kafka.transform.DeserializeJson
 * transforms.ignoreDeletes.type=com.couchbase.connect.kafka.transform.DropIfNullValue
 * transforms.addField.type=org.apache.kafka.connect.transforms.InsertField$Value
 * transforms.addField.static.field=magicWord
 * transforms.addField.static.value=xyzzy
 * </pre>
 *
 * see RawJsonWithMetadataSourceHandler
 */

// Based on RawJsonSourceHandler

public class NewCustomConverter extends com.couchbase.connect.kafka.handler.source.SourceHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RawJsonSourceHandler.class);

    private static final JsonFactory jsonFactory = new JsonFactory();

    protected static boolean isValidJson(byte[] bytes) {
        try {
            final JsonParser parser = jsonFactory.createParser(bytes);
            final JsonToken firstToken = parser.nextToken();

            final JsonToken incrementDepthToken;
            final JsonToken decrementDepthToken;

            if (firstToken == JsonToken.START_OBJECT) {
                incrementDepthToken = JsonToken.START_OBJECT;
                decrementDepthToken = JsonToken.END_OBJECT;

            } else if (firstToken == JsonToken.START_ARRAY) {
                incrementDepthToken = JsonToken.START_ARRAY;
                decrementDepthToken = JsonToken.END_ARRAY;

            } else {
                // valid if there's exactly one token.
                return firstToken != null && parser.nextToken() == null;
            }

            int depth = 1;
            JsonToken token;
            while ((token = parser.nextToken()) != null) {
                if (token == incrementDepthToken) {
                    depth++;
                } else if (token == decrementDepthToken) {
                    depth--;
                    if (depth == 0 && parser.nextToken() != null) {
                        // multiple JSON roots, or trailing garbage
                        return false;
                    }
                }
            }
        } catch (IOException e) {
            // malformed
            return false;
        }

        return true;
    }

    @Override
    public CouchbaseSourceRecord handle(SourceHandlerParams params) {
        final CouchbaseSourceRecord.Builder builder = CouchbaseSourceRecord.builder();

        if (!passesFilter(params)) {
            return null;
        }

        if (!buildValue(params, builder)) {
            return null;
        }

        return builder.topic(getTopic(params))
                .key(Schema.STRING_SCHEMA, params.documentEvent().key())
                .build();
    }

    protected boolean passesFilter(SourceHandlerParams params) {
        return true;
    }

    protected boolean buildValue(SourceHandlerParams params, CouchbaseSourceRecord.Builder builder) {
        final DocumentEvent docEvent = params.documentEvent();
        final ByteBuf event = docEvent.rawDcpEvent();

        final EventType type = EventType.of(event);
        if (type == null) {
            LOGGER.warn("unexpected event type {}", event.getByte(1));
            return false;
        }

        switch (type) {
            case EXPIRATION:
            case DELETION:
                //builder.value(null, null);
                return true;

            case MUTATION:
                //LK
                final byte[] document = MessageUtil.getContentAsByteArray(event);

                if (!isValidJson(document)) {
                    LOGGER.warn("Skipping non-JSON document: bucket={} key={}", docEvent.bucket(), docEvent.key());
                    return false;
                }
                byte[] transdoc = transformDoc (document);

                if (transdoc != null)
                    //  streaming into Kafka topic.
                    builder.value(null, transdoc);
                return true;

            default:
                LOGGER.warn("unexpected event type {}", event.getByte(1));
                return false;
        }
    }

    private byte[] transformDoc(byte[] document) {
        try {
            //Deserialize the document into a CBClass object.
            CBClass cbClass = new ObjectMapper().readValue(document, CBClass.class);

            if (filterDocument(cbClass))
                return null;


            // map to a Kafka class
            KafkaClass kafkaClass = new KafkaClass();
            kafkaClass.fullname = cbClass.firstname + " " + cbClass.lastname;
            kafkaClass.yearofbirth =  Year.now().getValue() - cbClass.age;
            kafkaClass.products = new ArrayList<ProductClass>();
            Bucket bucket = CouchbaseInstance.INSTANCE.getBucket();
            if (bucket == null)
                return null;

            ProductClass prod;
            ArrayList<ProductClass> products = new ArrayList<ProductClass>();

            // Iterate over all the products in the array and get the product details.
            for (int i=0; i< cbClass.products.length; i++) {
                // Get the product document by the product document key
                JsonDocument doc = bucket.get(cbClass.products[i]);

                //deserialize the product document into a product object
                prod = new ObjectMapper().readValue(doc.content().toString(), ProductClass.class);
                kafkaClass.products.add(prod);
            }
            // Serialize the kafka class object into a byte array
            byte[] result = new ObjectMapper().writeValueAsBytes(kafkaClass);
            return result;

        }
        catch (IOException e) {
            LOGGER.error(e.getMessage());
            return null;
        }

    }

    private boolean filterDocument(CBClass cbclass) {

        // Filter condition for test:
        //   Don't send to Kafka if the age is 100 and above or if the doc type is not "customer".
        if (cbclass.age >= 100 || !(cbclass.type.equals("customer")))
            return true; // filter out the document

        return false; // document is fine and will not be filtered out
    }

    protected String getTopic(SourceHandlerParams params) {
        // Alter the topic based on document key / content:
        //
        // if (params.documentEvent().key().startsWith("xyzzy")) {
        //     return params.topic() + "-xyzzy";
        // }

        // Or use the default topic
        return null;
    }
}