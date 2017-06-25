package watershine.itunes.model;

import watershine.itunes.jax2b.SongLibrary;

/**
 * Created by kevin on 24.06.2017.
 */
public class ITunesSongLibrary {

    private SongLibrary songLibrary;

    public ITunesSongLibrary(SongLibrary songLibrary) {
        this.songLibrary = songLibrary;
    }

    public Object getValue(String key) {
        if (!this.songLibrary.getDict().getKeys().contains(key)) {
            return null;
        }
        int index = this.songLibrary.getDict().getKeys().indexOf(key);
        try {
            return this.songLibrary.getDict().getValues().get(index);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return null;
    }
}
