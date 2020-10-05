package com.frostwire.jlibtorrent;

import com.frostwire.jlibtorrent.alerts.AddTorrentAlert;
import com.frostwire.jlibtorrent.alerts.Alert;
import com.frostwire.jlibtorrent.alerts.AlertType;
import com.frostwire.jlibtorrent.alerts.BlockFinishedAlert;
import org.json.simple.JSONObject;

import java.io.File;
import java.util.concurrent.CountDownLatch;

public class Main {
    private final static String SAVE_PATH = "F:/Users/Idan/Videos/Shows/";
    private final static String mTestTorrentPath =
            "F:/Users/Idan/Videos/Shows/the_boys_s02e06.torrent";
    private final static int mTestIndex = 1;

    public static void main(String[] args) {
        MediaManagement mediaManagement = new MediaManagement();
        watchEpisode(mediaManagement, "The Boys", 1, 1);
        System.out.println(mediaManagement);
    }

    private static void watchEpisode(MediaManagement mediaManagement,
                                     String showName, int season, int episode) {
        mediaManagement.addEpisode(showName, season, episode);
        JSONObject episodeJSON = mediaManagement.getEpisode(showName, season,
                episode);

        if (episodeJSON.get("video_file") == "") {
            downloadTorrent(mTestTorrentPath, mTestIndex, episodeJSON);
        }
    }

    // Downloading torrent for the first time
    private static void downloadTorrent(String torrentPath, int fileIndex,
                                        JSONObject episodeJSON) {
        File torrentFile = new File(torrentPath);
        TorrentInfo torrentInfo = new TorrentInfo(torrentFile);

        episodeJSON.put("torrent_file", torrentPath);
        System.out.println("Torrent file: " + torrentPath);
        System.out.print("Torrent contains: ");
        for (int i = 0; i < torrentInfo.numFiles(); i++)
            System.out.print(torrentInfo.files().fileName(i) + " ");
        System.out.println();
        System.out.println("Chosen file: " +
                torrentInfo.files().fileName(fileIndex));

        final SessionManager sessionManager = new SessionManager();
        final CountDownLatch latch = new CountDownLatch(1);

        sessionManager.addListener(new AlertListener() {
            @Override
            public int[] types() {
                return null;
            }

            @Override
            public void alert(Alert<?> alert) {
                AlertType type = alert.type();

                switch (type) {
                    case ADD_TORRENT:
                        System.out.println("Torrent added");
                        TorrentHandle torrentHandle =
                                ((AddTorrentAlert) alert).handle();
                        episodeJSON.put("video_file", torrentHandle.torrentFile().files().filePath(fileIndex));
                        applySettings(torrentHandle, fileIndex);
                        latch.countDown();
                        break;

                    case BLOCK_FINISHED:
                        BlockFinishedAlert a = (BlockFinishedAlert) alert;
                        int p = (int) (a.handle().status().progress() * 100);
                        System.out.println("Progress: " + p + " for torrent name: " + a.torrentName());
                        System.out.println("Download Speed: " +
                                sessionManager
                                        .stats()
                                        .downloadRate());
                        System.out.println("Total Downloaded: " +
                                sessionManager
                                        .stats()
                                        .totalDownload());
                        break;

                    case TORRENT_FINISHED:
                        System.out.println("Torrent finished");
                        latch.countDown();
                        break;
                }
            }
        });
        sessionManager.start();
        sessionManager.download(torrentInfo, new File(SAVE_PATH));

        try {
            latch.await();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sessionManager.stop();
        }
    }

    private static void applySettings(TorrentHandle torrentHandle,
                                      int fileIndex) {
        // Set sequential download
        torrentHandle.setFlags(TorrentFlags.SEQUENTIAL_DOWNLOAD);

        // Set priorities
        Priority[] priorities = new Priority[torrentHandle.torrentFile()
                .numFiles()];
        for (int i = 0; i < priorities.length; i++)
            priorities[i] = Priority.IGNORE;

        priorities[fileIndex] = Priority.NORMAL;
        torrentHandle.prioritizeFiles(priorities);
    }
}
