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

    public void addSongToPlaylist(int songId, int playlistId) throws IOException
    {
        try (RandomAccessFile raf = new RandomAccessFile(new File(FILE_PATH_RELATIONS), "rw"))
        {
            raf.seek(raf.length());
            raf.writeInt(playlistId);
            raf.writeInt(songId);
        }
    }

}
