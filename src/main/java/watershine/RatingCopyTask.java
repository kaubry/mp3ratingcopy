package watershine;


import javafx.concurrent.Task;
import watershine.model.Song;

import java.util.List;

public class RatingCopyTask extends Task<List<Message>> implements ProcessFileProgressListener, MessageListener {

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
    public List<Message> call() throws Exception {
        this.ratingCopyProcessor.addProgressListener(this);
        this.ratingCopyProcessor.addMessageListener(this);
        List<Message> errors = ratingCopyProcessor.copyRatingsIntoMp3Tag(songs, tag, override);
        this.ratingCopyProcessor.removeProgressListener(this);
        this.ratingCopyProcessor.removeMessageListener(this);
        return errors;
    }

    @Override
    public void progress(int nbrOfFileProcessed, int totalNbrOfFile) {
        updateProgress(nbrOfFileProcessed, totalNbrOfFile);
    }


    @Override
    public void updateMessages(List<Message> messages) {
        updateValue(messages);
    }
}
