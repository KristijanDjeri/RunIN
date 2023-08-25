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

import com.runin.db.CategoryDAO;
import com.runin.db.ParticipantDAO;
import com.runin.db.ResultDAO;
import com.runin.event.EventsDetailedViewController;
import com.runin.record.Category;
import com.runin.record.Event;
import com.runin.record.Participant;
import com.runin.record.RaceConfig;
import com.runin.shared.FXMLPaths;
import com.runin.shared.Lang;
import com.runin.shared.Save;
import com.runin.shared.Section;
import com.runin.stage.StageViewController;
import io.github.palexdev.materialfx.controls.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class RaceSetupViewController implements Initializable {

    @FXML
    private Label eventNameLabel, eventLocationLabel, eventDateLabel, raceConfigLabel, nameLabel, locationLabel, dateLabel, durationLabel;
    @FXML
    private MFXCheckbox infiniteDurationCheckBox;
    @FXML
    private TextField hoursTextField,minutesTextField,secondsTextField;
    @FXML
    private AnchorPane contentPane;
    @FXML
    private HBox progressHBox;
    @FXML
    private MFXProgressBar progressBar;
    @FXML
    private MFXButton backButton, startEventButton;

    private final Event event;


    public RaceSetupViewController(Event event) {
        this.event = event;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        eventNameLabel.setText(event.name());
        eventLocationLabel.setText(event.location());
        eventDateLabel.setText(event.date().toString());

        infiniteDurationCheckBox.setOnAction(x->{
            boolean disable = infiniteDurationCheckBox.isSelected();
            hoursTextField.setDisable(disable);
            minutesTextField.setDisable(disable);
            secondsTextField.setDisable(disable);
        });

        boolean disable = infiniteDurationCheckBox.isSelected();
        hoursTextField.setDisable(disable);
        minutesTextField.setDisable(disable);
        secondsTextField.setDisable(disable);

        hoursTextField.setTextFormatter(new TextFormatter<>(change->{
            String newText = change.getControlNewText();
            if(newText.isEmpty()){
                return change;
            }
            if(!newText.matches("\\d*")){
                return null;
            }else{
                int number = Integer.parseInt(newText);
                if(number>99||number<0){
                    hoursTextField.setStyle("-fx-border-color:red;");
                }else{
                    hoursTextField.setStyle("");
                }
            }
            return change;
        }));
        minutesTextField.setTextFormatter(new TextFormatter<>(change->{
            String newText = change.getControlNewText();
            if(newText.isEmpty()){
                return change;
            }
            if(!newText.matches("\\d*")){
                return null;
            }else{
                int number = Integer.parseInt(newText);
                if(number>59||number<0){
                    minutesTextField.setStyle("-fx-border-color:red;");
                }else{
                    minutesTextField.setStyle("");
                }
            }
            return change;
        }));
        secondsTextField.setTextFormatter(new TextFormatter<>(change->{
            String newText = change.getControlNewText();
            if(newText.isEmpty()){
                return change;
            }
            if(!newText.matches("\\d*")){
                return null;
            }else{
                int number = Integer.parseInt(newText);
                if(number>59||number<0){
                    secondsTextField.setStyle("-fx-border-color:red;");
                }else{
                    secondsTextField.setStyle("");
                }
            }
            return change;
        }));

        loadLang();

    }

    @FXML
    private void finishSetup(){
        int HH = Integer.parseInt("0"+hoursTextField.getText());
        int mm = Integer.parseInt("0"+minutesTextField.getText());
        int ss = Integer.parseInt("0"+secondsTextField.getText());
        LocalTime duration = null;
        if(!infiniteDurationCheckBox.isSelected()&&(HH>99||mm>59||ss>59||HH<0||mm<0||ss<0||(HH==0&&mm==0&&ss==0))){
            return;
        }else if(!infiniteDurationCheckBox.isSelected()){
            duration = LocalTime.of(HH, mm, ss);
        }
        RaceConfig raceConfig = new RaceConfig(duration);
        updateParticipantCategories();
        clearResults();
        StageViewController.getInstance().setScene(FXMLPaths.RACE_MAIN.PATH, new RaceViewController(event,raceConfig), Section.EVENT, Save.NO);
    }

    @FXML
    private void back(){
        StageViewController.getInstance().setScene(FXMLPaths.EVENT_DETAILED.PATH, new EventsDetailedViewController(event,Section.EVENT), Section.EVENT, Save.NO);
    }

    private void clearResults(){
        ResultDAO resultDAO = new ResultDAO(event.id());
        try{
            resultDAO.openConnection();
            resultDAO.clear();
            resultDAO.closeConnection();
            resultDAO.checkTableValidity();
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    private void updateParticipantCategories(){
        contentPane.setEffect(new GaussianBlur(10.0));
        progressHBox.setVisible(true);
        Thread thread = new Thread(() -> {
            CategoryDAO categoryDAO = new CategoryDAO(event.id());
            List<short[]> categories = new ArrayList<>();
            try {
                categoryDAO.openConnection();
                for(Category category:categoryDAO.getAll()){
                    short[] ages = new short[3];
                    ages[0]=category.id();
                    ages[1]=category.aboveAge();
                    ages[2]=category.belowAge();
                    categories.add(ages);
                }
                categoryDAO.closeConnection();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            ParticipantDAO participantDAO = new ParticipantDAO(event.id());
            try {
                participantDAO.openConnection();
                List<Participant> participantSet = participantDAO.getAll();
                double progressIncrement = 1.0 / participantSet.size();
                progressBar.setProgress(0);
                for(Participant participant:participantSet){
                    Participant participant1 = getParticipant(participant, categories);
                    participantDAO.update(participant1);
                    Platform.runLater(()->
                            progressBar.setProgress(progressBar.getProgress()+progressIncrement)
                    );
                }
                participantDAO.closeConnection();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();
    }

    private Participant getParticipant(Participant participant, List<short[]> categories) {
        short categoryID=0;
        for (short[] ages : categories) {
            if (participant.age() >= ages[1] && participant.age() < ages[2]) {
                categoryID = ages[0];
                break;
            }
        }
        return new Participant(participant.id(), participant.first_name(), participant.last_name(),
                participant.age(), participant.gender(), participant.distance_id(), categoryID, participant.runner_id());
    }

    private void loadLang(){
        raceConfigLabel.setText(Lang.get("raceConfiguration"));
        nameLabel.setText(Lang.get("name"));
        locationLabel.setText(Lang.get("location"));
        dateLabel.setText(Lang.get("date"));
        durationLabel.setText(Lang.get("duration"));
        infiniteDurationCheckBox.setText(Lang.get("infinite"));
        backButton.setText(Lang.get("back"));
        startEventButton.setText(Lang.get("startEvent"));
    }

}
