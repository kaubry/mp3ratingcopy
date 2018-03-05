package watershine.gui;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import watershine.*;
import watershine.itunes.ITunesXMLParser;
import watershine.model.Library;
import watershine.model.Playlist;
import watershine.model.Song;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Component
class MainFrameController {

    @Autowired
    private ITunesXMLParser iTunesXMLParser;
    @Autowired
    private RatingCopyProcessor ratingCopyProcessor;

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
    @FXML
    public TextFlow infoPanel;
    @FXML
    public ScrollPane infoScrollPane;

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
        infoScrollPane.vvalueProperty().bind(infoPanel.heightProperty());
        if (selectedFile != null) {
            updateLibrary();
            updateDropDown();
        }

    }

    private void updateSelectedFileName() {
        if (selectedFile != null) {
            itunesFileLabel.setText(this.selectedFile.getPath());
            enableStartButton();
        } else {
            itunesFileLabel.setText("");
        }
    }

    private void updateLibrary() {
        try {
            library = iTunesXMLParser.getLibrary(this.selectedFile.getPath());
        } catch (JAXBException | IOException | XMLStreamException e) {
            e.printStackTrace();
        }
    }

    private void updateDropDown() {
        playlistComboBox.updatePlaylist(library.getPlaylists());
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
            updateDropDown();
            updateSelectedFileName();
        }
    }

    @FXML
    private void startCopyRating() {
        updateLibrary();
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
        if (result.isPresent() && result.get() == ButtonType.OK) {
            progressBar.progressProperty().unbind();
            Task<List<Message>> ratingCopyTask = new RatingCopyTask(ratingCopyProcessor, songs, selectedTag, override.isSelected());
            progressBar.progressProperty().bind(ratingCopyTask.progressProperty());
//            ratingCopyTask.setOnSucceeded(e -> processResults(e));
            ratingCopyTask.setOnRunning(e -> displayMessage(new Message(Message.MessageLevel.INFO, "Start Processing")));
            ratingCopyTask.setOnSucceeded(e -> displayMessage(new Message(Message.MessageLevel.INFO, "Finish Processing")));
            ratingCopyTask.valueProperty().addListener((ob, o, n) -> displayMessage(n.toArray(new Message[n.size()])));
            new Thread(ratingCopyTask).start();
        }
    }

    private void processResults(WorkerStateEvent event) {
//        List<Message> messages = (List<Message>) event.getSource().getValue();
//
//        }
    }

    private void displayMessage(Message... messages) {
        for (Message m : messages) {
            Text t = new Text("#"+m.getLevel().toString()+" - "+m.getMessage() + "\n");
            switch (m.getLevel()) {
                case ERROR:
                    t.setFill(Color.valueOf("#CBA89F"));
                    break;
                case INFO:
                    t.setFill(Color.valueOf("#91CB8E"));
                    break;
            }
            infoPanel.getChildren().add(t);
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
        if (playlistSelected && tagSelected && selectedFile != null)
            startButton.setDisable(false);
    }
}
