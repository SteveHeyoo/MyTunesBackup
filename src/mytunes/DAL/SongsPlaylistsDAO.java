/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mytunes.DAL;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import mytunes.BE.Playlist;
import mytunes.BE.Song;

/**
 *
 * @author Stefan-VpcEB3J1E
 */
public class SongsPlaylistsDAO
{

    private static final int ID_SIZE = Integer.BYTES;
    private static final int DURATION_SIZE = Double.BYTES;
    private static final int NAME_SIZE = 50;
    private static final int FILEPATH_SIZE = 200;
    private static final int WRITE_SIZE = ID_SIZE + DURATION_SIZE + (NAME_SIZE * 2) + FILEPATH_SIZE;
    private static final String FILE_PATH_SONGS = "Songs.dat";
    private static final String FILE_PATH_PLAYLISTS = "Playlists.dat";
    private static final int WRITE_SIZE_PLAYLIST = ID_SIZE + NAME_SIZE;

    public SongsPlaylistsDAO()
    {
    }

    public Song addSong(File file) throws IOException, UnsupportedAudioFileException
    {
        int nextId;
        double duration;
        String artist;
        String title;
        String tag;
        String filePath = file.getAbsolutePath();

        try (RandomAccessFile raff = new RandomAccessFile(filePath, "rw"))
        {
            byte[] artistByte = new byte[30];
            byte[] titleByte = new byte[30];
            byte[] tagByte = new byte[3];
            raff.seek(raff.length() - 128);
            raff.read(tagByte);
            tag = new String(tagByte).trim();
            //System.out.println("Tag:" + tag);
            raff.read(titleByte);

            if (tag.equals("TAG"))
            {
                title = new String(titleByte).trim();
            } else
            {
                title = file.getName();
            }

            raff.read(artistByte);

            if (tag.equals("TAG"))
            {
                artist = new String(artistByte).trim();
            } else
            {
                artist = "";
            }

        }
        try (RandomAccessFile raf = new RandomAccessFile(new File(FILE_PATH_SONGS), "rw"))
        {

            if (raf.length() == 0)
            {
                raf.writeInt(1);
                raf.seek(0);
            }
            nextId = raf.readInt(); //header
            raf.seek(raf.length() - raf.length());
            raf.writeInt(nextId + 1); //+1 on our nextid in our header
            raf.seek(raf.length());  // place the file pointer at the end of the file.
            raf.writeInt(nextId); // id for song
            duration = getDuration(file);
            raf.writeDouble(duration);    //writes the duration on the song
            raf.writeBytes(String.format("%-" + NAME_SIZE + "s", artist).substring(0, NAME_SIZE)); //Writes artist
            raf.writeBytes(String.format("%-" + NAME_SIZE + "s", title).substring(0, NAME_SIZE)); // Writes Title
            raf.writeBytes(String.format("%-" + FILEPATH_SIZE + "s", filePath).substring(0, FILEPATH_SIZE)); //write file_path
        }

        return (new Song(nextId, artist, title, filePath, duration));
    }

    /**
     * Method that calculates the duration of a song and returns the duration as
     * a double
     *
     * @param file
     * @return the duration of a song as a double
     * @throws IOException
     * @throws UnsupportedAudioFileException
     */
    private double getDuration(File file) throws IOException, UnsupportedAudioFileException
    {

        AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(file);

        Map<?, ?> properties = fileFormat.properties();
        String key = "duration";
        Long microseconds = (Long) properties.get(key);
        int mili = (int) (microseconds / 1000);
        double sec = (double) (mili / 1000);
        return sec;

//            int sec = (mili / 1000) % 60;
//            int min = (mili / 1000) / 60;
    }

    /**
     * Reads all the songs in the Songs.dat file and returns the songs as a list
     *
     * @return List of all songs
     * @throws IOException
     */
    public List<Song> getAllSongs() throws IOException
    {
        try (RandomAccessFile raf = new RandomAccessFile(new File(FILE_PATH_SONGS), "rw"))
        {
            List<Song> songList = new ArrayList<>();

            while (raf.getFilePointer() < raf.length())
            {
                Song songToadd = getOneSong(raf);
                if (songToadd.getId() != 0)
                {
                    songList.add(songToadd);
                }
            }
            return songList;
        }
    }

    /**
     * Reads the Songs.dat file where the filePointer currently is and returns a
     * Song with the read attributes. Used in getAllSongs method
     *
     * @param raf
     * @return Song
     * @throws IOException
     */
    private Song getOneSong(final RandomAccessFile raf) throws IOException
    {
        byte[] nameBytes = new byte[NAME_SIZE];
        byte[] pathBytes = new byte[FILEPATH_SIZE];

        if (raf.getFilePointer() == 0)
        {
            raf.seek(ID_SIZE);
        }
        int id = raf.readInt();

        double duration = raf.readDouble();

        raf.read(nameBytes);
        String artist = new String(nameBytes).trim();

        raf.read(nameBytes);
        String title = new String(nameBytes).trim();

        raf.read(pathBytes);
        String filePath = new String(pathBytes).trim();

        return new Song(id, artist, title, filePath, duration);
    }

    public List<Playlist> getAllPlayLists() throws IOException
    {
        try (RandomAccessFile raf = new RandomAccessFile(new File(FILE_PATH_PLAYLISTS), "rw"))
        {
            List<Playlist> playlistList = new ArrayList<>();

            while (raf.getFilePointer() < raf.length())
            {
                Playlist playlistToadd = getOnePlaylist(raf);
                if (playlistToadd.getId() != 0)
                {
                    playlistList.add(playlistToadd);
                }
            }
            return playlistList;
        }
    }

    private Playlist getOnePlaylist(final RandomAccessFile raf) throws IOException
    {
        byte[] nameBytes = new byte[NAME_SIZE];

        if (raf.getFilePointer() == 0)
        {
            raf.seek(ID_SIZE);
        }
        int id = raf.readInt();

        raf.read(nameBytes);
        String playListName = new String(nameBytes).trim();

        return new Playlist(id, playListName);
    }

    /**
     * Removes the song with the given id in our Songs.dat file by overwriting
     * all the data with zeros
     *
     * @param id
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void removeSongById(int id) throws FileNotFoundException, IOException
    {
        try (RandomAccessFile raf = new RandomAccessFile(new File(FILE_PATH_SONGS), "rw"))
        {
            for (int i = ID_SIZE; i < raf.length(); i += WRITE_SIZE)
            {
                raf.seek(i);
                int readId = raf.readInt();
                if (readId == id)
                {
                    raf.seek(i);
                    byte[] overWriteBytes = new byte[WRITE_SIZE];
                    raf.write(overWriteBytes);
                    return;
                }

            }
        }
    }

    /**
     * Removes one playlist by id
     *
     * @param id
     * @throws IOException
     */
    public void removePlayListById(int id) throws IOException
    {
        try (RandomAccessFile raf = new RandomAccessFile(new File(FILE_PATH_PLAYLISTS), "rw"))
        {
            for (int i = ID_SIZE; i < raf.length(); i += WRITE_SIZE_PLAYLIST)
            {
                raf.seek(i);
                int readId = raf.readInt();
                if (readId == id)
                {
                    raf.seek(i);
                    byte[] overWriteBytes = new byte[WRITE_SIZE_PLAYLIST];
                    raf.write(overWriteBytes);
                    return;
                }

            }
        }
    }

    public Playlist createNewPlaylist(String playlistName) throws IOException
    {
        int playlistId;

        try (RandomAccessFile raf = new RandomAccessFile(new File(FILE_PATH_PLAYLISTS), "rw"))
        {
            if (raf.length() == 0)
            {
                raf.writeInt(1);
                raf.seek(0);
            }
            playlistId = raf.readInt();
            raf.seek(0);
            raf.writeInt(playlistId + 1);
            raf.seek(raf.length());
            raf.writeInt(playlistId);
            raf.writeBytes(String.format("%-" + NAME_SIZE + "s", playlistName).substring(0, NAME_SIZE));

        }
        return new Playlist(playlistId, playlistName);

    }

    public void editPlaylistName(Playlist playlistToEdit) throws IOException
    {
        try (RandomAccessFile raf = new RandomAccessFile(new File(FILE_PATH_PLAYLISTS), "rw"))
        {
            for (int i = ID_SIZE; i < raf.length(); i += WRITE_SIZE_PLAYLIST)
            {
                raf.seek(i);
                int playlistId = raf.readInt();

                if (playlistId == playlistToEdit.getId())
                {
                    raf.writeBytes(String.format("%-" + NAME_SIZE + "s", playlistToEdit.getName()).substring(0, NAME_SIZE));
                }

            }

        }
    }

    public void editSong(Song songToSave) throws IOException, UnsupportedAudioFileException
    {
        try (RandomAccessFile raf = new RandomAccessFile(new File(FILE_PATH_SONGS), "rw"))
        {
            for (int i = ID_SIZE; i < raf.length(); i += WRITE_SIZE)
            {
                raf.seek(i);
                int readId = raf.readInt();

                if (readId == songToSave.getId())
                {
                    double duration = getDuration(new File(songToSave.getFilePath()));
                    raf.writeDouble(duration);    //writes the duration on the song
                    raf.writeBytes(String.format("%-" + NAME_SIZE + "s", songToSave.getArtist()).substring(0, NAME_SIZE)); //Writes artist
                    raf.writeBytes(String.format("%-" + NAME_SIZE + "s", songToSave.getTitle()).substring(0, NAME_SIZE)); // Writes Title
                    raf.writeBytes(String.format("%-" + FILEPATH_SIZE + "s", songToSave.getFilePath()).substring(0, FILEPATH_SIZE)); //write file_path
                }

            }
        }
    }
}
