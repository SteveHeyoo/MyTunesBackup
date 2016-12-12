/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mytunes.BLL;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.sound.sampled.UnsupportedAudioFileException;
import mytunes.BE.Playlist;
import mytunes.BE.Song;
import mytunes.DAL.RelationsDAO;
import mytunes.DAL.SongsPlaylistsDAO;

/**
 *
 * @author Stefan-VpcEB3J1E
 */
public class MusicManager
{

    private static final String FILE_NAME = "Songs.dat";

    private SongsPlaylistsDAO sPlDAO;
    private RelationsDAO rDAO;

    
    public MusicManager()
    {
        sPlDAO = new SongsPlaylistsDAO();
        rDAO = new RelationsDAO();
    }

    /**
     * Adds a new song. Takes a mp3 file, and returns the song added as a Song object
     * @param file
     * @return
     * @throws IOException
     * @throws UnsupportedAudioFileException 
     */
    public Song addSong(File file) throws IOException, UnsupportedAudioFileException
    {
        return sPlDAO.addSong(file);
    }

    /**
     * Returns a list of all songs
     * @return
     * @throws IOException 
     */
    public List<Song> getAllSongs() throws IOException
    {
        return sPlDAO.getAllSongs();
    }

    /**
     * Takes a song id and removes the song with that id
     * @param id
     * @throws IOException
     */
    public void deleteSong(int id) throws IOException
    {
        sPlDAO.removeSongById(id);
    }

    /**
     * Takes a name(String) and creates a playlist with that name
     * @param playlistName
     * @return
     * @throws IOException 
     */
    public Playlist createNewPlaylist(String playlistName) throws IOException
    {
        return sPlDAO.createNewPlaylist(playlistName);
    }

    /**
     * Returns a list of all playlists. Updates the durations of all playlists.
     * @return
     * @throws IOException
     * @throws UnsupportedAudioFileException 
     */
    public List<Playlist> getAllPlayLists() throws IOException, UnsupportedAudioFileException
    {
        List<Playlist> playlists = sPlDAO.getAllPlayLists();

        for (Playlist playlist : playlists)
        {
            double totalDuration = 0;
            playlist.setNumberOfSongsInPlaylist(getSongsByPlaylistId(playlist.getId()).size());

            for (Song songInPlaylist : getSongsByPlaylistId(playlist.getId()))
            {
                totalDuration += songInPlaylist.getDuration();
            }
            long microseconds = (long) totalDuration * 1000000;
            int mili = (int) (microseconds / 1000);

            int sec = (mili / 1000) % 60;
            int min = ((mili / 1000) / 60);
            int minToShow = (int) (long) (TimeUnit.MILLISECONDS.toMinutes(mili) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(mili)));

            playlist.setPlaylistDuration(min / 60 + ":" + minToShow + ":" + sec);
        }

        return playlists;
    }

    /**
     * Calls the removePlaylistById in the songDAO class
     *
     * @param id
     * @throws IOException
     */
    public void deletePlaylist(int id) throws IOException
    {
        sPlDAO.removePlayListById(id);
    }

    /**
     * Takes a playlist id(Int). Returns all songs that are connected to that playlist id.
     * @param playlistId
     * @return
     * @throws IOException
     * @throws UnsupportedAudioFileException 
     */
    public List<Song> getSongsByPlaylistId(int playlistId) throws IOException, UnsupportedAudioFileException
    {
        List<Song> returnList = new ArrayList<>();
        List<Song> allSongs = sPlDAO.getAllSongs();
        List<Integer> songsWithPlaylistId = rDAO.getSongIdByPlaylistId(playlistId);

        for (Integer songId : songsWithPlaylistId)
        {
            for (Song song : allSongs)
            {
                int readSongId = song.getId();

                if (readSongId == songId)
                {

                    if (returnList.contains(song))
                    {
                        Song newSong;
                        String filePath = song.getFilePath();
                        File file = new File(filePath);
                        returnList.add(song);
                    } 
                    else
                    {
                        returnList.add(song);
                    }
                }
            }
        }

        return returnList;

    }

    /**
     * Checks the given playlist id and returns a list of the id's of the songs the playlist contains.
     * @param playlistId
     * @return
     * @throws IOException 
     */
    public List<Integer> getSongIdByPlaylistId(int playlistId) throws IOException
    {
        return rDAO.getSongIdByPlaylistId(playlistId);
    }
    
    /**
     * Writes a new relation between a playlist and a song in the Relations.dat file.
     * @param songId
     * @param playlistId
     * @throws IOException 
     */
    public void addSongToPlaylist(int songId, int playlistId) throws IOException
    {
        rDAO.addSongToPlaylist(songId, playlistId);
    }

    /**
     * Takes a query(String) and returns a list of the songs which contains the query in their song information.
     * @param query
     * @return
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public List<Song> search(String query) throws FileNotFoundException, IOException
    {
        List<Song> allSongs = getAllSongs();
        List<Song> searchList = new ArrayList<>();

        for (int i = 0; i < allSongs.size(); i++)
        {
            if (allSongs.get(i).getAllSongStringInfo().toLowerCase().contains(query.toLowerCase()))
            {
                searchList.add(allSongs.get(i));
            }
        }

        return searchList;
    }

    /**
     * Updates the given playlists name
     * @param playlistToEdit
     * @throws IOException 
     */
    public void editPlaylistName(Playlist playlistToEdit) throws IOException
    {
        sPlDAO.editPlaylistName(playlistToEdit);
    }

    /**
     * Updates the given songs information
     * @param songSong
     * @throws IOException
     * @throws UnsupportedAudioFileException 
     */
    public void saveEditedSong(Song songSong) throws IOException, UnsupportedAudioFileException
    {
        sPlDAO.editSong(songSong);
    }

    /**
     * Removes a relation between a song and a playlist.
     * @param songId
     * @param playListId
     * @throws IOException 
     */
    public void deleteSongInPlayList(int songId, int playListId) throws IOException
    {
        rDAO.deleteSongInPlayList(songId,playListId);
    }

}
