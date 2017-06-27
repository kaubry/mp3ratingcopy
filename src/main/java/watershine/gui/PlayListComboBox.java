package watershine.gui;

import watershine.model.Playlist;

import javax.swing.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by kevin on 27.06.2017.
 */
public class PlayListComboBox extends JComboBox<Playlist> {

    private List<Playlist> playlists;

    public PlayListComboBox() {
        this.setEnabled(false);
    }

    public void updatePlaylist(List<Playlist> playlists) {
        this.removeAllItems();
        this.playlists = playlists.stream().flatMap(Playlist::flattened).filter(p -> !p.isFolder() && p.getTracks() != null && p.getTracks().size() > 0).collect(Collectors.toList());
        if(this.playlists.size() > 0) {
            this.setEnabled(true);
        }
        updateContent();
    }

    private void updateContent() {
        for(Playlist playlist : this.playlists) {
            this.addItem(playlist);
        }
    }
}
