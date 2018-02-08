package watershine.gui;

import javafx.scene.control.ComboBox;
import watershine.model.Playlist;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by kevin on 27.06.2017.
 */
public class PlayListComboBox extends ComboBox<Playlist> {

    private List<Playlist> playlists;

    public PlayListComboBox() {
        this.setDisable(true);
    }

    public void updatePlaylist(List<Playlist> pl) {
        this.getItems().removeAll(this.getItems());
        playlists = pl.stream().flatMap(Playlist::flattened).filter(p -> !p.isFolder() && p.getTracks() != null && p.getTracks().size() > 0).collect(Collectors.toList());
        if(playlists.size() > 0) {
            this.setDisable(false);
        }
        this.getItems().addAll(playlists);
    }
}
