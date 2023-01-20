package com.example.demomymusicplayer;

import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;


public class Playlist {
    private String name;
    private List<HelloController.Song> songs;
    public Playlist(String name) {
        this.name = name;
        this.songs = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<HelloController.Song> getSongs() {
        return songs;
    }

    public void addSong(HelloController.Song song) {
        songs.add(song);
    }

    public void removeSong(HelloController.Song song) {
        songs.remove(song);
    }

    public String getTotalDuration() {
        Duration totalDuration = Duration.ZERO;
        for (HelloController.Song song : songs) {
            totalDuration = totalDuration.add(song.getMedia().getDuration());
        }
        long min = (long) totalDuration.toMinutes();
        long sec = (long) (totalDuration.toSeconds() % 60);
        return String.format("%02d:%02d:%02d", min / 60, min % 60, sec);
    }
}


