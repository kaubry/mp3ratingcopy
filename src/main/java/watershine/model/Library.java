package watershine.model;

import java.util.List;

/**
 * Created by kevin on 27.06.2017.
 */
public class Library {

    private List<Playlist> playlists;
    private List<Song> songs;

    public List<Playlist> getPlaylists() {
        return playlists;
    }

    public void setPlaylists(List<Playlist> playlists) {
        this.playlists = playlists;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }
}
