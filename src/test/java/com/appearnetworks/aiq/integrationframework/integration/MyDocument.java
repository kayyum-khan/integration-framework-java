package com.appearnetworks.aiq.integrationframework.integration;

public class MyDocument extends BusinessDocument {
    private String foo;

    public MyDocument() { }

    public MyDocument(String _id, String _type, long _rev, String foo) {
        super(_id, _type, _rev);
        this.foo = foo;
    }

    public String getFoo() {
        return foo;
    }
}
