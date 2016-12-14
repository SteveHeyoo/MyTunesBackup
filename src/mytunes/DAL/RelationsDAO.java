/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mytunes.DAL;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Stefan-VpcEB3J1E
 */
public class RelationsDAO
{

    private static final int ID_SIZE = Integer.BYTES;
    private static final int WRITE_SIZE = (ID_SIZE * 2);
    private static final String FILE_PATH_RELATIONS = "Relations.dat";

    
    /**
     * Checks the given playlist id and returns a list of the id's of the songs the playlist contains .
     * @param playlistID
     * @return
     * @throws IOException 
     */
    public List<Integer> getSongIdByPlaylistId(int playlistID) throws IOException
    {
        List<Integer> songIds = new ArrayList<>();
        try (RandomAccessFile raf = new RandomAccessFile(new File(FILE_PATH_RELATIONS), "rw"))
        {
            for (int i = 0; i < raf.length(); i += WRITE_SIZE)
            {
                raf.seek(i);
                int readPlaylistId = raf.readInt();

                if (readPlaylistId == playlistID)
                {
                    int songId = raf.readInt();
                    songIds.add(songId);

                }

            }
            return songIds;
        }

    }
    /**
     * Writes a new relation between a playlist and a song in the Relations.dat file.
     * @param songId
     * @param playlistId
     * @throws IOException 
     */
    public void addSongToPlaylist(int songId, int playlistId) throws IOException
    {
        try (RandomAccessFile raf = new RandomAccessFile(new File(FILE_PATH_RELATIONS), "rw"))
        {
            raf.seek(raf.length());
            raf.writeInt(playlistId);
            raf.writeInt(songId);
        }
    }
    
    /**
     * Deletes a relation between a playlist and a song in the Relations.dat file.
     * @param songId
     * @param playListId
     * @throws IOException 
     */
    public void deleteSongInPlayList(int songId, int playListId) throws IOException
    {
        try (RandomAccessFile raf = new RandomAccessFile(new File(FILE_PATH_RELATIONS), "rw"))
        {
                       
            for (int i = 0; i < raf.length(); i += WRITE_SIZE)
            {

                raf.seek(i);
                int readPlayListId = raf.readInt();
                int readSongId = raf.readInt();
                if (readPlayListId == playListId)
                {

                    
                    if (readSongId == songId)
                    {
                        raf.seek(i);
                        byte[] overWriteBytes = new byte[WRITE_SIZE];
                        raf.write(overWriteBytes);
                        return;
                    }                    
                    
                }           
            }
        }
    }

}
