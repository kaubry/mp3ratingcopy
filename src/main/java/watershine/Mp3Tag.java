package watershine;

/**
 * Created by kevin on 26.06.2017.
 */
public enum Mp3Tag {
    COMPOSER("Composer"),
    COMMENT("Comment");


    private String tagName;

    Mp3Tag(String tagName) {
        this.tagName = tagName;
    }

    public String tagName() {
        return tagName;
    }

    public static Mp3Tag getEnum(String value) {
        switch (value) {
            case "Composer":
                return COMPOSER;
            case "Comment":
                return COMMENT;
            default:
                return null;
        }
    }
}
