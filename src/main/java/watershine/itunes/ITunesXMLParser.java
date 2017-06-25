package watershine.itunes;

import org.springframework.stereotype.Component;
import watershine.LibraryParserInterface;
import watershine.itunes.jax2b.Dict;
import watershine.itunes.jax2b.SongLibrary;
import watershine.itunes.model.ITunesSongLibrary;
import watershine.model.Song;


import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

@Component
public class ITunesXMLParser implements LibraryParserInterface {

    private final static String TO_REMOVE_FROM_PATH = "file://localhost/";


    private SongLibrary parseFile(String filePath) throws IOException, JAXBException, XMLStreamException {

        FileInputStream is = null;
        try {
            is = new FileInputStream(filePath);
            JAXBContext context = JAXBContext.newInstance(SongLibrary.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            XMLInputFactory xif = XMLInputFactory.newFactory();
            xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            XMLStreamReader xsr = xif.createXMLStreamReader(new StreamSource(filePath));
            return unmarshaller.unmarshal(xsr, SongLibrary.class).getValue();
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    @Override
    public ArrayList<Song> getSongs(String xmlFilePath) throws JAXBException, IOException, XMLStreamException {
        ArrayList<Song> songs = new ArrayList<>();
        SongLibrary songLibrary = null;
        try {
            songLibrary = parseFile(xmlFilePath);
        } catch (IOException | JAXBException | XMLStreamException e) {
            throw e;
        }
        if (songLibrary != null && songLibrary.getDict() != null) {
            ITunesSongLibrary iTunesSongLibrary = new ITunesSongLibrary(songLibrary);
            Dict tracks = (Dict) iTunesSongLibrary.getValue("Tracks");
            for (Object d : tracks.getValues()) {
                if (d instanceof Dict) {
                    Dict dict = (Dict) d;
                    String location = (String) getValueInDict(dict, "Location");
                    Song song = new Song();
//                    song.setSongFileURI(location.substring(TO_REMOVE_FROM_PATH.length(), location.length()));
                    song.setSongFileURI(location);
                    try {
                        int rating = (Integer) getValueInDict(dict, "Rating");
                        song.setStarRating(rating);
                    } catch (NullPointerException e) {
                    }
                    songs.add(song);
                }
            }

        }

        return songs;
    }

    private Object getValueInDict(Dict dict, String key) {
        if (!dict.getKeys().contains(key)) {
            return null;
        }
        int index = dict.getKeys().indexOf(key);
        try {
            return dict.getValues().get(index);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return null;
    }
}
