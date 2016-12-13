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

    private boolean isPlayingBoolean;

    public MyTunesPlayer(String filePath)
    {
        String path = filePath.replace("\\", "/");
        file = new File(path);
        media = new Media(file.toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        
    }
    
    /**
     * Returns the mediaplayer
     * @return 
     */
    public MediaPlayer getMediaPlayer()
    {
        return mediaPlayer;
    }
}

