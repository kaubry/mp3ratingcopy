package watershine;

import watershine.model.Library;
import watershine.model.Song;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.ArrayList;


public interface LibraryParserInterface {

    public Library getLibrary(String xmlFilePath) throws JAXBException, IOException, XMLStreamException;
}
