/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mytunes.GUI.CONTROLLER;

import java.io.File;

import java.io.IOException;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import javafx.stage.FileChooser;
import javax.sound.sampled.UnsupportedAudioFileException;

import javafx.stage.Stage;

import mytunes.BE.Song;
import static mytunes.GUI.CONTROLLER.FXMLDocumentController.showAlert;
import mytunes.GUI.MODEL.Model;

/**
 *
 * @author Bo
 */
public class SongEditController implements Initializable
{

    @FXML
    private TextField txtTitle;
    @FXML
    private TextField txtArtist;
    @FXML
    private ComboBox cbCategory;
    @FXML
    private TextField txtTime;
    @FXML
    private TextField txtFile;

    private Song currentSong;

    private File file;

    private Model model;

    public void initialize(URL url, ResourceBundle rb)
    {
        model = Model.getInstance();
    }

    public void setSong(Song song)
    {
        currentSong = song;
        fillTextField();
    }

    private void fillTextField()
    {
        txtTitle.setText(currentSong.getTitle());
        txtArtist.setText(currentSong.getArtist());
        txtTime.setText(currentSong.getDurationInMinutes());
        txtFile.setText(currentSong.getFilePath());

    }

    @FXML
    private void handleSaveSong(ActionEvent event)
    {

        currentSong.setArtist(txtArtist.getText());
        currentSong.setTitle(txtTitle.getText());
        try
        {
            model.editSong(currentSong, file);
        } catch (IOException ex)
        {
            showAlert("IOException", ex.getMessage());
        } catch (UnsupportedAudioFileException ex)
        {
            showAlert("IOException", ex.getMessage());
        }
        Stage getStage = (Stage) txtFile.getScene().getWindow();
        getStage.close();

    }

    @FXML
    private void handleBtnCancel(ActionEvent event)
    {
        Stage getStage = (Stage) txtFile.getScene().getWindow();
        getStage.close();
    }

    @FXML
    private void handleBtnChoose(ActionEvent event)
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName(currentSong.getFilePath());
        FileChooser.ExtensionFilter mp3Filter = new FileChooser.ExtensionFilter("MP3 Files(*.mp3)", "*.mp3");
        fileChooser.getExtensionFilters().add(mp3Filter);

        file = fileChooser.showOpenDialog(null);

    }

}
