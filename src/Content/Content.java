package Content;

import java.util.List;

public abstract class Content {
    private int contentID ;
    private String titel ;
    private String director ;
    private String description ;
    private int releaseYears ;
    private boolean isAvailable ;

    public class genre extends Content {
        private List<Content> genreList ;

    }
}


