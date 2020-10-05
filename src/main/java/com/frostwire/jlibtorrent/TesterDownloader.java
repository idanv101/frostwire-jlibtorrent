package com.frostwire.jlibtorrent;


import com.frostwire.jlibtorrent.alerts.*;

import java.io.File;
import java.util.concurrent.CountDownLatch;

public class TesterDownloader {
    public static final int KILO_MULTIPLIER = 10;
    public static final int MEGA_MULTIPLIER = 20;
    public static final int GIGA_MULTIPLIER = 30;

    public static String formatFileSize(long size) {
        if (size < Math.pow(2.0, KILO_MULTIPLIER))
            return String.format("%d bytes", size);

        if (size < Math.pow(2.0, MEGA_MULTIPLIER))
            return String
                    .format("%,.2f KB", size/Math.pow(2.0, KILO_MULTIPLIER));

        if (size < Math.pow(2.0, GIGA_MULTIPLIER))
            return String
                    .format("%,.2f MB", size/Math.pow(2.0, MEGA_MULTIPLIER));

        return String.format("%,.2f GB", size/Math.pow(2.0, GIGA_MULTIPLIER));
    }

    private static void applySettings(TorrentHandle torrentHandle) {
        // Set sequential download
        torrentHandle.setFlags(TorrentFlags.SEQUENTIAL_DOWNLOAD);

        // Set priorities
        torrentHandle.prioritizeFiles(new Priority[]{
                Priority.IGNORE, Priority.IGNORE, Priority.NORMAL});
    }

    public static void main(String[] args) throws Throwable {
        args = new String[]{"C:/Users/Idan/Downloads/spn_15_03.torrent"};
        File torrentFile = new File(args[0]);
        final SessionManager sessionManager = new SessionManager();
        final CountDownLatch countDownLatch = new CountDownLatch(1);
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
                        applySettings(torrentHandle);
                        torrentHandle.resume();
                        break;

                    case BLOCK_FINISHED:
                        BlockFinishedAlert a = (BlockFinishedAlert) alert;
                        int p = (int) (a.handle().status().progress() * 100);
                        System.out.println("Progress: " + p + " for torrent name: " + a.torrentName());
                        System.out.println("Download Speed: " +
                                formatFileSize(sessionManager
                                        .stats()
                                        .downloadRate()) + "/s");
                        System.out.println("Total Downloaded: " +
                                formatFileSize(sessionManager
                                        .stats()
                                        .totalDownload()));
                        break;

                    case TORRENT_FINISHED:
                        System.out.println("Torrent finished");
                        countDownLatch.countDown();
                        break;
                }
            }
        });
        sessionManager.start();
        TorrentInfo torrentInfo = new TorrentInfo(torrentFile);

        try {
            for (int index = 0; index < torrentInfo.numFiles(); ++index) {
                String info = "[";
                info += index + ", ";
                info += torrentInfo.files().fileName(index) + ", ";
                info += torrentInfo.files().fileSize(index);
                info += "]";

                System.out.println(info);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }


        sessionManager.download(torrentInfo, torrentFile.getParentFile());

        countDownLatch.await();
        sessionManager.stop();
    }
}
