/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mytunes.GUI.CONTROLLER;

import com.jfoenix.controls.JFXProgressBar;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;

import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import mytunes.BE.Playlist;
import mytunes.BE.Song;
import mytunes.GUI.MODEL.Model;

/**
 *
 * @author Bo
 */
public class FXMLDocumentController implements Initializable, Observer
{

    private Model model;

    @FXML
    private TableView<Playlist> tblPlaylist;
    @FXML
    private TableColumn<Playlist, String> columnPlaylistName;
    @FXML
    private TableColumn<Playlist, Integer> columnPlaylistNumberOfSongs;
    @FXML
    private TableColumn<Playlist, String> columnPlaylistTotalDuration;

    @FXML
    private TableView<Song> tblSong;
    @FXML
    private TableColumn<Song, String> columnTitle;
    @FXML
    private TableColumn<Song, String> columnArtist;
    @FXML
    private TableColumn<Song, String> columnTime;
    @FXML
    private TableColumn<?, ?> columnCategory;

    @FXML
    private ListView<Song> listPlaylistSong;

    @FXML
    private TextField txtFieldSearch;

    @FXML
    private Slider volumeSlide;

    private Song currentSong;
    private Control currentControlList;
    @FXML
    private Label lblDuration;
    @FXML
    private JFXProgressBar progressbarDuration;

    public FXMLDocumentController()
    {
        model = Model.getInstance();
        model.addObserver(this);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        volumeSlide.setValue(100);
        dataBind();

    }

    private void dataBind()
    {
        //I define the mapping of the table's columns to the objects that are added to it.
        columnTitle.setCellValueFactory(value -> new SimpleObjectProperty<>(value.getValue().getTitle()));
        columnArtist.setCellValueFactory(value -> new SimpleObjectProperty<>(value.getValue().getArtist()));
        columnTime.setCellValueFactory(value -> new SimpleObjectProperty<>(value.getValue().getDurationInMinutes()));
        columnPlaylistName.setCellValueFactory(value -> new SimpleObjectProperty<>(value.getValue().getName()));
        columnPlaylistNumberOfSongs.setCellValueFactory(value -> new SimpleObjectProperty<>(value.getValue().getNumberOfSongsInPlaylist()));
        columnPlaylistTotalDuration.setCellValueFactory(value -> new SimpleObjectProperty<>(value.getValue().getPlaylistDuration()));
        //I bind the table to a list of data (Empty at startup):
        tblSong.setItems(model.getAllSongs());
        tblPlaylist.setItems(model.getAllPlaylists());
        listPlaylistSong.setItems(model.getAllSongsByPlaylistId());

        volumeSlide.valueProperty().addListener((javafx.beans.Observable observable)
                -> 
                {
                    if (model.getmTPlayer() != null)
                    {
                        model.getmTPlayer().getMediaPlayer().setVolume(volumeSlide.getValue() / 100);
                    }
        });
    }

    @FXML
    private void handleNewSong(ActionEvent event)
    {
        /*
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter mp3Filter = new FileChooser.ExtensionFilter("MP3 Files(*.mp3)", "*.mp3");
        fileChooser.getExtensionFilters().add(mp3Filter);
        File file = fileChooser.showOpenDialog(null);

        if (file != null)
        {
            try
            {
                model.createNewSong(file);
            } catch (IOException iOEx)
            {
                showAlert("IOException", iOEx.getMessage());
            } catch (UnsupportedAudioFileException ex)
            {
                showAlert("UnsupportedAudioFileException", ex.getMessage());
            }
        }*/

        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("MP3 Files(*.mp3)", "mp3");
        chooser.setFileFilter(filter);
        chooser.showOpenDialog(null);

        File[] files = chooser.getSelectedFiles();

        if (files != null)
        {
            for (int i = 0; i < files.length; i++)
            {
                try
                {
                    model.createNewSong(files[i]);
                } catch (IOException iOEx)
                {
                    showAlert("IOException", iOEx.getMessage());
                } catch (UnsupportedAudioFileException ex)
                {
                    showAlert("UnsupportedAudioFileException", ex.getMessage());
                }
            }

        }
    }

    private void loadSongDataView(Song song) throws IOException
    {
        // Fetches primary stage and gets loader and loads FXML file to Parent
        Stage primStage = (Stage) tblSong.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/mytunes/GUI/VIEW/SongEdit.fxml"));
        Parent root = loader.load();

        // Fetches controller from patient view
        SongEditController songEditController
                = loader.getController();

        songEditController.setSong(song);

        // Sets new stage as modal window
        Stage stageSongEdit = new Stage();
        stageSongEdit.setScene(new Scene(root));

        stageSongEdit.initModality(Modality.WINDOW_MODAL);
        stageSongEdit.initOwner(primStage);

        stageSongEdit.show();
    }

    @FXML
    private void handleTblViewMouseClick(MouseEvent event)
    {
        currentSong = tblSong.getSelectionModel().getSelectedItem();
        currentControlList = tblSong;
        model.setSelectedIndex(tblSong.getSelectionModel().getSelectedIndex());
        if (event.getClickCount() == 2 && currentSong != null)
        {
            model.setIndex(tblSong.getSelectionModel().getSelectedIndex());
            model.setCurrentListControl(currentControlList);
            model.playSong(currentSong);
            model.getmTPlayer().getMediaPlayer().setVolume(volumeSlide.getValue() / 100);
            //  mediaBinding();

        }

    }

    @FXML
    private void handleTblViewSongsDelete(ActionEvent event)
    {
        Song song = tblSong.getSelectionModel().getSelectedItem();
        try
        {
            if (song != null)
            {
                model.deleteSong(song);
            }
        } catch (IOException ex)
        {
            showAlert("IOException", ex.getMessage());
        }
    }

    @FXML
    private void handleNewPlaylist(ActionEvent event) throws IOException
    {
        showNewEditPlaylistDialog(null);

    }

    @FXML
    private void handleDeletePlayList(ActionEvent event)
    {
        Playlist playlist = tblPlaylist.getSelectionModel().getSelectedItem();
        try
        {
            if (playlist != null)
            {
                model.deletPlaylist(playlist);
            }

        } catch (IOException ex)
        {
            showAlert("IOException", ex.getMessage());
        }
    }

    @FXML
    private void handleSongEdit(ActionEvent event)
    {
        Song song = tblSong.getSelectionModel().getSelectedItem();
        try
        {
            if (song != null)
            {
                loadSongDataView(song);
            }
        } catch (IOException ex)
        {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void handleShowPlaylistSongs(MouseEvent event)
    {
        Playlist playlist = tblPlaylist.getSelectionModel().getSelectedItem();

        if (playlist != null)
        {
            int playlistId = playlist.getId();
            try
            {
                model.setCurrentPlaylist(playlist);
                model.showPlaylistSongs(playlistId);

            } catch (IOException ex)
            {
                showAlert("IOException", ex.getMessage());
            } catch (UnsupportedAudioFileException ex)
            {
                showAlert("UnsupportedAudioFileException", ex.getMessage());
            }

        }

        if (event.getClickCount() == 2 && playlist != null)
        {
            try
            {
                showNewEditPlaylistDialog(playlist);
            } catch (IOException ex)
            {
                showAlert("IOException", ex.getMessage());
            }
        }
    }

    @FXML
    private void handleAddSongToPlaylist(ActionEvent event)
    {
        Song songToAdd = tblSong.getSelectionModel().getSelectedItem();
        Playlist playlistToAddTo = tblPlaylist.getSelectionModel().getSelectedItem();
        int plIndexNum = tblPlaylist.getSelectionModel().getSelectedIndex();

        try
        {
            if (songToAdd != null && playlistToAddTo != null)
            {

                model.addSongToPlaylist(songToAdd, playlistToAddTo);
            }
        } catch (IOException ex)
        {
            showAlert("IOException", ex.getMessage());
        } catch (UnsupportedAudioFileException ex)
        {
            showAlert("UnsupportedAudioFileException", ex.getMessage());
        }
        tblPlaylist.getSelectionModel().clearAndSelect(plIndexNum);
    }

    @FXML
    private void handleSongsOnPlaylistPlay(MouseEvent event)
    {
        currentSong = listPlaylistSong.getSelectionModel().getSelectedItem();
        currentControlList = listPlaylistSong;
        model.setSelectedIndex(listPlaylistSong.getSelectionModel().getSelectedIndex());
        if (event.getClickCount() == 2 && currentSong != null)
        {
            model.setIndex(listPlaylistSong.getSelectionModel().getSelectedIndex());
            model.setCurrentListControl(currentControlList);
            model.playSong(currentSong);
            model.getmTPlayer().getMediaPlayer().setVolume(volumeSlide.getValue() / 100);

            //currentControlList = listPlaylistSong;
        }
    }

    @FXML
    private void handleSearch3(KeyEvent event)
    {
        String query = txtFieldSearch.getText().trim();

        List<Song> searchResult = null;
        try
        {
            searchResult = model.filterSongs(query);
        } catch (IOException ex)
        {
            showAlert("IOException", ex.getMessage());
        }
        model.setSongs(searchResult);
    }

    @FXML

    private void handleMoveSongUp(ActionEvent event)
    {
        Song songToMoveUp = listPlaylistSong.getSelectionModel().getSelectedItem();

        if (songToMoveUp != null)
        {
            listPlaylistSong.getSelectionModel().clearAndSelect(model.moveSongUp(songToMoveUp) - 1);

        }
    }

    @FXML
    private void handleMoveSongDown(ActionEvent event)
    {
        Song songToMoveDown = listPlaylistSong.getSelectionModel().getSelectedItem();
        System.out.println(songToMoveDown);

        if (songToMoveDown != null)
        {
            listPlaylistSong.getSelectionModel().clearAndSelect(model.moveSongDown(songToMoveDown) + 1);
        }
    }

    @FXML
    private void handlePlayButton(ActionEvent event)
    {
        if (tblSong.getSelectionModel().getSelectedItem() != null || listPlaylistSong.getSelectionModel().getSelectedItem() != null)
        {
            model.setCurrentListControl(currentControlList);
            model.playSongButtonClick();
            model.getmTPlayer().getMediaPlayer().setVolume(volumeSlide.getValue() / 100);

            try
            {
                ListView<Song> playlist = (ListView) currentControlList;
                model.setIndex(playlist.getSelectionModel().getSelectedIndex());

            } catch (ClassCastException c)
            {
                TableView<Song> playlist = (TableView) currentControlList;
                model.setIndex(playlist.getSelectionModel().getSelectedIndex());

            }
        }

        //btnPlaySong.setText("Pause");
    }

    private void showNewEditPlaylistDialog(Playlist playlist) throws IOException
    {
        // TODO Display the New/Edit gui to enter a name to the new playlist
        Stage primStage = (Stage) tblSong.getScene().getWindow();

        //mvc pattern til fxml sti
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/mytunes/GUI/VIEW/NewEditPlaylistView.fxml"));

        Parent root = loader.load();

        //Fethes controller from patient view
        NewEditPlaylistViewController newEditController = loader.getController();
        if (playlist != null)
        {
            newEditController.setPlaylistToEdit(playlist);

        }

        // sets new stage as modal window
        Stage stageNewEditPlaylist = new Stage();
        stageNewEditPlaylist.setScene(new Scene(root));
        stageNewEditPlaylist.initModality(Modality.WINDOW_MODAL);
        stageNewEditPlaylist.initOwner(primStage);
        stageNewEditPlaylist.setResizable(false);

        stageNewEditPlaylist.show();
    }

    @FXML
    private void handleEditPlaylist(ActionEvent event) throws IOException
    {
        Playlist playlist = tblPlaylist.getSelectionModel().getSelectedItem();
        if (playlist != null)
        {
            showNewEditPlaylistDialog(playlist);
        }

    }

    public static void showAlert(String header, String body)
    {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("Warning Dialog");
        alert.setHeaderText(header);
        alert.setContentText(body);

        alert.showAndWait();
    }

    @FXML
    private void handlePlayNextSong(ActionEvent event)
    {
        if (tblSong.getSelectionModel().getSelectedItem() != null || listPlaylistSong.getSelectionModel().getSelectedItem() != null)
        {
            model.pressNextButton();
            model.getmTPlayer().getMediaPlayer().setVolume(volumeSlide.getValue() / 100);

        }

    }

    @FXML
    private void handlePlayPreviousSong(ActionEvent event)
    {
        if (tblSong.getSelectionModel().getSelectedItem() != null || listPlaylistSong.getSelectionModel().getSelectedItem() != null)
        {
            model.pressPreviousButton();
            model.getmTPlayer().getMediaPlayer().setVolume(volumeSlide.getValue() / 100);
        }

    }

    private void handleRadioReapetSong(ActionEvent event)
    {
        model.setRepeatSong(!model.getRepeatSong());
    }

    private void bindPlayerToGUI()
    {
        // Binds the currentTimeProperty to a StringProperty on the label
        // The computeValue() calculates minutes and seconds from the
        // CurrentTimeProperty, which is a javafx Duration type.
        lblDuration.textProperty().bind(
                new StringBinding()
        {
            // Initialization block 
            // Somewhat like a constructor without arguments
            {
                // Makes the StringBinding listen for changes to 
                // the currentTimeProperty
                super.bind(model.getmTPlayer().getMediaPlayer().currentTimeProperty());
            }

            @Override
            protected String computeValue()
            {

                String form = String.format("%d min, %d sec",
                        TimeUnit.MILLISECONDS.toMinutes((long) model.getmTPlayer().getMediaPlayer().getCurrentTime().toMillis()),
                        TimeUnit.MILLISECONDS.toSeconds((long) model.getmTPlayer().getMediaPlayer().getCurrentTime().toMillis())
                        - TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(
                                        (long) model.getmTPlayer().getMediaPlayer().getCurrentTime().toMillis()
                                )
                        )
                );

                return form;
            }
        });

        progressbarDuration.progressProperty().bind(new ObjectBinding<Number>()
        {
            {
                super.bind(model.getmTPlayer().getMediaPlayer().currentTimeProperty());
                progressbarDuration.maxWidthProperty().set(model.getmTPlayer().getMediaPlayer().getTotalDuration().toMillis());
                //  progressbarDuration.maxWidthProperty().set(model.getmTPlayer().getMediaPlayer().getTotalDuration().toMillis());
            }

            @Override
            protected Number computeValue()
            {

                return (model.getmTPlayer().getMediaPlayer().getCurrentTime().toMillis() / model.getmTPlayer().getMediaPlayer().getTotalDuration().toMillis());

            }
        });
    }

    private void handleDragDropFiles(DragEvent event)
    {
        System.out.println("Drag&Dropped a item!");
    }

    @FXML
    private void handleDeleteSongInPlaylist(ActionEvent event)
    {
        if (listPlaylistSong.getSelectionModel().getSelectedItem() == null)
        {
            return;
        }
        int plIndexNum = tblPlaylist.getSelectionModel().getSelectedIndex();
        int selectedSongIndex = listPlaylistSong.getSelectionModel().getSelectedIndex();
        try
        {
            model.deleteSongInPlaylist(listPlaylistSong.getSelectionModel().getSelectedItem(), tblPlaylist.getSelectionModel().getSelectedItem());
        } catch (IOException ex)
        {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedAudioFileException ex)
        {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
        tblPlaylist.getSelectionModel().clearAndSelect(plIndexNum);
        listPlaylistSong.getSelectionModel().clearAndSelect(selectedSongIndex);
    }

    @FXML
    private void handleRadioRepeatSong(ActionEvent event)
    {
        model.setRepeatSong(!model.getRepeatSong());
    }

    @Override
    public void update(Observable o, Object arg)
    {
        bindPlayerToGUI();

    }

    @FXML
    private void handleDragDropped(DragEvent event)
    {
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasFiles())
        {
            success = true;
            for (File file : db.getFiles())
            {
                try
                {
                    model.createNewSong(file);
                } catch (IOException ex)
                {
                    showAlert("IOException", "File must be .mp3 files!");
                } catch (UnsupportedAudioFileException ex)
                {
                    showAlert("UnsupportedAudioFileException", ex.getMessage());
                }
            }
        }
        event.setDropCompleted(success);
        event.consume();
    }

    @FXML
    private void handleDragOver(DragEvent event)
    {
        Dragboard db = event.getDragboard();
        if (db.hasFiles())
        {
            event.acceptTransferModes(TransferMode.COPY);
        } else
        {
            event.consume();
        }
    }

    @FXML
    private void handleSeekDurationDragged(MouseEvent event)
    {
        double mouseClickedWidth = event.getX();
        double progressbarWidth = progressbarDuration.getWidth();

        model.seekSong((mouseClickedWidth / progressbarWidth));
    }

    @FXML
    private void handleSeekDurationPressed(MouseEvent event)
    {
        double mouseClickedWidth = event.getX();
        double progressbarWidth = progressbarDuration.getWidth();

        model.seekSong((mouseClickedWidth / progressbarWidth));
    }
}
