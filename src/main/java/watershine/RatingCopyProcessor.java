package watershine;

import com.mpatric.mp3agic.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.stereotype.Component;
import watershine.model.Song;

import java.io.File;
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

    private List<ProcessFileProgressListener> progressListeners = new ArrayList<>();

    public void copyRatingsIntoMp3Tag(List<Song> songs, Mp3Tag tag, boolean override) {
        String tempDir = System.getProperty("java.io.tmpdir");
        File tempDirectory = new File(tempDir + File.separator + "rating_copy");
        tempDirectory.mkdir();
        tempDirectory.deleteOnExit();
        List<Song> toProcess = getSongToProcess(songs, tag, override);
        int fileProcessed = 0;
        int nbrOfFileToProcess = toProcess.size();
        notifyProgressListener(fileProcessed, nbrOfFileToProcess);
        for (Song song : toProcess) {
            String filePath = URI.create(song.getSongFileURI()).getPath();
            if (SystemUtils.IS_OS_WINDOWS && filePath.startsWith("/")) {
                filePath = filePath.substring(1, filePath.length());
            }
            try {
                Mp3File mp3File = new Mp3File(filePath);
                if (mp3File.hasId3v2Tag()) {
                    switch (tag) {
                        case COMMENT:
                            mp3File.getId3v2Tag().setComment(getStarsInUnicode(song.getStarRating()));
                            if (mp3File.getId3v1Tag() != null) {
                                mp3File.getId3v1Tag().setComment(getStarsInUnicode(song.getStarRating()));
                            }
                            break;
                        case COMPOSER:
                            String composer = getStarsInUnicode(song.getStarRating());
                            if (StringUtils.isEmpty(composer)) {
                                mp3File.getId3v2Tag().clearFrameSet("TCOM");
                            } else
                                mp3File.getId3v2Tag().setComposer(composer);
                            break;
                    }
                    String tempMp3 = tempDirectory.getPath() + File.separator + new File(mp3File.getFilename()).getName() + ".new";
                    mp3File.save(tempMp3);
                    Files.move(Paths.get(tempMp3), Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);
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
            fileProcessed++;
            notifyProgressListener(fileProcessed, nbrOfFileToProcess);
        }
    }

    public static List<Song> getSongToProcess(List<Song> songs, Mp3Tag tag, boolean override) {
        List<Song> returnList;
        if (override) {
            returnList = songs;
        } else {
            returnList = songs.stream().filter(e -> StringUtils.isEmpty(e.getTag(tag))).collect(Collectors.toList());
        }
        return returnList.stream().filter(e -> !getStarsInUnicode(e.getStarRating()).equals(e.getTag(tag))).collect(Collectors.toList());
    }

    public static String getStarsInUnicode(int rating) {
        int numberOfStars = rating / 20;
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < numberOfStars; i++) {
            stringBuilder.append("*");
        }
        return stringBuilder.toString();
    }

    private void notifyProgressListener(int fileProcessed, int fileToProcess) {
        for (ProcessFileProgressListener listener : progressListeners) {
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
