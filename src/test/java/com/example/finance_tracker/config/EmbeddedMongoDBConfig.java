//package com.example.finance_tracker.config;
//
//import io.github.mongomemoryserver.MongoMemoryServer;
//
//public class EmbeddedMongoDBConfig {
//
//    private static MongoMemoryServer mongoMemoryServer;
//
//    public static void start() {
//        mongoMemoryServer = new MongoMemoryServer();
//    }
//
//    public static void stop() {
//        if (mongoMemoryServer != null) {
//            mongoMemoryServer.close();
//        }
//    }
//
//    public static String getConnectionUri() {
//        return mongoMemoryServer.getUri();
//    }
//}
