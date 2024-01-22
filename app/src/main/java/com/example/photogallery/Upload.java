package com.example.photogallery;


public class Upload {
    private String mName;
    private String mImageUrl;
    private String mKey;
    private String mQuote;
    private String mLocation;
    private int id;
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

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
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
        mLocation = location;
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

}
