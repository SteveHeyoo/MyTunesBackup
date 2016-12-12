/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mytunes.BE;

import javafx.beans.property.IntegerProperty;
import javafx.collections.FXCollections;


/**
 *
 * @author Stefan-VpcEB3J1E
 */
public class Playlist
{
   private final int id;
   private String name;
   private String playlistDuration;
   private int numberOfSongsInPlaylist;

    public Playlist(int id, String name)
    {
        this.id = id;
        this.name = name;
    }

    /**
     * Returns the name of the playlist
     * @return 
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets the name of the playlist
     * @param name 
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Returns the duration of the playlist.
     * @return 
     */
    public String getPlaylistDuration()
    {
        return playlistDuration;
    }

    /**
     * Sets the duration of the playlist.
     * @param playlistDuration 
     */
    public void setPlaylistDuration(String playlistDuration)
    {
        this.playlistDuration = playlistDuration;
    }
    
    /**
     * Returns the id of the playlist.
     * @return 
     */
    public int getId()
    {
        return id;
    }

    /**
     * Returns the amount of song in the playlist.
     * @return 
     */
    public int getNumberOfSongsInPlaylist()
    {
        return numberOfSongsInPlaylist;
    }

    /**
     * Sets the amount of songs in the playlist.
     * @param numberOfSongsInPlaylist 
     */
    public void setNumberOfSongsInPlaylist(int numberOfSongsInPlaylist)
    {
        this.numberOfSongsInPlaylist = numberOfSongsInPlaylist;
    }
}
