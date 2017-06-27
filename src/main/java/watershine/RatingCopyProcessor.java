package watershine;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;
import org.springframework.stereotype.Component;
import watershine.model.Song;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RatingCopyProcessor {

    private final static String STAR_UNICODE = "\u2B50";
    private List<ProcessFileProgressListener> progressListeners = new ArrayList<>();

    public void copyRatingsIntoMp3Tag(List<Song> songs, Mp3Tag tag) {
        List<Song> toProcess = songs.stream().filter(e -> e.getStarRating() != 0).collect(Collectors.toList());
        int fileProcessed = 0;
        int fileToProcess = toProcess.size();
        notifyProgressListener(fileProcessed, fileToProcess);
        for (Song song : toProcess) {
            String filePath = URI.create(song.getSongFileURI()).getPath();
            if (filePath.startsWith("/")) {
                filePath = filePath.substring(1, filePath.length());
            }
            try {
                Mp3File mp3File = new Mp3File(filePath);
                if (mp3File.hasId3v2Tag()) {
                    boolean modified = false;
                    switch (tag) {
                        case COMMENT:
                            if (mp3File.getId3v2Tag().getComment() == null) {
                                mp3File.getId3v2Tag().setComment(getStarsInUnicode(song.getStarRating()));
                                modified = true;
                            }
                            break;
                        case COMPOSER:
                            if (mp3File.getId3v2Tag().getComposer() == null) {
                                mp3File.getId3v2Tag().setComposer(getStarsInUnicode(song.getStarRating()));
                                modified = true;
                            }
                            break;
                    }
                    if (modified) {
                        mp3File.save(filePath + ".new");
                        Files.move(Paths.get(filePath + ".new"), Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);
                    } else {
                        System.err.println("Not overriding composer field for file " + filePath);
                    }
                    fileToProcess--;
                    fileProcessed++;
                    notifyProgressListener(fileProcessed ,fileToProcess);

                } else {
                    System.err.println("File " + filePath + "is not Id3v2 compatible");
                }
            } catch (UnsupportedTagException | InvalidDataException | NotSupportedException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                System.err.println("File" + filePath + "can't be found");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getStarsInUnicode(int rating) {
        int numberOfStars = rating / 20;
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < numberOfStars; i++) {
            stringBuilder.append(STAR_UNICODE);
        }
        return stringBuilder.toString();
    }

    private void notifyProgressListener(int fileProcessed, int fileToProcess) {
        for(ProcessFileProgressListener listener : progressListeners) {
            listener.progress(fileProcessed, fileToProcess);
        }
    }

    public void addProgressListener(ProcessFileProgressListener listener) {
        progressListeners.add(listener);
    }

    public void removeProgressListener(ProcessFileProgressListener listener) {
        progressListeners.remove(listener);
    }
}
