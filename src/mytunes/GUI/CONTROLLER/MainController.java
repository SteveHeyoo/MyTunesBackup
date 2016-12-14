/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mytunes.GUI.CONTROLLER;

import com.jfoenix.controls.JFXButton;
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
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.media.MediaPlayer;
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
public class MainController implements Initializable, Observer
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
    @FXML
    private Label lblTotalDuration;
    @FXML
    private Label lblSongPlaying;
    @FXML
    private JFXButton btnPlayPause;

    /**
     * Contructor of controller Sets this controller as observer for our model.
     */
    public MainController()
    {
        model = Model.getInstance();
        model.addObserver(this);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        volumeSlide.setValue(100);
        dataBind();
        bindContextMenu();

    }

    /**
     * Databind our columns to our Song and Playlists using Lambda expressions.
     * Adds a event listener to our volume slider.
     */
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

    /**
     * Binds a context menu with an Add and Edit option, to our tableview with
     * all songs.
     */
    private void bindContextMenu()
    {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem addTo = new MenuItem("Add to selected playlist");
        MenuItem edit = new MenuItem("Edit");
        contextMenu.getItems().addAll(addTo, edit);

        tblSong.setContextMenu(contextMenu);

        addTo.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                handleAddSongToPlaylist(event);
            }
        });
        edit.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                handleSongEdit(event);
            }
        });
    }

    /**
     * Using the JFileChooser to choose wich files to be added to our songs
     * table.
     *
     * @param event
     */
    @FXML
    private void handleNewSong(ActionEvent event)
    {
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

    /**
     * Loads the view to edit our selected Song.
     *
     * @param song
     * @throws IOException
     */
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

    /**
     * Handles the mouse event of the All-songs table
     *
     * @param event
     */
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
            model.setCurrentList(model.getAllSongs());
        }
        if (event.getButton() == MouseButton.SECONDARY && currentSong != null)
        {

            tblSong.getContextMenu().show(tblSong, event.getSceneX(), event.getSceneY());
        }

    }

    /**
     * Handles the delete button on our all-songs table.
     *
     * @param event
     */
    @FXML
    private void handleTblViewSongsDelete(ActionEvent event)
    {

        Song song = tblSong.getSelectionModel().getSelectedItem();
        try
        {
            if (song != null)
            {

                Alert alert = new Alert(AlertType.CONFIRMATION);
                alert.setTitle("Delete confirmation");
                alert.setHeaderText("Confirm removing");
                alert.setContentText("You really really want to delete: " + song.toString() + "?");

                alert.showAndWait();

                if (alert.getResult() == ButtonType.OK)
                {
                    model.deleteSong(song);
                } else
                {
                    return;
                }
            }
        } catch (IOException ex)
        {
            showAlert("IOException", ex.getMessage());
        }
    }

    /**
     * Handles the new button in the Playlist table.
     *
     * @param event
     * @throws IOException
     */
    @FXML
    private void handleNewPlaylist(ActionEvent event) throws IOException
    {
        showNewEditPlaylistDialog(null);

    }

    /**
     * Handles the delete button in the playlist table.
     *
     * @param event
     */
    @FXML
    private void handleDeletePlayList(ActionEvent event)
    {
        Playlist playlist = tblPlaylist.getSelectionModel().getSelectedItem();
        try
        {
            if (playlist != null)
            {
                Alert alert = new Alert(AlertType.CONFIRMATION);
                alert.setTitle("Delete confirmation");
                alert.setHeaderText("Confirm removing");
                alert.setContentText("You really really want to delete: " + playlist.getName() + "?");

                alert.showAndWait();
                if (alert.getResult() == ButtonType.OK)
                {
                    model.deletPlaylist(playlist);
                } else
                {
                    return;
                }
            }

        } catch (IOException ex)
        {
            showAlert("IOException", ex.getMessage());
        }
    }

    /**
     * Handles the edit button in all-songs table.
     *
     * @param event
     */
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
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Handles the click event to show songs on the seleceted playlist.
     *
     * @param event
     */
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

    /**
     * Handles the "add" button, wich adds a song to the selected playlist.
     *
     * @param event
     */
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

    /**
     * Handles the mouse event on our Listview. The view that displays songs on
     * a selected playlist.
     *
     * @param event
     */
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

            model.setCurrentList(model.getAllSongsByPlaylistId());
        }
    }

    /**
     * Our well selected method-name, to handle the search function. Or filter
     * songs.
     *
     * @param event
     */
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

        tblSong.getSelectionModel().clearAndSelect(model.getCurrentIndex());
    }

    /**
     * The arrow button to move a song up in the list.
     *
     * @param event
     */
    @FXML
    private void handleMoveSongUp(ActionEvent event)
    {
        Song songToMoveUp = listPlaylistSong.getSelectionModel().getSelectedItem();

        if (songToMoveUp != null)
        {
            listPlaylistSong.getSelectionModel().clearAndSelect(model.moveSongUp(songToMoveUp) - 1);

        }
    }

    /**
     * The arrow button to move a song down in the list.
     *
     * @param event
     */
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

    /**
     * Handles the play/pause button.
     *
     * @param event
     */
    @FXML
    private void handlePlayButton(ActionEvent event)
    {
        if (tblSong.getSelectionModel().getSelectedItem() != null || listPlaylistSong.getSelectionModel().getSelectedItem() != null)
        {
            model.setCurrentListControl(currentControlList);
            model.playSongButtonClick();
            if (model.getmTPlayer() != null)
            {
                model.getmTPlayer().getMediaPlayer().setVolume(volumeSlide.getValue() / 100);
            }

            try
            {
                ListView<Song> playlist = (ListView) currentControlList;
                model.setIndex(playlist.getSelectionModel().getSelectedIndex());
                model.setCurrentList(model.getAllSongsByPlaylistId());

            } catch (ClassCastException c)
            {
                TableView<Song> playlist = (TableView) currentControlList;
                model.setIndex(playlist.getSelectionModel().getSelectedIndex());
                model.setCurrentList(model.getAllSongs());
            }
        }
    }

    /**
     * Method to show a new view, to either save a new playlist, or save a
     * edited playlist.
     *
     * @param playlist
     * @throws IOException
     */
    private void showNewEditPlaylistDialog(Playlist playlist) throws IOException
    {
        // TODO Display the New/Edit gui to enter a name to the new playlist
        Stage primStage = (Stage) tblSong.getScene().getWindow();

        //mvc pattern to fxml path
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/mytunes/GUI/VIEW/NewEditPlaylistView.fxml"));

        Parent root = loader.load();

        //Fethes controller
        NewEditPlaylistViewController newEditController = loader.getController();
        //Here we decide if it is a new or a already existing playlist to edit.
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

    /**
     * Button to edit a playlist
     *
     * @param event
     * @throws IOException
     */
    @FXML
    private void handleEditPlaylist(ActionEvent event) throws IOException
    {
        Playlist playlist = tblPlaylist.getSelectionModel().getSelectedItem();
        if (playlist != null)
        {
            showNewEditPlaylistDialog(playlist);
        }

    }

    /**
     * Static method to show the exception dialog.
     *
     * @param header
     * @param body
     */
    public static void showAlert(String header, String body)
    {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("Warning Dialog");
        alert.setHeaderText(header);
        alert.setContentText(body);

        alert.showAndWait();
    }

    /**
     * Next button.
     *
     * @param event
     */
    @FXML
    private void handlePlayNextSong(ActionEvent event)
    {
        if (tblSong.getSelectionModel().getSelectedItem() != null || listPlaylistSong.getSelectionModel().getSelectedItem() != null)
        {
            model.pressNextButton();
            model.getmTPlayer().getMediaPlayer().setVolume(volumeSlide.getValue() / 100);

        }

    }

    /**
     * Previous button
     *
     * @param event
     */
    @FXML
    private void handlePlayPreviousSong(ActionEvent event)
    {
        if (tblSong.getSelectionModel().getSelectedItem() != null || listPlaylistSong.getSelectionModel().getSelectedItem() != null)
        {
            model.pressPreviousButton();
            model.getmTPlayer().getMediaPlayer().setVolume(volumeSlide.getValue() / 100);
        }

    }

    /**
     * Method to show current time status of the song, and show how long the
     * song is played in the progressbar.
     */
    private void bindPlayerToGUI()
    {
        // Binds the currentTimeProperty to a StringProperty on the label
        // The computeValue() calculates minutes and seconds from the
        // CurrentTimeProperty, which is a javafx Duration type.
        lblDuration.setAlignment(Pos.CENTER_RIGHT);
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

                String form = String.format("%d:%d",
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
        lblTotalDuration.setAlignment(Pos.CENTER_LEFT);
        lblTotalDuration.textProperty().bind(new StringBinding()
        {
            {
                super.bind(model.getmTPlayer().getMediaPlayer().currentTimeProperty());
            }

            @Override
            protected String computeValue()
            {
                String form = String.format("%d:%d",
                        TimeUnit.MILLISECONDS.toMinutes((long) model.getmTPlayer().getMediaPlayer().getTotalDuration().toMillis()),
                        TimeUnit.MILLISECONDS.toSeconds((long) model.getmTPlayer().getMediaPlayer().getTotalDuration().toMillis())
                        - TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(
                                        (long) model.getmTPlayer().getMediaPlayer().getTotalDuration().toMillis()
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
        lblSongPlaying.setAlignment(Pos.CENTER);
        lblSongPlaying.textProperty().bind(new StringBinding()
        {
            @Override
            protected String computeValue()
            {
                System.out.println(model.getmTPlayer().getMediaPlayer().getStatus());
                if (model.getmTPlayer().getMediaPlayer().getStatus() == MediaPlayer.Status.PLAYING)
                {
                    return "";
                }
                else
                {
                    return "(" + model.getSongPlaying().toString() + ")... Is playing";
                }
                
            }
        });
        btnPlayPause.textProperty().bind(new StringBinding()
        {
                {
                    super.bind(model.getmTPlayer().getMediaPlayer().statusProperty());
                }
            @Override
            protected String computeValue()
            {
                if (model.getmTPlayer().getMediaPlayer().getStatus() == MediaPlayer.Status.PAUSED || model.getmTPlayer().getMediaPlayer().getStatus() == MediaPlayer.Status.READY || model.getmTPlayer().getMediaPlayer().getStatus() == MediaPlayer.Status.STOPPED)
                {
                    return "Play";
                }
                else
                {
                    return "Pause";
                }
            }
        });
    }

    /**
     * Button to delete a song in the listview
     *
     * @param event
     */
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
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedAudioFileException ex)
        {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
        tblPlaylist.getSelectionModel().clearAndSelect(plIndexNum);
        listPlaylistSong.getSelectionModel().clearAndSelect(selectedSongIndex);
    }

    /**
     * Radio button.
     *
     * @param event
     */
    @FXML
    private void handleRadioRepeatSong(ActionEvent event)
    {
        model.setRepeatSong(!model.getRepeatSong());
    }

    /**
     * The method that is called when the NotifyObservers() is called in the
     * Model.
     *
     * @param o
     * @param arg
     */
    @Override
    public void update(Observable o, Object arg)
    {
        bindPlayerToGUI();

    }

    /**
     * Drag&Drop function.
     *
     * @param event
     */
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

    /**
     * Drag&Drop function.
     *
     * @param event
     */
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

    /**
     * Mouse event to handle the seek duration
     *
     * @param event
     */
    @FXML
    private void handleSeekDurationDragged(MouseEvent event)
    {
        double mouseClickedWidth = event.getX();
        double progressbarWidth = progressbarDuration.getWidth();

        model.seekSong((mouseClickedWidth / progressbarWidth));
    }

    /**
     * Mouse event to handle the seek duration.
     *
     * @param event
     */
    @FXML
    private void handleSeekDurationPressed(MouseEvent event)
    {
        double mouseClickedWidth = event.getX();
        double progressbarWidth = progressbarDuration.getWidth();

        model.seekSong((mouseClickedWidth / progressbarWidth));
    }
}
