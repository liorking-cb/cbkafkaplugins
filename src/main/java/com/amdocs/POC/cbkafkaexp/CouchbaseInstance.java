package com.amdocs.POC.cbkafkaexp;

import com.couchbase.client.core.CouchbaseException;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.CouchbaseCluster;

import com.couchbase.connect.kafka.handler.source.RawJsonSourceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Couchbase Instance - a singleton holding the cluster and bucket objects
 */

public enum CouchbaseInstance {

    INSTANCE;
    private CouchbaseCluster cluster = null;
    private Bucket bucket = null;
    private String host = "127.0.0.1";
    private String bucketname = "KafkaPOC";
    private String username = "Administrator";
    private String password = "admini";

    private Logger logger = LoggerFactory.getLogger(RawJsonSourceHandler.class);

    CouchbaseInstance() {
        try {
                cluster = getCluster();
                cluster.authenticate(username, password);
                bucket = getbucket();
                logger.info("Couchbase connection created successfully");
            }
        catch (CouchbaseException ex) {
            logger.error("ERROR while connecting to Couchbase", ex);
        }
    }

    private synchronized CouchbaseCluster getCluster() {
        //String host = PropertyFileReader.getValue("COUCHBASE_HOST");

        return CouchbaseCluster.create(host);

    }

    private synchronized Bucket getbucket() {
        //String host = PropertyFileReader.getValue("COUCHBASE_HOST");
        return cluster.openBucket(bucketname);
    }
    public Bucket getBucket() {
        if (cluster == null) {
            logger.warn("Couchbase cluster is null");
            cluster = getCluster();
        }
        if (bucket == null) {
            logger.warn("Couchbase bucket is null");
            bucket = getbucket();
        }
        return bucket;
    }
}