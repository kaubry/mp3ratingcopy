package watershine.itunes.jax2b;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;


@XmlAccessorType(XmlAccessType.FIELD)
public class DictArray {

    @XmlElement( name="dict" )
    private ArrayList<Dict> dicts;

    public ArrayList<Dict> getDicts() {
        return dicts;
    }
}
