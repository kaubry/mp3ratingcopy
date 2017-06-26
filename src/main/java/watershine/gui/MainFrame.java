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


@Component
public class MainFrame extends JFrame implements ProcessFileProgressListener {

    private JFileChooser fc;
    private JProgressBar progressBar;
    private Mp3Tag selectedTag;
    private File selectedFile;

    @Autowired
    private ITunesXMLParser iTunesXMLParser;

    @Autowired
    private RatingCopyProcessor ratingCopyProcessor;

    @Autowired
    private TaskExecutor taskExecutor;

    public MainFrame() throws HeadlessException {
        super();
        this.selectedFile = getDefaultItunesXMLFile();
        initFileChooser();
        setSize(600, 200);

        this.setContentPane(getContentPanel());

    }

    @PostConstruct
    private void init() {
        ratingCopyProcessor.addProgressListener(this);
    }

    @PreDestroy
    private void destroy() {
        ratingCopyProcessor.removeProgressListener(this);
    }

    private JPanel getChooseFilePanel() {
        JLabel label = new JLabel();
        if(this.selectedFile != null) {
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
        contentPanel.add(getChooseTagPanel());
        contentPanel.add(getActionPanel());

        contentPanel.setBackground(Color.cyan);
        return contentPanel;
    }

    private void chooseITunesFile(JLabel label) {
        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            this.selectedFile = fc.getSelectedFile();
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
        if(selectedFile == null)
            return;
        try {
            ArrayList<Song> songs = iTunesXMLParser.getSongs(this.selectedFile.getPath());
            taskExecutor.execute(new RatingCopyTask(ratingCopyProcessor, songs, this.selectedTag));
//            ratingCopyProcessor.copyRatingsIntoMp3Tag(songs, this.selectedTag);
        } catch (JAXBException | IOException | XMLStreamException e) {
            e.printStackTrace();
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
        if(SystemUtils.IS_OS_WINDOWS) {
            File iTunesXml = new File(String.join(File.separator, homePath, "Music", "iTunes", "iTunes Music Library.xml"));
             if(iTunesXml.exists()) {
                 return iTunesXml;
             }
        }
        return null;
    }
}
