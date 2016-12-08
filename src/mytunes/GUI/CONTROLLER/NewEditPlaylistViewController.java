/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mytunes.GUI.CONTROLLER;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.Alert;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javax.sound.sampled.UnsupportedAudioFileException;
import mytunes.BE.Playlist;
import static mytunes.GUI.CONTROLLER.FXMLDocumentController.showAlert;
import mytunes.GUI.MODEL.Model;

/**
 * FXML Controller class
 *
 * @author Stefan-VpcEB3J1E
 */
public class NewEditPlaylistViewController implements Initializable
{

    @FXML
    private TextField lblNameNewEditPlaylist;
    @FXML
    private Button btnCancelPlaylistEdit;

    private Model model;
    private Playlist playlistToEdit;

    public NewEditPlaylistViewController()
    {
        model = Model.getInstance();

    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        // TODO
    }

    @FXML
    private void handleSaveNewPlaylist(ActionEvent event)
    {
        String playlistName = lblNameNewEditPlaylist.getText().trim();
        try
        {
            model.createNewPlaylist(playlistToEdit, playlistName);
        } catch (IOException ex)
        {
            showAlert("IOException", ex.getMessage());
        } catch (UnsupportedAudioFileException ex)
        {
            showAlert("UnsupportedAudioFileException", ex.getMessage());
        }

        Stage getStage = (Stage) lblNameNewEditPlaylist.getScene().getWindow();
        getStage.close();

    }

    @FXML
    private void handleCancelNewPlaylist(ActionEvent event)
    {

        Stage getStage = (Stage) lblNameNewEditPlaylist.getScene().getWindow();
        getStage.close();

    }

    public void setPlaylistToEdit(Playlist playlist)
    {
        playlistToEdit = playlist;
        lblNameNewEditPlaylist.setText(playlist.getName());
    }
}
