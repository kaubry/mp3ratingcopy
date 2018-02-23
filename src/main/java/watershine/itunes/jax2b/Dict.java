package watershine.itunes.jax2b;


import javax.xml.bind.annotation.*;
import java.util.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class Dict {

    @XmlElement( name="key" )
    private List<String> keys;

    @XmlElements(value = {
            @XmlElement(name="integer",
                    type=Integer.class),
            @XmlElement(name="string",
                    type=String.class),
            @XmlElement(name="date",
                    type=Date.class),
            @XmlElement(name="true",
                    type=Boolean.class, defaultValue = "true"),
            @XmlElement(name="false",
                    type=Boolean.class, defaultValue = "false"),
            @XmlElement(name="dict",
                    type=Dict.class),
            @XmlElement(name="array",
                    type=DictArray.class),
            @XmlElement(name="data",
                    type=String.class)
    })

    private List<Object> values = new ArrayList<>();

    public List<String> getKeys() {
        return keys;
    }

    public List<Object> getValues() {
        return values;
    }
}
