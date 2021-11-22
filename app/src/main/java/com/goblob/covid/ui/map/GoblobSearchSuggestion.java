package com.goblob.covid.ui.map;

import android.os.Parcel;

import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

/**
 * Created by edel on 26/12/17.
 */

public class GoblobSearchSuggestion implements SearchSuggestion {
    private String search;
    private GoblobSearchType searchOn;
    private String id;
    private int count;

    public GoblobSearchSuggestion(String id, String suggestion, String searchOn) {
        this.id = id;
        this.search = suggestion.toLowerCase();
        this.searchOn = GoblobSearchType.valueOf(searchOn.toLowerCase().toUpperCase());
        this.count = 0;
    }

    public GoblobSearchSuggestion(String id, String suggestion, String searchOn, int count) {
        this.id = id;
        this.search = suggestion.toLowerCase();
        this.searchOn = GoblobSearchType.valueOf(searchOn.toLowerCase().toUpperCase());
        this.count = count;
    }

    public String getId() {
        return id;
    }

    public GoblobSearchSuggestion(Parcel source) {
        this.search = source.readString();
        this.searchOn = GoblobSearchType.valueOf(source.readString().toUpperCase());
    }

    public String getSearch() {
        return search;
    }

    public GoblobSearchType getSearchOn() {
        return searchOn;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public void setSearchOn(GoblobSearchType searchOn) {
        this.searchOn = searchOn;
    }

    @Override
    public String getBody() {
        return search;
    }

    public static final Creator<GoblobSearchSuggestion> CREATOR = new Creator<GoblobSearchSuggestion>() {
        @Override
        public GoblobSearchSuggestion createFromParcel(Parcel in) {
            return new GoblobSearchSuggestion(in);
        }

        @Override
        public GoblobSearchSuggestion[] newArray(int size) {
            return new GoblobSearchSuggestion[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(search);
        dest.writeString(searchOn.toString());
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
