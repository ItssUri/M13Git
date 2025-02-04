package com.example.musicplayer;

public class Song {
    String name;
    int songInt;
    long id;

    public Song(long Id, String name, int songInt) {
        this.id=Id;
        this.name= name;
        this.songInt= songInt;
    }
    public String getName() {return name;}
    public long getId() {return id;}
    public int getSong() {return songInt;}
}
