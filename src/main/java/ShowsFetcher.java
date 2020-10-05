import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class ShowsFetcher {
    private final static String IMDB_URL =
            "https://www.imdb.com/chart/tvmeter/?ref_=nv_tvv_mptv";

    public List<String> popularShows() throws IOException {
        Document document = Jsoup.connect(IMDB_URL).get();
        Element element = document
                .getElementsByClass("lister-list")
                .get(0);

        return element
                .getElementsByTag("tr")
                .stream()
                .map(ShowsFetcher::getTitle)
                .collect(Collectors.toList());
    }

    private static String getTitle(Element e) {
        String name = e
                .getElementsByClass("titleColumn")
                .get(0)
                .getElementsByTag("a")
                .text();

        String year = e
                .getElementsByClass("titleColumn")
                .get(0)
                .getElementsByTag("span")
                .text();

        return name + " " + year;
    }
}
