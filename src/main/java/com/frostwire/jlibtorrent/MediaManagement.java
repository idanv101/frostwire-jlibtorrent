package com.frostwire.jlibtorrent;

import org.json.simple.JSONObject;

import java.util.HashMap;

public class MediaManagement {
    private final JSONObject mMedia;

    public MediaManagement() {
        mMedia = new JSONObject();
    }

    public JSONObject getShow(String name) {
        return (JSONObject) mMedia.get(name);
    }

    public void addShow(String name) {
        // if show already exists in data base then do nothing
        if (this.getShow(name) != null)
            return;

        mMedia.put(name, new JSONObject());
    }

    public JSONObject getSeason(String showName, int season) {
        if (this.getShow(showName) == null)
            return null;

        return (JSONObject) this.getShow(showName).get(season);
    }

    public void addSeason(String showName, int season) {
        // if season already exists in data base, do nothing
        if (this.getSeason(showName, season) != null)
            return;

        // if show does not exist in data base, add it
        this.addShow(showName);

        JSONObject tvShowJSON = this.getShow(showName);
        tvShowJSON.put(season, new JSONObject());
    }

    public JSONObject getEpisode(String showName, int season, int episode) {
        if (this.getSeason(showName, season) == null)
            return null;

        return (JSONObject) this.getSeason(showName, season).get(episode);
    }

    public void addEpisode(String showName, int season, int episode) {
        if (this.getEpisode(showName, season, episode) != null)
            return;

        // if season does not exist in data base, add it
        this.addSeason(showName, season);

        JSONObject seasonJSON = this.getSeason(showName, season);
        HashMap<String, Object> episodeDetails = new HashMap<>();
        episodeDetails.put("video_file", "");
        episodeDetails.put("finished", false);
        episodeDetails.put("torrent_file", "");

        seasonJSON.put(episode, new JSONObject(episodeDetails));
    }

    public String toString() {
        return mMedia.toString();
    }
}
