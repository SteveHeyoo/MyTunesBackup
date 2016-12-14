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
import java.util.Observable;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import javax.sound.sampled.UnsupportedAudioFileException;
import mytunes.BE.Playlist;
import mytunes.BE.Song;
import mytunes.BLL.MusicManager;
import mytunes.BLL.MyTunesPlayer;
import mytunes.GUI.CONTROLLER.MainController;

/**
 *
 * @author Stefan-VpcEB3J1E
 */
public class Model extends Observable
{

    private static Model INSTANCE;
    private MusicManager mMgr;
    private ObservableList songs, playlists, songsByPlaylistId;
    private MyTunesPlayer mTPlayer;
    private int lastSongId;
    private int lastSongIndex;
    private int selectedIndex;
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
    
    /**
     * Private constructor for our Singleton pattern
     */
    private Model()
    {
        mMgr = new MusicManager();
        songs = FXCollections.observableArrayList();
        playlists = FXCollections.observableArrayList();
        songsByPlaylistId = FXCollections.observableArrayList();

        loadSongsAndPlaylists();

    }
    /**
     * A part of the Singleton pattern, to get our Model instance.
     * @return Model
     */
    public static Model getInstance()
    {
        if (INSTANCE == null)
        {
            INSTANCE = new Model();
        }
        return INSTANCE;
    }
    /**
     * Method to create a new song. Sends the File object to our Logic-layer, and returns a Song object.
     * Adds the new Song object to our Collection.
     * @param file MP3 file
     * @throws IOException
     * @throws UnsupportedAudioFileException 
     */
    public void createNewSong(File file) throws IOException, UnsupportedAudioFileException
    {
        Song song = mMgr.addSong(file);
        songs.add(song);
    }
    /**
     * Getting all the playlists and songs into our Collections.
     */
    private void loadSongsAndPlaylists()
    {
        try
        {
            playlists.clear();
            songs.clear();
            playlists.addAll(mMgr.getAllPlayLists());
            songs.addAll(mMgr.getAllSongs());
        } catch (IOException ex)
        {
            ex.printStackTrace();
        } catch (UnsupportedAudioFileException ex)
        {
            ex.printStackTrace();
        }
    }

    /**
     * Returns a list of all songs
     *
     * @return
     */
    public ObservableList<Song> getAllSongs()
    {
        return songs;
    }

    
    /**
     * Sets currentListControl
     *
     * @param currentListControl
     */
    public void setCurrentListControl(Control currentListControl)
    {
        this.currentListControl = currentListControl;
    }

    /**
     * Sets current playlist
     *
     * @param playlist
     */
    public void setCurrentPlaylist(Playlist playlist)
    {
        currentPlaylist = playlist;
    }

    public List<Song> getCurrentList()
    {
        return currentList;
    }

    public void setCurrentList(List<Song> currentList)
    {
        this.currentList = currentList;
    }
    
    /**
     * sets current index
     *
     * @param index
     */
    public void setIndex(int index)
    {
        currentIndex = index;
    }

    /**
     * Returns current index.
     * @return 
     */
    public int getCurrentIndex()
    {
        return currentIndex;
    }
    
    /**
     * sets repeat song boolean
     *
     * @param value
     */
    public void setRepeatSong(boolean value)
    {
        repeatSong = value;
    }

    /**
     * gets repeat song boolean
     *
     * @return
     */
    public boolean getRepeatSong()
    {
        return repeatSong;
    }
    /**
     * Sets the selected index (Part of the play-next-song method)
     * @param selectedIndex 
     */
    public void setSelectedIndex(int selectedIndex)
    {
        this.selectedIndex = selectedIndex;
    }
    /**
     * Gets the selected song index
     * @return 
     */
    public int gettSelectedPlaylistSongIndex()
    {
        return selectedIndex;
    }

    /**
     * Stops the current song and plays the song passed to it with the song
     * parameter
     *
     * @param song
     */
    public void playSong(Song song)
    {

        if (mTPlayer == null)
        {
            playTheSong(song);
        } else
        {
            mTPlayer.getMediaPlayer().stop();
            playTheSong(song);
        }
    }

    /**
     * If a song is paused, and this method is called the song will resume
     * playing. If it is playing it will pause. If nothing is playing it will
     * start playing the selected song
     *
     */
    public void playSongButtonClick()
    {
        
        if (mTPlayer != null)
        {
            if (mTPlayer.getMediaPlayer().getStatus() == MediaPlayer.Status.PAUSED)
                {
                    //resume
                    timeline.play();
                    mTPlayer.getMediaPlayer().play();
                    

                } else //pause
                {
                    if (mTPlayer.getMediaPlayer().getCurrentTime().toMillis() < mTPlayer.getMediaPlayer().getCycleDuration().toMillis())
                    {
                        //mTPlayer.getMediaPlayer().setAutoPlay(false);
                        //playTheSong(song);
                        timeline.pause();
                        mTPlayer.getMediaPlayer().pause();
                        
                        //Pause song            
                    } else
                    {
                        //playTheSong(songToPlay);
                    }
                }
        }
        else
        {
            
        }
        /*
        Song songToPlay;
        try
        {
            ListView<Song> playlist = (ListView) currentListControl;
            songToPlay = playlist.getSelectionModel().getSelectedItem();
        } catch (ClassCastException c)
        {
            TableView<Song> playlist = (TableView) currentListControl;
            songToPlay = playlist.getSelectionModel().getSelectedItem();

        }

        if (mTPlayer != null)
        {

            if (currentIndex == lastSongIndex)
            {
                //it is the same song as the last. the song should pause/play
                if (mTPlayer.isPaused())
                {
                    //resume
                    timeline.play();
                    mTPlayer.getMediaPlayer().play();
                    mTPlayer.setPause(false);

                } else //pause
                {
                    if (mTPlayer.getMediaPlayer().getCurrentTime().toMillis() < mTPlayer.getMediaPlayer().getCycleDuration().toMillis())
                    {
                        //mTPlayer.getMediaPlayer().setAutoPlay(false);
                        //playTheSong(song);
                        timeline.pause();
                        mTPlayer.getMediaPlayer().pause();
                        mTPlayer.setPause(true);
                        //Pause song            
                    } else
                    {
                        playTheSong(songToPlay);
                    }
                }
            } else
            {
                //it is a new song. play the song
                mTPlayer.getMediaPlayer().stop();
                playTheSong(songToPlay);
            }
        } else
        {
            //no song playing, and no song has been played before
            playTheSong(songToPlay);

        }*/
    }

    /**
     * Plays the song that is passed in the parameter.
     *
     * @param song
     */
    private void playTheSong(Song song)
    {
        if (song != null)
        {
            if (playingSong == true)
            {
                timeline.stop();
            }
            songPlaying = song;
            playingSong = true;

            mTPlayer = new MyTunesPlayer(song.getFilePath());
            mTPlayer.getMediaPlayer().setAutoPlay(true);
            //Notify our Observer (
            setChanged();
            notifyObservers();
            lastSongIndex = currentIndex;

            startDelay(song);
        }
        else
        {
            return;
        }
        
    }

    /**
     * Starts a delay based on the duration of the song. When the delay is gone
     * it calls the "playNextSong" method
     *
     * @param song
     */
    private void startDelay(Song song)
    {
        timeline = new Timeline(new KeyFrame(Duration.millis((song.getDuration() * 1000)), ae -> playNextSong("next")));
        timeline.play();

        runningDelay = true;
    }

    /**
     * Returns the song playing
     *
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
        } else
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
     * Plays the next song. The parameter defines whether the next is song is
     * the previous, next or the same. Moves the playlists selection model to
     * the next song aswell
     *
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
                nextSong = getNextSongInCurrentList(songPlaying, previousNextOrRepeat);
            } else
            {
                nextSong = getNextSongInCurrentList(songPlaying, "repeat");
            }

        } 
        catch (IOException ex)
        {
            ex.printStackTrace();
        } 
        catch (UnsupportedAudioFileException ex)
        {
            ex.printStackTrace();
        }

        try
        {
            ListView<Song> playlist = (ListView) currentListControl;
            playlist.getSelectionModel().clearAndSelect(currentIndex);

        } catch (ClassCastException c)
        {
            TableView<Song> playlist = (TableView) currentListControl;
            playlist.getSelectionModel().clearAndSelect(currentIndex);
        }
        
        double vol = mTPlayer.getMediaPlayer().getVolume();
        playTheSong(nextSong);

        mTPlayer.getMediaPlayer().setVolume(vol);   
    }

    /**
     * Returns the next/previous or same song in the currently active list of
     * songs, depending on the String parameter given
     *
     * @param song
     * @return the next song
     */
    public Song getNextSongInCurrentList(Song currentSong, String previousNextOrRepeat) throws IOException, UnsupportedAudioFileException
    {
        Song nextSong;
        
        if (previousNextOrRepeat.equals("next"))
        {
            if (currentList.size() == 0)
            {
                mTPlayer.getMediaPlayer().stop();
                System.out.println("sasa");
                setChanged();
                notifyObservers();
                return null;
            }
            else if (currentIndex != currentList.size() - 1)
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
        else if (previousNextOrRepeat.equals("previous"))
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
    /**
     * Deletes the Song object from our collection, and passes the Song ID to our logic layer.
     * @param song Song to delete
     * @throws IOException 
     */
    public void deleteSong(Song song) throws IOException
    {

        mMgr.deleteSong(song.getId());
        songs.remove(song);
    }
    /**
     * Method to create a new (empty) playlist, or Edit a already existing playlist.
     * @param playlistToEdit Playlist object to edit
     * @param playlistName String of our playlist name
     * @throws IOException
     * @throws UnsupportedAudioFileException 
     */
    public void createNewPlaylist(Playlist playlistToEdit, String playlistName) throws IOException, UnsupportedAudioFileException
    {
        if (playlistToEdit == null)
        {
            Playlist playlistToAdd = mMgr.createNewPlaylist(playlistName);
            playlists.add(playlistToAdd);
        } else
        {
            playlistToEdit.setName(playlistName);
            mMgr.editPlaylistName(playlistToEdit);
            playlists.clear();
            playlists.addAll(mMgr.getAllPlayLists());
        }

    }
    /**
     * Get all the playlists in a List.
     * @return ObservableList Playlist objects
     */
    public ObservableList<Playlist> getAllPlaylists()
    {
        return playlists;
    }
    /**
     * Get all Songs in a List.
     * @return List Song
     */
    public ObservableList<Song> getAllSongsByPlaylistId()
    {
        return songsByPlaylistId;
    }
    /**
     * Deletes a playlist from our Collection and passes the Playlist ID to our logic layer.
     * @param playlist Playlist to delete
     * @throws IOException 
     */
    public void deletPlaylist(Playlist playlist) throws IOException
    {
        mMgr.deletePlaylist(playlist.getId());

        playlists.remove(playlist);
        songsByPlaylistId.clear();

    }
    /**
     * Fills the Collection with the selected playlist ID.
     * Gets all the Songs by playlist ID from our logic-layer.
     * @param playlistId Playlist ID to get all the songs matching this playlist
     * @throws UnsupportedAudioFileException
     * @throws IOException 
     */
    public void showPlaylistSongs(int playlistId) throws UnsupportedAudioFileException, IOException

    {
        songsByPlaylistId.clear();
        songsByPlaylistId.addAll(mMgr.getSongsByPlaylistId(playlistId));
    }
    /** 
     * Method to add a song to our playlist
     * @param songToAdd Song object to add
     * @param playlistToAddTo Playlist object the song should be added to.
     * @throws UnsupportedAudioFileException
     * @throws IOException 
     */
    public void addSongToPlaylist(Song songToAdd, Playlist playlistToAddTo) throws UnsupportedAudioFileException, IOException

    {
        //Sends only the ID on the song and playlist to our logic-layer.
        mMgr.addSongToPlaylist(songToAdd.getId(), playlistToAddTo.getId());
        
        //+1 to our value NumberOfSongsInPlaylist integer.
        playlistToAddTo.setNumberOfSongsInPlaylist(+1);
        
        //Load all the playlists again.
        playlists.clear();
        playlists.addAll(mMgr.getAllPlayLists());
        
        //Calls the method to show the songs in our playlist
        showPlaylistSongs(playlistToAddTo.getId());

    }
    /**
     * Method to filter songs
     * @param query Search string to filter our songs with
     * @return A List of songs with the met parameter.
     * @throws IOException 
     */
    public List<Song> filterSongs(String query) throws IOException
    {
        List<Song> songList = null;

        songList = mMgr.search(query);
        
        String songPlayingInfo = songPlaying.getAllSongStringInfo();
        
        if(currentList == songs)
        {
            for (int i = 0; i < songList.size(); i++)
            {
                if (songList.get(i).getAllSongStringInfo().equals(songPlayingInfo))
                {
                    currentIndex = i;
                    break;
                }
                else
                {
                   currentIndex = 0; 
                }
            }            
        }
        return songList;
    }
    /**
     * Method to set our Song objects in our Songs collection.
     * @param songList 
     */
    public void setSongs(List<Song> songList)
    {
        songs.clear();
        songs.addAll(songList);
    }
    /**
     * Method to move our selected song a index up.
     * @param songToMoveUp Song object to swap a index up in our collection.
     * @return 
     */
    public int moveSongUp(Song songToMoveUp)
    {
        int indexId = songsByPlaylistId.indexOf(songToMoveUp);
        System.out.println(indexId);
        if (indexId != 0)
        {
            Collections.swap(songsByPlaylistId, indexId - 1, indexId);

        } else
        {
            indexId += 1;
        }
        return indexId;

    }
    /**
     * Method to move a song down in our Collection.
     * @param songToMoveDown Song object to move down in our collection.
     * @return 
     */
    public int moveSongDown(Song songToMoveDown)
    {
        int indexId = songsByPlaylistId.indexOf(songToMoveDown);
        if (indexId != songsByPlaylistId.size() - 1)
        {
            Collections.swap(songsByPlaylistId, indexId + 1, indexId);

        } else
        {
            indexId -= 1;
        }
        return indexId;
    }
    /**
     * Method to edit the selected song
     * @param songToEdit Song to edit
     * @param fileSong File object where our song is stored.
     * @throws IOException
     * @throws UnsupportedAudioFileException 
     */
    public void editSong(Song songToEdit, File fileSong) throws IOException, UnsupportedAudioFileException
    {
        //Pulls out the selected Song object from our collection and stores it.
        Song songSong = (Song) songs.get(songs.indexOf(songToEdit));
        //Give it a new artist from our parameter
        songSong.setArtist(songToEdit.getArtist());
        //Give it a new title from our parameter
        songSong.setTitle(songToEdit.getTitle());
        //If we have picked a new File destination it sets the filepath to our new filepath.
        if (fileSong != null)
        {
            songSong.setFilePath(fileSong.getAbsolutePath());
        }
        //passes the song from our collection down to our logic-layer
        mMgr.saveEditedSong(songSong);
        //Updates the songs collection
        songs.clear();
        songs.addAll(mMgr.getAllSongs());

    }
    /**
     * Gets the MyTunesPlayer object.
     * @return MyTunesPlayer
     */
    public MyTunesPlayer getmTPlayer()
    {
        return mTPlayer;
    }
    /**
     * Method to delete a song from our playlist.
     * @param song Selected Song object to delete
     * @param playList Playlist from wich our song should be deleted from.
     * @throws IOException
     * @throws UnsupportedAudioFileException 
     */
    public void deleteSongInPlaylist(Song song, Playlist playList) throws IOException, UnsupportedAudioFileException
    {
        //Passes the Song ID and playlist ID to our logic-layer
        mMgr.deleteSongInPlayList(song.getId(), playList.getId());
        //Removes the song from our collection by playlist ID.
        songsByPlaylistId.remove(song);

        //Corrects the number of songs in playlist
        playList.setNumberOfSongsInPlaylist(-1);
        //Updates the view.
        playlists.clear();
        playlists.addAll(mMgr.getAllPlayLists());
        showPlaylistSongs(playList.getId());
    }
    /**
     * Method to seek our song, where in the durationbar we click with our mouse.
     * @param mouseClickedWidth Where we click on the duration (Divived value with the progressbar-width)
     */
    public void seekSong(double mouseClickedWidth)
    {
        //Makes a new Duration variable
        Duration newDuration = mTPlayer.getMediaPlayer().getTotalDuration().multiply(mouseClickedWidth);
        //Play the song from our new duration
        mTPlayer.getMediaPlayer().seek(newDuration);
        //Stops the timeline for our next song to play
        timeline.stop();
        //Defining a new duration to pass to our Timeline.
        Duration totalTime = mTPlayer.getMediaPlayer().getTotalDuration();
        Duration timeLeft = totalTime.subtract(newDuration);
        //Sets it with the new duration.
        timeline = new Timeline(new KeyFrame(Duration.millis((timeLeft.toMillis())), ae -> playNextSong("next")));
        timeline.play();
    }

}
