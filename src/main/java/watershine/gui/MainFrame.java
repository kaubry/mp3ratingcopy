package watershine.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import watershine.Mp3Tag;
import watershine.ProcessFileProgressListener;
import watershine.RatingCopyProcessor;
import watershine.RatingCopyTask;
import watershine.itunes.ITunesXMLParser;
import watershine.model.Library;
import watershine.model.Playlist;
import watershine.model.Song;

import javax.annotation.PreDestroy;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Component
class MainFrameController implements ProcessFileProgressListener {

    @Autowired
    private ITunesXMLParser iTunesXMLParser;
    @Autowired
    private RatingCopyProcessor ratingCopyProcessor;
    @Autowired
    private TaskExecutor taskExecutor;

    @FXML
    public Label itunesFileLabel;
    @FXML
    public ComboBox<String> tagsCombo;
    @FXML
    private PlayListComboBox playlistComboBox;
    @FXML
    private CheckBox override;
    @FXML
    private ProgressBar progressBar;
    @FXML
    public Button startButton;

    private FileChooser fc;
    private Library library;
    private File selectedFile;

    private boolean tagSelected;
    private boolean playlistSelected;

    @FXML
    private void initialize() {
        this.selectedFile = getDefaultItunesXMLFile();
        updateSelectedFileName();
        initFileChooser();
        initTagsCombo();
        ratingCopyProcessor.addProgressListener(this);
        if (selectedFile != null)
            updateLibrary();

    }

    private void updateSelectedFileName() {
        if (selectedFile != null) {
            itunesFileLabel.setText(this.selectedFile.getPath());
            enableStartButton();
        } else {
            itunesFileLabel.setText("");
        }
    }

    @PreDestroy
    private void destroy() {
        ratingCopyProcessor.removeProgressListener(this);
    }

    private void updateLibrary() {
        try {
            library = iTunesXMLParser.getLibrary(this.selectedFile.getPath());
            playlistComboBox.updatePlaylist(library.getPlaylists());
        } catch (JAXBException | IOException | XMLStreamException e) {
            e.printStackTrace();
        }
    }

    private void initTagsCombo() {
        String[] tags = Arrays.stream(Mp3Tag.values()).map(s -> s.tagName()).toArray(String[]::new);
        tagsCombo.getItems().addAll(tags);
    }

    private void initFileChooser() {
        fc = new FileChooser();
        fc.setTitle("Select iTunes XML Library File");
        fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("XML Files (*.xml)", "*.xml"));
    }

    @FXML
    private void chooseITunesFile() {
        File file = fc.showOpenDialog(null);
        if (file != null) {
            this.selectedFile = file;
            updateLibrary();
            updateSelectedFileName();
        } else {

        }
    }

    @FXML
    private void startCopyRating() {
        Mp3Tag selectedTag = Mp3Tag.getEnum(tagsCombo.getValue());
        Playlist selectedPlaylist = this.playlistComboBox.getValue();
        if (selectedFile == null)
            return;
        List<Song> songs = getSongsFromPlaylist(library.getSongs(), selectedPlaylist);
        String message = RatingCopyProcessor.getSongToProcess(songs, selectedTag, override.isSelected()).size() + " Songs will be processed";
        String title = "Warning";
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText(title);
        alert.setContentText(message);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            taskExecutor.execute(new RatingCopyTask(ratingCopyProcessor, songs, selectedTag, override.isSelected()));
        }
    }

    @Override
    public void progress(int nbrOfFileProcessed, int totalNbrOfFile) {
        if (nbrOfFileProcessed == 0) {
            this.progressBar.setProgress(0);
        } else {
            this.progressBar.setProgress((double)totalNbrOfFile/(double)nbrOfFileProcessed);
        }
    }

    private File getDefaultItunesXMLFile() {
        String homePath = System.getProperty("user.home");
        if (SystemUtils.IS_OS_WINDOWS || SystemUtils.IS_OS_MAC_OSX) {
            File iTunesXml = new File(String.join(File.separator, homePath, "Music", "iTunes", "iTunes Music Library.xml"));
            if (iTunesXml.exists()) {
                return iTunesXml;
            }
        }
        return null;
    }

    private List<Song> getSongsFromPlaylist(List<Song> allSongs, Playlist playlist) {
        if (playlist == null)
            return allSongs;
        return allSongs.stream().filter(s -> playlist.getTracks().contains(s.getId())).collect(Collectors.toList());
    }

    @FXML
    public void tagsSelected() {
        tagSelected = true;
        enableStartButton();
    }

    @FXML
    public void playlistSelected() {
        playlistSelected = true;
        enableStartButton();
    }

    private void enableStartButton() {
        if(playlistSelected && tagSelected && selectedFile != null)
            startButton.setDisable(false);
    }
}
