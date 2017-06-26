package watershine;

/**
 * Created by kevin on 26.06.2017.
 */
public enum Mp3Tag {
    COMPOSER("composer"),
    COMMENT("comment");


    private String tagName;

    Mp3Tag(String tagName) {
        this.tagName = tagName;
    }

    public String tagName() {
        return tagName;
    }

    public static Mp3Tag getEnum(String value) {
        switch (value) {
            case "composer":
                return COMPOSER;
            case "comment":
                return COMMENT;
            default:
                return null;
        }
    }
}
