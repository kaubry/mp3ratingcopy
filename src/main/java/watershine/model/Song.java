package watershine.model;

/**
 * Created by kevin on 23.06.2017.
 */
public class Song {

    private String songFileURI;
    private int starRating;

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
}
