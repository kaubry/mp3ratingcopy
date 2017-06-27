package watershine.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kevin on 27.06.2017.
 */
public class Playlist {

    private String name;
    private List<Integer> tracks = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Integer> getTracks() {
        return tracks;
    }

    public void setTracks(List<Integer> tracks) {
        this.tracks = tracks;
    }

    public void addTrack(int trackId) {
        tracks.add(trackId);
    }
}
