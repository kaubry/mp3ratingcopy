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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RatingCopyProcessor {

    private List<ProcessFileProgressListener> progressListeners = new ArrayList<>();
    private List<MessageListener> messageListeners = new ArrayList<>();

    public List<Message> copyRatingsIntoMp3Tag(List<Song> songs, Mp3Tag tag, boolean override) {
        List<Message> errors = new ArrayList();
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
                    errors.add(new Message(Message.MessageLevel.ERROR, "File " + filePath + " is not Id3v2 compatible"));
                }
            } catch (UnsupportedTagException | InvalidDataException | NotSupportedException e) {
                Message m = new Message(Message.MessageLevel.ERROR, e.getMessage() + " for: " + filePath);
                notifyMessageListener(m);
//                errors.add();
            } catch (FileNotFoundException e) {
                errors.add(new Message(Message.MessageLevel.ERROR, "File" + filePath + " can't be found"));
            } catch (IOException e) {
                errors.add(new Message(Message.MessageLevel.ERROR, e.getMessage() + " for: " + filePath));
            }
            fileProcessed++;
            notifyProgressListener(fileProcessed, nbrOfFileToProcess);
        }
        return errors;
    }

    public static List<Song> getSongToProcess(List<Song> songs, Mp3Tag tag, boolean override) {
        List<Song> returnList;
        if (override) {
            returnList = songs;
        } else {
            returnList = songs.stream().filter(e -> StringUtils.isEmpty(e.getTag(tag))).collect(Collectors.toList());
        }
        return returnList.stream().filter(s -> s.getSongFileURI().endsWith(".mp3")).filter(e -> !getStarsInUnicode(e.getStarRating()).equals(e.getTag(tag))).collect(Collectors.toList());
    }

    public static String getStarsInUnicode(int rating) {
        int numberOfStars = rating / 20;
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < numberOfStars; i++) {
            stringBuilder.append("★");
        }
        return stringBuilder.toString();
    }

    private void notifyProgressListener(int fileProcessed, int fileToProcess) {
        for (ProcessFileProgressListener listener : progressListeners) {
            listener.progress(fileProcessed, fileToProcess);
        }
    }

    private void notifyMessageListener(Message m) {
        messageListeners.stream().forEach(l -> l.updateMessages(Arrays.asList(m)));
    }

    public void addProgressListener(ProcessFileProgressListener listener) {
        progressListeners.add(listener);
    }

    public void removeProgressListener(ProcessFileProgressListener listener) {
        progressListeners.remove(listener);
    }

    public void addMessageListener(MessageListener listener) {
        messageListeners.add(listener);
    }

    public void removeMessageListener(MessageListener listener) {
        messageListeners.remove(listener);
    }
}
