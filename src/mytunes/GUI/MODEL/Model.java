/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mytunes.GUI.MODEL;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.util.Duration;
import javax.sound.sampled.UnsupportedAudioFileException;
import mytunes.BE.Playlist;
import mytunes.BE.Song;
import mytunes.BLL.MusicManager;
import mytunes.BLL.MyTunesPlayer;
import mytunes.GUI.CONTROLLER.FXMLDocumentController;

/**
 *
 * @author Stefan-VpcEB3J1E
 */
public class Model
{

    private static Model INSTANCE;
    private MusicManager mMgr;
    private ObservableList songs, playlists, songsByPlaylistId;
    private MyTunesPlayer mTPlayer;
    private int lastSongId;
    private int lastSongIndex;
    private boolean runningDelay;
    private boolean playingSong;
    
    private Song songPlaying;
    private Timeline timeline;
    private List<Song> currentList;
    private Control currentListControl;
    private Playlist currentPlaylist;
    private int currentIndex;
    private List<Song> songsCleared;
    private boolean repeatSong;
    
    private Model()
    {
        mMgr = new MusicManager();
        songs = FXCollections.observableArrayList();
        playlists = FXCollections.observableArrayList();
        songsByPlaylistId = FXCollections.observableArrayList();
        loadSongsAndPlaylists();
       
   
    }
    
    public static Model getInstance()
    {
        if (INSTANCE == null)
        {
            INSTANCE = new Model();
        }
        return INSTANCE;
    }

    public void createNewSong(File file) throws IOException, UnsupportedAudioFileException
    {
        Song song;
        song = mMgr.addSong(file);
        songs.add(song);
    }
    
    private void loadSongsAndPlaylists()
    {
        try
        {
            playlists.clear();
            songs.clear();
            playlists.addAll(mMgr.getAllPlayLists());
            songs.addAll(mMgr.getAllSongs());
        } 
        catch (IOException ex)
        {
            ex.printStackTrace();
        } catch (UnsupportedAudioFileException ex)
        {
           ex.printStackTrace();
        }
    }
    
    /**
     * Returns a list of all songs
     * @return 
     */
    public ObservableList<Song> getAllSongs()
    {
        return songs;
    }
    
    /**
     * Sets currentListControl
     * @param currentListControl 
     */
    public void setCurrentListControl(Control currentListControl)
    {
        this.currentListControl = currentListControl; 
    }
    
    /**
     * Sets current playlist
     * @param playlist 
     */
    public void setCurrentPlaylist(Playlist playlist)
    {
        currentPlaylist = playlist;
    }
    
    /**
     * sets current index
     * @param index 
     */
    public void setIndex(int index)
    {
        currentIndex = index;
    }
    
    /**
     * sets repeat song boolean
     * @param value 
     */
    public void setRepeatSong(boolean value)
    {
        repeatSong = value;
    }
    
    /**
     * gets repeat song boolean
     * @return 
     */
    public boolean getRepeatSong()
    {
        return repeatSong;
    }
    
    
    /**
     * Stops the current song and plays the song passed to it with the song parameter
     * @param song 
     */
    public void playSong(Song song)
    {
        
        if (mTPlayer == null)
        {            
            playTheSong(song);
        }
        else
        {
            mTPlayer.getMediaPlayer().stop();
            playTheSong(song);
        }            
    }

    /**
     * If a song is paused, and this method is called the song will resume playing. If it is playing it will pause.
     * If nothing is playing it will start playing the selected song
     * 
     */
    public void playSongButtonClick()
    {
        Song songToPlay;
        try
        {
            ListView<Song> playlist = (ListView)currentListControl;
            songToPlay = playlist.getSelectionModel().getSelectedItem();    
        }
        catch(ClassCastException c)
        {
            TableView<Song> playlist = (TableView)currentListControl;
            songToPlay = playlist.getSelectionModel().getSelectedItem();
           
        }   
        
        if(mTPlayer != null)
        {
            
            if(currentIndex == lastSongIndex)
            {
                //it is the same song as the last. the song should pause/play
                if(mTPlayer.isPaused())
                {
                    //resume
                    timeline.play();
                    mTPlayer.getMediaPlayer().play();
                    mTPlayer.setPause(false);
            
                }
                else
                {
                    //pause
                    if(mTPlayer.getMediaPlayer().getCurrentTime().toMillis() < mTPlayer.getMediaPlayer().getCycleDuration().toMillis())
                    {
                        //mTPlayer.getMediaPlayer().setAutoPlay(false);
                        //playTheSong(song);
                        timeline.pause();
                        mTPlayer.getMediaPlayer().pause();                    
                        mTPlayer.setPause(true);
                        //Pause song            
                    }
                    else
                    {
                        playTheSong(songToPlay);
                    }
                }
            }
            else
            {
                //it is a new song. play the song
                mTPlayer.getMediaPlayer().stop();
                playTheSong(songToPlay);
            }          
        }
        else
        {   
                //no song playing, and no song has been played before
                playTheSong(songToPlay);
                
        }
    }
    
    /**
     * Plays the song that is passed in the parameter.
     * @param song 
     */
    private void playTheSong(Song song)
    {      

        if(playingSong == true)
        {
            timeline.stop();
            
        }
        songPlaying = song;
        playingSong = true;
        mTPlayer = new MyTunesPlayer(song.getFilePath());
        mTPlayer.getMediaPlayer().setAutoPlay(true);
        
        //lastSongId = song.getId();

        lastSongIndex = currentIndex;
        
        ListView playlist = null;
        
        try
        {
            playlist = (ListView)currentListControl;
            //playlist.getSelectionModel().clearAndSelect(currentList.indexOf(nextSong));
            //currentIndex = playlist.getSelectionModel().getSelectedIndex();
            //currentIndex = playlist.ge
            
        }
        catch(ClassCastException c)
        {
            System.out.println("sdas");
        }
        
        //index = currentList.indexOf(song);
        startDelay(song); 
        //currentIndex = playlist.getSelectionModel().getSelectedIndex();
        
        
    }
    
    /**
     * Starts a delay based on the duration of the song. When the delay is gone it calls the "playNextSong" method
     * @param song 
     */
    private void startDelay(Song song)
    {
        if (repeatSong == false)
        {
            timeline = new Timeline(new KeyFrame(Duration.millis((song.getDuration()*1000)),ae -> playNextSong("next")));timeline.play();
        }
        else
        {
            timeline = new Timeline(new KeyFrame(Duration.millis((song.getDuration()*1000)),ae -> playNextSong("repeat")));timeline.play();
        }
        
        runningDelay = true;
    }
    
    /**
     * Returns the song playing
     * @return 
     */
    public Song getSongPlaying()
    {
        return songPlaying;
    }
    
    /**
     * Calls the "playNextSong" method with the string parameter "next"
     */
    public void pressNextButton()
    {       
        //mTPlayer.setPause(false);
        if (mTPlayer == null)
        {
            //mTPlayer.getMediaPlayer().stop();
            timeline.stop();

            playNextSong("next");
        }
        else
        {
            mTPlayer.getMediaPlayer().stop();

            timeline.stop();
            
            playNextSong("next");           
        }       
    }
    /**
     * Calls the "playNextSong" method with the string parameter "previous"
     */
    public void pressPreviousButton()
    {
        if (mTPlayer == null)
        {
            //mTPlayer.getMediaPlayer().stop();
            timeline.stop();

            playNextSong("previous");
        }
        else
        {
            mTPlayer.getMediaPlayer().stop();

            timeline.stop();
            
            playNextSong("previous");         
        }
    }
    
    /**
     * Plays the next song. The parameter defines whether the next is song is the previous, next or the same.
     * Moves the playlists selection model to the next song aswell
     * @param previousNextOrRepeat 
     */
    private void playNextSong(String previousNextOrRepeat) 
    {
        playingSong = false;
        Song nextSong = null;
        try
        {
            if (repeatSong == false)
            {
                nextSong = getNextSongInCurrentList(songPlaying,previousNextOrRepeat);
            }
            else
            {
                nextSong = getNextSongInCurrentList(songPlaying,"repeat");
            }
            
        } catch (IOException ex)
        {
            ex.printStackTrace();
        } catch (UnsupportedAudioFileException ex)
        {
           ex.printStackTrace();
        }
        
        try
        {
            ListView<Song> playlist = (ListView)currentListControl;
            //playlist.getSelectionModel().clearAndSelect(currentList.indexOf(nextSong));
            playlist.getSelectionModel().clearAndSelect(currentIndex);
            
        }
        catch(ClassCastException c)
        {
            TableView<Song> playlist = (TableView)currentListControl;
            //playlist.getSelectionModel().clearAndSelect(currentList.indexOf(nextSong));
            playlist.getSelectionModel().clearAndSelect(currentIndex);
        }
        playTheSong(nextSong);
        //runningDelay = false;      
    }
    
    /**
     * Returns the next/previous or same song in the currently active list of songs, depending on the String parameter given
     * @param song
     * @return the next song
     */
    public Song getNextSongInCurrentList(Song currentSong, String previousNextOrRepeat) throws IOException, UnsupportedAudioFileException
    {
        Song nextSong;       
        
        if(songs.contains(currentSong))
        {
            currentList = songs;
            System.out.println("List: all list");
        }
        else if(songsByPlaylistId.contains(currentSong))
        {
            currentList = songsByPlaylistId;
            System.out.println("List: playlist list");
        }
        else
        {
            currentList = null;
            System.out.println("ERROR no list found");
        }        
        
        System.out.println("index:" + currentIndex);
        
        if (previousNextOrRepeat.equals("next"))
        {
            if (currentIndex != currentList.size()-1)
            {
                nextSong = currentList.get(currentIndex + 1);
                currentIndex = currentIndex + 1;
            }
            else
            {
                nextSong = currentList.get(0);
                currentIndex = 0;
            }
        }
        else if(previousNextOrRepeat.equals("previous"))
        {
            if (currentIndex != 0)
            {
                nextSong = currentList.get(currentIndex - 1);
                currentIndex = currentIndex - 1;
            }
            else
            {
                nextSong = currentList.get(0);
                currentIndex = 0;
            }
        }
        else
        {
            nextSong = currentSong;
        }
        
        return nextSong;
    }
    
    public void deleteSong(Song song) throws IOException
    {

            mMgr.deleteSong(song.getId());
            songs.remove(song);
    }

    public void createNewPlaylist(Playlist playlistToEdit, String playlistName) throws IOException, UnsupportedAudioFileException
    {
        if (playlistToEdit == null)
        {
            Playlist playlistToAdd = mMgr.createNewPlaylist(playlistName);
            playlists.add(playlistToAdd);            
        }
        else
        {          
                playlistToEdit.setName(playlistName);
                mMgr.editPlaylistName(playlistToEdit);          
                playlists.clear();
                playlists.addAll(mMgr.getAllPlayLists());
        }

    }

    public ObservableList<Playlist> getAllPlaylists()
    {
        return playlists;
    }

    public ObservableList<Song> getAllSongsByPlaylistId()
    {
        return songsByPlaylistId;
    }

    public void deletPlaylist(Playlist playlist) throws IOException
    {
            mMgr.deletePlaylist(playlist.getId());

            playlists.remove(playlist);
            songsByPlaylistId.clear();
           

    }

    public void showPlaylistSongs(int playlistId) throws UnsupportedAudioFileException, IOException

    {
            songsByPlaylistId.clear();
            songsByPlaylistId.addAll(mMgr.getSongsByPlaylistId(playlistId));      
    }


    public void addSongToPlaylist(Song songToAdd, Playlist playlistToAddTo) throws UnsupportedAudioFileException, IOException

    {

            //  songsByPlaylistId.add(songToAdd);

            mMgr.addSongToPlaylist(songToAdd.getId(), playlistToAddTo.getId());
            playlistToAddTo.setNumberOfSongsInPlaylist(+1);
            playlists.clear();
            playlists.addAll(mMgr.getAllPlayLists());
            showPlaylistSongs(playlistToAddTo.getId());

    }

    public List<Song> filterSongs(String query) throws IOException
    {
        List<Song> songList = null;

            songList = mMgr.search(query);


        return songList;
    }

    public void setSongs(List<Song> songList)
    {
        songs.clear();
        songs.addAll(songList);
    }

    public int moveSongUp(Song songToMoveUp)
    {
        int indexId = songsByPlaylistId.indexOf(songToMoveUp);
        System.out.println(indexId);
        if (indexId != 0)
        {
            Collections.swap(songsByPlaylistId, indexId - 1, indexId);

        }
        else
        {
            indexId += 1;
        }
        return indexId;

    }

    public int moveSongDown(Song songToMoveDown)
    {
        int indexId = songsByPlaylistId.indexOf(songToMoveDown);
        if (indexId  != songsByPlaylistId.size()-1)
        {
            Collections.swap(songsByPlaylistId, indexId + 1, indexId);

        }
        else
        {
            indexId -= 1;
        }
        return indexId;
    }


    private void setVolume()
    {
        mTPlayer.getMediaPlayer().setVolume(lastSongId);
    }

    public void setVolume(double d)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void editSong(Song songToEdit, File fileSong) throws IOException, UnsupportedAudioFileException
    {
        Song songSong = (Song) songs.get(songs.indexOf(songToEdit));
        songSong.setArtist(songToEdit.getArtist());
        songSong.setTitle(songToEdit.getTitle());
        if (fileSong != null)
        {
            songSong.setFilePath(fileSong.getAbsolutePath());
        }
        
            mMgr.saveEditedSong(songSong);
            songs.clear();
            songs.addAll(mMgr.getAllSongs());

        
    }

    public MyTunesPlayer getmTPlayer()
    {
        return mTPlayer;
    }

    

    

}
