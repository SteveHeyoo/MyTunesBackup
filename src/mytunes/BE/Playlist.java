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

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getPlaylistDuration()
    {
        return playlistDuration;
    }

    public void setPlaylistDuration(String playlistDuration)
    {
        this.playlistDuration = playlistDuration;
    }
    
    public int getId()
    {
        return id;
    }

    public int getNumberOfSongsInPlaylist()
    {
        return numberOfSongsInPlaylist;
    }

    public void setNumberOfSongsInPlaylist(int numberOfSongsInPlaylist)
    {
        this.numberOfSongsInPlaylist = numberOfSongsInPlaylist;
    }
   
    
}
