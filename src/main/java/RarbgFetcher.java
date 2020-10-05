import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RarbgFetcher {
    private final static String BASE_URL =
            "https://torrentapi.org/pubapi_v2.php?mode=search";
    private final static String SEARCH_QUERY = "&search_string=";
    private final static String LIMIT_QUERY = "&limit=100";
    private final static String FORMAT_QUERY = "&format=json_extended";
    private final static String TOKEN_QUERY = "&token=";
    private final static String SORT_QUERY = "&sort=seeders";
    private final static String APP_ID_QUERY = "&app_id=showtime";
    private static String TOKEN = "fm6wjx54ht";

    public JSONArray search(String showName, int season, int episode)
            throws IOException {
        String requestUrl = createTvShowQuery(showName, season, episode);
        JSONArray jsonArray = null;

        try {
            String pageContents = fetchPageContents(requestUrl);
            JSONObject resultsJson = (JSONObject) new JSONParser()
                    .parse(pageContents);
            int timePassed = 0;
            while (resultsJson.get("torrent_results") == null) {
                if (timePassed > 10000) {
                    return new JSONArray();
                }
                Thread.sleep(1000);
                timePassed += 1000;

                pageContents = fetchPageContents(requestUrl);
                resultsJson = (JSONObject) new JSONParser().parse(pageContents);
            }
            jsonArray = (JSONArray) resultsJson.get("torrent_results");
        } catch (InterruptedException | ParseException e) {
            e.printStackTrace();
        }

        return jsonArray;
    }

    private String createTvShowQuery(String showName, int season, int episode) {
        String searchQuery = String.format("%s%s+s%02de%02d",
                SEARCH_QUERY, showName, season, episode);
        String tokenQuery = TOKEN_QUERY + TOKEN;

        return BASE_URL + searchQuery + LIMIT_QUERY + SORT_QUERY
                + FORMAT_QUERY  + tokenQuery + APP_ID_QUERY;
    }

    private String fetchPageContents(String requestUrl)
            throws IOException {
        Document document = Jsoup
                .connect(requestUrl)
                .ignoreContentType(true)
                .get();

        return document.text();
    }

    private static boolean relevantResult(String showName, int season,
                                          int episode) {
        return true;
    }
}
