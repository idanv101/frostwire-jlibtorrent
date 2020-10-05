import java.util.HashMap;

public class Database {
    private static final HashMap<Long, TvShow> mShowsDb = new HashMap<>();

    public TvShow addTvShow(long imdbID, TvShow show) {
        return mShowsDb.put(imdbID, show);
    }

    public TvShow getTvShow(long imdbID) {
        return mShowsDb.get(imdbID);
    }
}
