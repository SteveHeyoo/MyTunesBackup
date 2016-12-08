/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mytunes.BLL;

import java.io.File;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/**
 *
 * @author Stefan-VpcEB3J1E
 */
public class MyTunesPlayer
{

    MediaPlayer mediaPlayer;
    File file;
    Media media;
    
    private boolean isPaused;
    private boolean isPlayingBoolean;

    public MyTunesPlayer(String filePath)
    {
        String path = filePath.replace("\\", "/");
        file = new File(path);
        media = new Media(file.toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        
    }
    
    public MediaPlayer getMediaPlayer()
    {
        return mediaPlayer;
    }
    
    public void setPause(boolean value)
    {
        isPaused = value;
    }
    
    public boolean isPaused()
    {
        return isPaused;
    }
    
    public void setIsPlaying(boolean value)
    {
        isPlayingBoolean = value;
    }
    
    public boolean isPlaying()
    {
        return isPlayingBoolean;
    }
}

