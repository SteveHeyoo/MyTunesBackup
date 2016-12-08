/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mytunes.BE;

/**
 *
 * @author Stefan-VpcEB3J1E
 */
public class Song
{

    private final int id;
    private String artist;
    private String title;
    private String filePath;
    private double duration;



    /**
     * Constructer. Called when a new song is instansiated
     * @param id
     * @param artist
     * @param title
     * @param filePath
     * @param duration 
     */

    public Song(int id, String artist, String title, String filePath, double duration)
    {
        this.id = id;
        this.artist = artist;
        this.title = title;
        this.filePath = filePath;
        this.duration = duration;
    }

    /**
     * Gets the songs Artist
     * @return Artist of the song as a String
     */
    public String getArtist()
    {
        return artist;
    }
    
    /**
     * Sets the song's artist
     * @param artist 
     */
    public void setArtist(String artist)
    {
        this.artist = artist;
    }
    
    /**
     * Returns the song's title (String)
     * @return title of the song as a String
     */
    public String getTitle()
    {
        return title;
    }
    
    /**
     * Sets the title of the song to the String given
     * @param title 
     */
    public void setTitle(String title)
    {
        this.title = title;
    }
    /**
     * Returns the unique id of the song
     * @return the song's unique id (int)
     */
    public int getId()
    {
        return id;
    }
    
    /**
     * Returns the song's file path (String)
     * @return String path
     */
    public String getFilePath()
    {
        return filePath;
    }

    /**
     * Returns the duration of the song in seconds
     * @return double Seconds
     */
    public double getDuration()
    {
        return duration;
    }
    /*
     * Takes the duration of the song (Seconds) and transforms it to (Minutes:Seconds)
     * @return String. example: "3:20"
     */
    public String getDurationInMinutes()
    {
        long microseconds = (long) duration * 1000000;
        int mili = (int) (microseconds / 1000);
        
        int sec = (mili / 1000) % 60;
        int min = (mili / 1000) / 60;
        
        String secString;
        if (sec < 10)
        {
            secString = "0" +sec;
        }
        else
        {
           secString = "" + sec; 
        }
        String minString = "" + min;
        String minutesAndSeconds =minString + ":" + secString;
        
        return minutesAndSeconds; 
    }

    @Override
    public String toString()
    {
        return artist + " - " + title;
    }
    
    /**
     * Returns the artist and the title in one string
     * @return 
     */
    public String getAllSongStringInfo()
    {
        return artist + " " + title;
    }

    public void setFilePath(String filePath)
    {
        this.filePath = filePath;
    }

    public void setDuration(double duration)
    {
        this.duration = duration;
    }
}
