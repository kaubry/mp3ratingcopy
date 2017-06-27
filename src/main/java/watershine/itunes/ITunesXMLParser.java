package watershine.itunes;

import org.springframework.stereotype.Component;
import watershine.LibraryParserInterface;
import watershine.itunes.jax2b.Dict;
import watershine.itunes.jax2b.DictArray;
import watershine.itunes.jax2b.SongLibrary;
import watershine.model.Library;
import watershine.model.Playlist;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    public Library getLibrary(String xmlFilePath) throws JAXBException, IOException, XMLStreamException {
        Library library = new Library();

        try {
            SongLibrary songLibrary = parseFile(xmlFilePath);
            library.setSongs(getSongs(songLibrary));
            library.setPlaylists(getStructuredPlaylist(getPlaylists(songLibrary)));

        } catch (IOException | JAXBException | XMLStreamException e) {
            throw e;
        }


        return library;
    }

    private List<Song> getSongs(SongLibrary songLibrary) {
        ArrayList<Song> songs = new ArrayList<>();
        if (songLibrary != null && songLibrary.getDict() != null) {
            Dict tracks = (Dict) getValueInDict(songLibrary.getDict(), "Tracks");
            for (Object d : tracks.getValues()) {
                if (d instanceof Dict) {
                    Dict dict = (Dict) d;
                    Song song = new Song();
                    int id = (int) getValueInDict(dict, "Track ID");
                    song.setId(id);
                    String location = (String) getValueInDict(dict, "Location");
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

    private List<Playlist> getPlaylists(SongLibrary songLibrary) {
        ArrayList<Playlist> playlists = new ArrayList<>();
        if (songLibrary != null && songLibrary.getDict() != null) {
            DictArray lists = (DictArray) getValueInDict(songLibrary.getDict(), "Playlists");
            for (Dict dict : lists.getDicts()) {
                Playlist p = new Playlist();
                if (Boolean.FALSE.equals(getValueInDict(dict, "Visible"))) {
                    continue;
                }
                if (Boolean.TRUE.equals(getValueInDict(dict, "Folder"))) {
                    p.setFolder(true);
                }
                DictArray playlistItem = (DictArray) getValueInDict(dict, "Playlist Items");

                p.setName((String) getValueInDict(dict, "Name"));
                if (!p.isFolder()) {
                    p.setTracks(getTracksInPlaylist(playlistItem));
                }
                p.setPersistentId((String) getValueInDict(dict, "Playlist Persistent ID"));
                p.setParentPersistentId((String) getValueInDict(dict, "Parent Persistent ID"));
                playlists.add(p);
            }
        }
        return playlists;
    }

    private List<Integer> getTracksInPlaylist(DictArray playlistItem) {
        if (playlistItem == null)
            return null;
        List<Integer> tracks = new ArrayList<>();
        for (Dict dict : playlistItem.getDicts()) {
            tracks.add((Integer) getValueInDict(dict, "Track ID"));
        }
        return tracks;
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

    private List<Playlist> getStructuredPlaylist(List<Playlist> playlists) {
        Map<String, Playlist> playlistMap = playlists.stream().collect(Collectors.toMap(Playlist::getPersistentId, Function.identity()));
        for (Iterator<Map.Entry<String, Playlist>> it = playlistMap.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Playlist> item = it.next();
            if (item.getValue().getParentPersistentId() != null)
                playlistMap.get(item.getValue().getParentPersistentId()).addChild(item.getValue());
        }
        return playlistMap.values().stream().filter(v -> v.getParentPersistentId() == null).collect(Collectors.toList());
    }


}
