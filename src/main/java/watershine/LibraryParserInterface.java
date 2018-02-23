package watershine;

import watershine.model.Library;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;


public interface LibraryParserInterface {

    Library getLibrary(String xmlFilePath) throws JAXBException, IOException, XMLStreamException;
}
