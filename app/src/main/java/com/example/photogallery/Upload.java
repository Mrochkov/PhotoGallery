package com.example.photogallery;

import com.google.firebase.database.Exclude;

public class Upload {
    private String mName;
    private String mImageUrl;
    private String mKey;
    private String mQuote;
    private String mLocation;
    public Upload() {

    }


    public Upload(String name, String imageUrl, String quote, String location) {
        if (name.trim().equals("")) {
            name = "No Name";
        }
        mName = name;
        mImageUrl = imageUrl;
        mQuote = (quote == null) ? "Fetching quote..." : quote;
        mLocation = (location == null) ? "Fetching location..." : location;
    }

    public String getQuote() {
        return mQuote;
    }
    public void setQuote(String quote) {
        mQuote = quote;
    }
    public String getLocation() {
        return mLocation;
    }
    public void setLocation(String location) {
        mQuote = location;
    }
    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String imageUrl){
        mImageUrl = imageUrl;
    }

    @Exclude
    public String getKey(){
        return mKey;
    }
    @Exclude
    public void setKey(String key){
        mKey = key;
    }

}
