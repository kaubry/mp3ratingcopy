package watershine.itunes.jax2b;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;


@XmlAccessorType(XmlAccessType.FIELD)
public class DictArray {

    @XmlElement( name="dict" )
    private List<Dict> dicts;

    public List<Dict> getDicts() {
        return dicts;
    }
}
