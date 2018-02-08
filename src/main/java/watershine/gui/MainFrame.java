package watershine.gui;

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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class MainFrame extends JFrame implements ProcessFileProgressListener {

    private JFileChooser fc;
    private JProgressBar progressBar;
    private Mp3Tag selectedTag;
    private File selectedFile;
    private PlayListComboBox playlistJComboBox;
    private Playlist selectedPlaylist = null;
    private Library library;
    private JCheckBox override;

    @Autowired
    private ITunesXMLParser iTunesXMLParser;

    @Autowired
    private RatingCopyProcessor ratingCopyProcessor;

    @Autowired
    private TaskExecutor taskExecutor;

    public MainFrame() throws HeadlessException {
        super();
        setTitle("Mp3 Ratings Copy Tool");
        setSize(600, 250);
    }

    @PostConstruct
    private void init() {
        this.selectedFile = getDefaultItunesXMLFile();

        initFileChooser();
        ratingCopyProcessor.addProgressListener(this);
        this.setContentPane(getContentPanel());
        if (selectedFile != null)
            updateLibrary();

    }

    @PreDestroy
    private void destroy() {
        ratingCopyProcessor.removeProgressListener(this);
    }

    private void updateLibrary() {
        try {
            library = iTunesXMLParser.getLibrary(this.selectedFile.getPath());
            playlistJComboBox.updatePlaylist(library.getPlaylists());
        } catch (JAXBException | IOException | XMLStreamException e) {
            e.printStackTrace();
        }
    }

    private JPanel getChooseFilePanel() {
        JLabel label = new JLabel();
        if (this.selectedFile != null) {
            changeSelectedFileLabelText(label);
        }

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        JButton chooseITunesXml = new JButton("Locate iTunes XML file");
        chooseITunesXml.addActionListener(e -> chooseITunesFile(label));

        panel.add(chooseITunesXml);
        panel.add(label);
        return panel;
    }

    private JPanel getChooseTagPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        String[] tags = Arrays.stream(Mp3Tag.values()).map(s -> s.tagName()).toArray(String[]::new);
        JComboBox<String> tagsCombo = new JComboBox<>(tags);
        tagsCombo.addActionListener(e -> tagChanged((String) tagsCombo.getSelectedItem()));
        tagsCombo.setSelectedIndex(0);
        JLabel label = new JLabel("Id2 tag to copy ratings to");
        panel.add(label);
        panel.add(tagsCombo);

        override = new JCheckBox("Override");
        panel.add(override);

        panel.setPreferredSize(new Dimension(0, 30));
        return panel;
    }

    private JPanel getActionPanel() {
        JButton startButton = new JButton("Start");
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        startButton.addActionListener(e -> startCopyRating());
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        panel.add(startButton);
        panel.add(progressBar);
        return panel;
    }

    private JPanel getPlaylistPannel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        playlistJComboBox = new PlayListComboBox();
        playlistJComboBox.addActionListener(e -> selectPlaylist());
        panel.add(new JLabel("Playlist"));
        panel.add(playlistJComboBox);
        return panel;
    }

    private void selectPlaylist() {
        selectedPlaylist = (Playlist) this.playlistJComboBox.getSelectedItem();
    }

    private void initFileChooser() {
        fc = new JFileChooser();
        FileNameExtensionFilter xmlfilter = new FileNameExtensionFilter(
                "xml files (*.xml)", "xml");
        fc.setFileFilter(xmlfilter);
        fc.setAcceptAllFileFilterUsed(false);
    }

    private JPanel getContentPanel() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        contentPanel.add(getChooseFilePanel());
        contentPanel.add(getPlaylistPannel());
        contentPanel.add(getChooseTagPanel());
        contentPanel.add(getActionPanel());

        contentPanel.setBackground(Color.cyan);
        return contentPanel;
    }

    private void chooseITunesFile(JLabel label) {
        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            this.selectedFile = fc.getSelectedFile();
            updateLibrary();
            changeSelectedFileLabelText(label);
        } else {

        }
    }

    private void changeSelectedFileLabelText(JLabel label) {
        label.setText(this.selectedFile.getPath());
    }

    private void tagChanged(String selectedTag) {
        this.selectedTag = Mp3Tag.getEnum(selectedTag);
    }

    private void startCopyRating() {
        if (selectedFile == null)
            return;
        List<Song> songs = getSongsFromPlaylist(library.getSongs(), this.selectedPlaylist);
        int dialogResult = JOptionPane.showConfirmDialog(null, RatingCopyProcessor.getSongToProcess(songs, selectedTag, override.isSelected()).size() + " Songs will be processed", "Warning", JOptionPane.YES_NO_OPTION);
        if (dialogResult == JOptionPane.YES_OPTION) {
            taskExecutor.execute(new RatingCopyTask(ratingCopyProcessor, songs, this.selectedTag, override.isSelected()));
        }
    }

    @Override
    public void progress(int nbrOfFileProcessed, int nbrOfFileToProcess) {
        if (nbrOfFileProcessed == 0) {
            this.progressBar.setMinimum(0);
            this.progressBar.setMaximum(nbrOfFileToProcess);
        } else {
            this.progressBar.setValue(nbrOfFileProcessed);
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
}
