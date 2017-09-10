package watershine.model;

import watershine.Mp3Tag;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kevin on 23.06.2017.
 */
public class Song {

    private String songFileURI;
    private int starRating;
    private int id;
    private Map<Mp3Tag, String> tags;

    public Song() {
        this.tags = new HashMap<>();
    }

    public String getSongFileURI() {
        return songFileURI;
    }

    public void setSongFileURI(String songFileURI) {
        this.songFileURI = songFileURI;
    }

    public int getStarRating() {
        return starRating;
    }

    public void setStarRating(int starRating) {
        this.starRating = starRating;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTag(Mp3Tag tag, String value) {
        tags.put(tag, value);
    }

    public String getTag(Mp3Tag tag) {
        return tags.get(tag);
    }
}
