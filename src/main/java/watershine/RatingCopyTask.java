package watershine;


import watershine.model.Song;

import java.util.ArrayList;
import java.util.List;

public class RatingCopyTask implements Runnable {

    RatingCopyProcessor ratingCopyProcessor;
    List<Song> songs;
    Mp3Tag tag;

    public RatingCopyTask(RatingCopyProcessor ratingCopyProcessor, List<Song> songs, Mp3Tag tag) {
        this.ratingCopyProcessor = ratingCopyProcessor;
        this.songs = songs;
        this.tag = tag;
    }

    @Override
    public void run() {
        ratingCopyProcessor.copyRatingsIntoMp3Tag(songs, tag);
    }
}
