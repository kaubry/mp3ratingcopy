package watershine;


import watershine.model.Song;

import java.util.List;

public class RatingCopyTask implements Runnable {

    private RatingCopyProcessor ratingCopyProcessor;
    private List<Song> songs;
    private Mp3Tag tag;
    private boolean override;

    public RatingCopyTask(RatingCopyProcessor ratingCopyProcessor, List<Song> songs, Mp3Tag tag, boolean override) {
        this.ratingCopyProcessor = ratingCopyProcessor;
        this.songs = songs;
        this.tag = tag;
        this.override = override;
    }

    @Override
    public void run() {
        ratingCopyProcessor.copyRatingsIntoMp3Tag(songs, tag, override);
    }
}
