package watershine.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by kevin on 27.06.2017.
 */
public class Playlist {

    private String name;
    private List<Integer> tracks = new ArrayList<>();
    private String persistentId;
    private String parentPersistentId;
    private boolean folder = false;
    private List<Playlist> children= new ArrayList<>();
    private Playlist parent;

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

    public String getPersistentId() {
        return persistentId;
    }

    public void setPersistentId(String persistentId) {
        this.persistentId = persistentId;
    }

    public String getParentPersistentId() {
        return parentPersistentId;
    }

    public void setParentPersistentId(String parentPersistentId) {
        this.parentPersistentId = parentPersistentId;
    }

    public boolean isFolder() {
        return folder;
    }

    public void setFolder(boolean folder) {
        this.folder = folder;
    }

    public void addChild(Playlist playlist) {
        this.children.add(playlist);
    }

    public List<Playlist> getChildren() {
        return children;
    }

    public void removeChild(Playlist playlist) {
        this.children.remove(playlist);
    }

    public Playlist getParent() {
        return parent;
    }

    public void setParent(Playlist parent) {
        this.parent = parent;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(getName());
        if(getParent() != null) {
            builder.insert(0, " -> ");
            builder.insert(0, getParent().toString());
        }

        return builder.toString();
    }

    public Stream<Playlist> flattened() {
        return Stream.concat(
                Stream.of(this),
                children.stream().flatMap(Playlist::flattened));
    }
}
