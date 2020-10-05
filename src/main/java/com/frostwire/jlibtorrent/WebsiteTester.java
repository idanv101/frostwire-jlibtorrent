package com.frostwire.jlibtorrent;

public class WebsiteTester {
    public static void main(String[] args) {
        ShowsFetcher showsFetcher = new ShowsFetcher();

        try {
            System.out.println(showsFetcher.popularShows());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
