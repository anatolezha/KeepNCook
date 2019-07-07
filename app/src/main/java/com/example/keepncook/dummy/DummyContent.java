package com.example.keepncook.dummy;

import java.util.Date;

public class DummyContent {

    /**
     * A dummy item representing a piece of content.
     */
    public static class DummyItem {
        public  String id;
        public  String name;
        public Date expiration_date;

        public DummyItem() {
        }

        public String getId() {
            return id;
        }

        public String getContent() {
            return name;
        }

        public Date getDetails() {
            return expiration_date;
        }

        public DummyItem(String id, String content, Date details) {
            this.id = id;
            this.name = content;
            this.expiration_date = details;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
