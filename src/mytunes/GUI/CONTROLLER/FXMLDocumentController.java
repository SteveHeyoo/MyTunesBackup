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
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javax.sound.sampled.UnsupportedAudioFileException;
import mytunes.BE.Playlist;
import mytunes.BE.Song;
import mytunes.GUI.MODEL.Model;

/**
 *
 * @author Bo
 */
public class FXMLDocumentController implements Initializable
{

    private Model model;

    @FXML
    private Label lblSong;
    @FXML
    private Label lblSongPlaylist;

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
    private Button btnPreviousSong;
    @FXML
    private Button btnPlaySong;
    @FXML
    private Button btnNextSong;

    @FXML
    private Slider volumeSlide;
    @FXML
    private Label lblVolume;

    private Song currentSong;
    private Control currentControlList;
    @FXML
    private RadioButton radioRepeat;
    @FXML
    private Label lblDuration;
    @FXML
    private JFXProgressBar progressbarDuration;

    public FXMLDocumentController()
    {
        model = Model.getInstance();

        //    progressbarDuration.
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

        if (event.getClickCount() == 2 && currentSong != null)
        {
            model.setCurrentListControl(currentControlList);
            model.playSong(currentSong);
            model.getmTPlayer().getMediaPlayer().setVolume(volumeSlide.getValue() / 100);
            bindPlayerToGUI();

        }
    }

    @FXML
    private void handleTblViewSongsDelete(ActionEvent event)
    {
        Song song = tblSong.getSelectionModel().getSelectedItem();
        try
        {
            model.deleteSong(song);
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
            model.deletPlaylist(playlist);

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
            loadSongDataView(song);
        }
        catch (IOException ex)
        {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void handleShowPlaylistSongs(MouseEvent event)
    {
        Playlist playlist = tblPlaylist.getSelectionModel().getSelectedItem();
        model.setCurrentPlaylist(playlist);
        int index = tblPlaylist.getSelectionModel().getSelectedIndex();
        int playlistId = playlist.getId();
        if (playlist != null)
        {



            try
            {
                model.showPlaylistSongs(playlistId);

            } catch (IOException ex)
            {
                showAlert("IOException", ex.getMessage());
            } catch (UnsupportedAudioFileException ex)
            {
                showAlert("UnsupportedAudioFileException", ex.getMessage());
            }


        }

        if (event.getClickCount() == 2)
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

            model.addSongToPlaylist(songToAdd, playlistToAddTo);
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
        //model.playSong(currentSong);
        model.setIndex(listPlaylistSong.getSelectionModel().getSelectedIndex());
        model.setCurrentListControl(currentControlList);
        model.playSongButtonClick();
        model.getmTPlayer().getMediaPlayer().setVolume(volumeSlide.getValue() / 100);

        //btnPlaySong.setText("Pause");
    }

    

    @FXML
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

    private void handleEditPlaylist(ActionEvent event) throws IOException
    {
        Playlist playlist = tblPlaylist.getSelectionModel().getSelectedItem();
        showNewEditPlaylistDialog(playlist);

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

        model.pressNextButton();
        model.getmTPlayer().getMediaPlayer().setVolume(volumeSlide.getValue() / 100);

    }

    @FXML
    private void handlePlayPreviousSong(ActionEvent event)
    {
        model.pressPreviousButton();
        model.getmTPlayer().getMediaPlayer().setVolume(volumeSlide.getValue() / 100);
    }

    @FXML
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
                System.out.println(model.getmTPlayer().getMediaPlayer().getTotalDuration().toMillis()/model.getmTPlayer().getMediaPlayer().getCurrentTime().toMillis());
                
                return (model.getmTPlayer().getMediaPlayer().getCurrentTime().toMillis()/model.getmTPlayer().getMediaPlayer().getTotalDuration().toMillis());
                

            }
        });
    }

    @FXML
    private void handleDragDropFiles(DragEvent event)
    {
        System.out.println("Drag&Dropped a item!");
    }

}
