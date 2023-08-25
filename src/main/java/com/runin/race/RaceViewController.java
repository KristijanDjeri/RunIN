 /*
  * Copyright (C) 2023 Kristijan Đeri
  *
  * RunIN is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * RunIN is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with RunIN.  If not, see <https://www.gnu.org/licenses/>.
  */

package com.runin.race;

import com.runin.db.*;
import com.runin.event.EventsDetailedViewController;
import com.runin.event.EventsMainViewController;
import com.runin.model_managers.EventTableManager;
import com.runin.model_managers.ResultTableManager;
import com.runin.models.EventTable;
import com.runin.models.ResultTable;
import com.runin.record.*;
import com.runin.shared.*;
import com.runin.stage.StageViewController;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.legacy.MFXLegacyListView;
import io.github.palexdev.materialfx.controls.legacy.MFXLegacyTableRow;
import io.github.palexdev.materialfx.controls.legacy.MFXLegacyTableView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class RaceViewController implements Initializable {

    @FXML
    private MFXLegacyListView<String> categoryListView,distanceListView,genderListView;
    @FXML
    private MFXLegacyTableView<ResultTable> tableView;
    @FXML
    private GridPane gridPane;
    @FXML
    private AnchorPane startPane;
    @FXML
    private Label stopwatchLabel;
    @FXML
    private TextField numberTextField;
    @FXML
    private MFXButton enterButton, removeButton,stopwatchButton,startButton;
    @FXML
    private VBox rightVbox;

    private final ParticipantDAO participantDAO;
    private final ResultDAO resultDAO;
    private final Event event;
    private final ResultTableManager resultTableManager;
    private Stopwatch stopwatch;

    private short selectedDistance, selectedCategory, selectedGender;
    private final RaceConfig raceConfig;

    public RaceViewController(Event event, RaceConfig raceConfig){
        this.event = event;
        this.raceConfig = raceConfig;
        resultTableManager = new ResultTableManager(event);
        participantDAO = new ParticipantDAO(event.id());
        resultDAO = new ResultDAO(event.id());
        openConnection();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        StageViewController.getInstance().hideMenu(true);
        StageViewController.getInstance().setFullScreenStage(true);

        numberTextField.setTextFormatter(new TextFormatter<>(change->{
            String newText = change.getControlNewText();
            if(newText.isEmpty()){
                return change;
            }
            if(!newText.matches("\\d*")){
                return null;
            }
            return change;
        }));

        numberTextField.setOnKeyPressed(keyEvent->{
            if(keyEvent.getCode() == KeyCode.DELETE){
                try {
                    remove();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        tableView.setRowFactory(x->{
            MFXLegacyTableRow<ResultTable> tableRow = new MFXLegacyTableRow<>();
            tableRow.setOnMouseClicked(mouseEvent -> {
                if(!tableRow.isEmpty()&&mouseEvent.getButton() == MouseButton.PRIMARY&&mouseEvent.getClickCount()==1){
                    numberTextField.setText(String.valueOf(tableRow.getItem().getParticipantId()));
                    removeButton.setVisible(true);
                }
            });
            tableRow.focusedProperty().addListener((observable,oldValue,newValue)->{
                if(!newValue){
                    removeButton.setVisible(false);
                }
            });
            return tableRow;
        });

        resultTableManager.loadTableView(tableView);
        resultTableManager.refresh();

        stopwatch = new Stopwatch(raceConfig.duration(),stopwatchLabel,this);

        populateCategoryListView();
        populateDistanceListView();
        populateGenderListView();

        startPane.setVisible(true);
        gridPane.setEffect(new GaussianBlur(10.0));

        startButton.setGraphic(ImageManager.getImageView(ImageManager.getIcon("play"),50));
        enterButton.setGraphic(ImageManager.getImageView(ImageManager.getIcon("user-check"),25));
        removeButton.setGraphic(ImageManager.getImageView(ImageManager.getIcon("user-error"),25));
        stopwatchButton.setGraphic(ImageManager.getImageView(ImageManager.getIcon("stop"),20));

        loadLang();

    }

    private void populateGenderListView(){
        genderListView.getItems().addAll(Lang.get("both"),Lang.get("male"),Lang.get("female"));
        genderListView.setOnMouseClicked(mouseEvent -> {
            if(genderListView.getSelectionModel().getSelectedItem()!=null){
                selectedGender = (short)genderListView.getSelectionModel().getSelectedIndex();
                refreshTable();
            }
        });
    }

    private void populateDistanceListView(){
        distanceListView.setOnMouseClicked(mouseEvent -> {
            if(distanceListView.getSelectionModel().getSelectedItem()!=null){
                selectedDistance = (short)distanceListView.getSelectionModel().getSelectedIndex();
                refreshTable();
            }
        });

        DistanceDAO distanceDAO = new DistanceDAO(event.id());
        try{
            distanceDAO.openConnection();
            distanceListView.getItems().clear();
            distanceListView.getItems().add(Lang.get("all"));
            for(Distance distance:distanceDAO.getAll()){
                distanceListView.getItems().add(distance.name());
            }
            distanceDAO.closeConnection();
        }catch(SQLException e){
            throw new RuntimeException(e);
        }

    }

    private void populateCategoryListView(){

        categoryListView.setOnMouseClicked(mouseEvent -> {
            if(categoryListView.getSelectionModel().getSelectedItem()!=null){
                selectedCategory = (short)categoryListView.getSelectionModel().getSelectedIndex();
                refreshTable();
            }
        });

        CategoryDAO categoryDAO = new CategoryDAO(event.id());
        try {
            categoryDAO.openConnection();
            categoryListView.getItems().clear();
            categoryListView.getItems().add(Lang.get("all"));
            for(Category category:categoryDAO.getAll()){
                categoryListView.getItems().add(category.name());
            }
            categoryDAO.closeConnection();
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void start(){
        gridPane.setEffect(null);
        startPane.setVisible(false);
        openConnection();
        stopwatch.start();
    }

    @FXML
    private void enter() throws SQLException {
        if(stopwatch.isRunning()) {
            int number = Integer.parseInt(numberTextField.getText());
            if(participantDAO.participantExists(number)&&!resultDAO.participantExists(number)){
                Participant participant = participantDAO.get(number);
                Result result = new Result(0,participant.id(),participant.gender(),participant.distance_id(),participant.category_id(), stopwatch.time);
                resultDAO.insert(result);
                ResultTableManager.getInstance().add(new ResultTable(event,result));
                numberTextField.clear();
                numberTextField.setStyle("");
            }else{
                numberTextField.setStyle("-fx-border-color:red;");
            }
        }
    }

    @FXML
    private void stop(){
        stopPopup();
    }

    @FXML
    private void remove() throws SQLException {
        if(stopwatch.isRunning()) {
            int number = Integer.parseInt(numberTextField.getText());
            if(resultDAO.participantExists(number)){
                removeDialogPane(number);
            }
        }
    }

    private void refreshTable(){
        resultTableManager.search(selectedCategory,selectedDistance,selectedGender,"");
    }

    private void openConnection(){
        try {
            resultDAO.openConnection();
            participantDAO.openConnection();
        }catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void closeConnection(){
        new Thread(() -> {
            try {
                insertDNF();
                EventDAO eventDAO = new EventDAO();
                eventDAO.openConnection();
                eventDAO.update(new Event(event.id(),event.name(), event.location(), event.date(),true,false));
                eventDAO.closeConnection();
                resultDAO.closeConnection();
                participantDAO.closeConnection();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    private void insertDNF() throws SQLException {
        for(Participant participant:participantDAO.getAll()){
            if(!resultDAO.participantExists(participant.id())){
                Result result = new Result(0, participant.id(), participant.gender(), participant.distance_id(), participant.category_id(), "00:00:00");
                resultDAO.insert(result);
                resultTableManager.add(new ResultTable(event, result));
            }
        }
    }

    private void removeDialogPane(int number) throws SQLException {
        DialogController dialogController = new DialogController(StageViewController.getStage());
        Participant participant = participantDAO.get(number);
        dialogController.setContentText("\n"+Lang.get("dialogRW2")+" ["+participant.first_name()+" "+participant.last_name()+" ("+number+")]");

        dialogController.getSecondaryButton().setText(Lang.get("no"));
        dialogController.getPrimaryButton().setText(Lang.get("yes"));

        dialogController.showWarning(Lang.get("warning"));

        dialogController.addAction(dialogController.getSecondaryButton(),evt->dialogController.close());
        dialogController.addAction(dialogController.getPrimaryButton(),evt->{
            try{
                resultDAO.delete(resultDAO.getIDFromParticipant(number));
                numberTextField.clear();
                resultTableManager.refresh();
                refreshTable();
                dialogController.close();
            }catch(SQLException e){
                throw new RuntimeException(e);
            }
        });
    }

    private void stopPopup(){
        DialogController dialogController = new DialogController(StageViewController.getStage());
        dialogController.setContentText("\n"+Lang.get("dialogRW3"));

        dialogController.getSecondaryButton().setText(Lang.get("no"));
        dialogController.getPrimaryButton().setText(Lang.get("yes"));

        dialogController.showWarning(Lang.get("warning"));

        dialogController.addAction(dialogController.getSecondaryButton(),evt->dialogController.close());
        dialogController.addAction(dialogController.getPrimaryButton(),evt->{
            endEvent();
            dialogController.close();
        });
    }

    public void endEvent(){
        stopwatch.stop();
        closeConnection();
        gridPane.getColumnConstraints().remove(2);
        gridPane.getChildren().remove(rightVbox);
        stopwatchButton.setText(Lang.get("close"));
        stopwatchButton.setId("primary");
        stopwatchButton.setGraphic(ImageManager.getImageView(ImageManager.getIcon("close-square"),20));
        stopwatchButton.setOnAction(x->close());
    }

    private void close(){
        StageViewController.getInstance().hideMenu(false);
        StageViewController.getInstance().setFullScreenStage(false);
        Event event1 = new Event(event.id(), event.name(), event.location(), event.date(), true,false);
        new EventTableManager(Section.HISTORY).add(new EventTable(event1,Section.HISTORY));
        StageViewController.getInstance().setScene(FXMLPaths.TABLE_VIEW.PATH, new EventsMainViewController(),Section.EVENT, Save.NO);
        StageViewController.getInstance().setScene(FXMLPaths.EVENT_DETAILED.PATH, new EventsDetailedViewController(event1,Section.HISTORY), Section.HISTORY, Save.NO);
    }

    private void loadLang(){
        stopwatchButton.setText(Lang.get("stop"));
        numberTextField.setPromptText(Lang.get("number"));
        enterButton.setText(Lang.get("enter"));
        removeButton.setText(Lang.get("remove"));
        startButton.setText(Lang.get("start"));
    }

}
