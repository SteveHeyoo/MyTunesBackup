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

    public Song addSong(File file) throws IOException, UnsupportedAudioFileException
    {
        return sPlDAO.addSong(file);
    }

    public List<Song> getAllSongs() throws IOException
    {
        return sPlDAO.getAllSongs();
    }

    /**
     * Calls the removeSongById in the songDAO class
     *
     * @param id
     * @throws IOException
     */
    public void deleteSong(int id) throws IOException
    {
        sPlDAO.removeSongById(id);
    }

    public Playlist createNewPlaylist(String playlistName) throws IOException
    {
        return sPlDAO.createNewPlaylist(playlistName);
    }

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
                        //newSong = sPlDAO.addSong(file);
                        //returnList.add(newSong);
                        //System.out.println(song.getAllSongStringInfo());
                        returnList.add(song);
                    } else
                    {
                        returnList.add(song);
                    }

                    /*
                    for (Song songInReturnList : returnList)
                    {
                        if (returnList.size() != 0)
                        {
                            if (returnList.contains(song))
                            {
                                //System.out.println(songInReturnList.getAllSongStringInfo());
                            }
                        }
                        //duplicate.
                    }*/
                }
            }
        }

        return returnList;

    }

    public List<Integer> getSongIdByPlaylistId(int playlistId) throws IOException
    {
        return rDAO.getSongIdByPlaylistId(playlistId);
    }
    
    public void addSongToPlaylist(int songId, int playlistId) throws IOException
    {
        rDAO.addSongToPlaylist(songId, playlistId);

    }

    public List<Song> search(String query) throws FileNotFoundException, IOException
    {
        List<Song> allWords = getAllSongs();
        List<Song> searchList = new ArrayList<>();

        for (int i = 0; i < allWords.size(); i++)
        {
            if (allWords.get(i).getAllSongStringInfo().toLowerCase().contains(query.toLowerCase()))
            {
                searchList.add(allWords.get(i));
            }
        }

        return searchList;
    }

    public void editPlaylistName(Playlist playlistToEdit) throws IOException
    {
        sPlDAO.editPlaylistName(playlistToEdit);
    }

    public void saveEditedSong(Song songSong) throws IOException, UnsupportedAudioFileException
    {
        sPlDAO.editSong(songSong);
    }

}
