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

package com.runin.participant;

import com.runin.db.DistanceDAO;
import com.runin.db.ParticipantDAO;
import com.runin.model_managers.ParticipantTableManager;
import com.runin.record.Distance;
import com.runin.record.Event;
import com.runin.record.Participant;
import com.runin.shared.FXMLPaths;
import com.runin.shared.Lang;
import com.runin.shared.TextResize;
import com.runin.stage.PopupStageViewController;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ParticipantsAddViewController implements Initializable {

    @FXML
    private MFXComboBox<String> distanceComboBox,genderComboBox;
    @FXML
    private MFXTextField firstNameTextField,lastNameTextField;
    @FXML
    private TextField ageTextField;
    @FXML
    private Label numberLabel,addParticipantLabel;
    @FXML
    private MFXButton cancelButton, addButton;
    private final PopupStageViewController popupStageViewController;
    private final ParticipantDAO participantDAO;
    private final DistanceDAO distanceDAO;

    private int participantId;
    public ParticipantsAddViewController(Event event){
        participantDAO = new ParticipantDAO(event.id());
        distanceDAO = new DistanceDAO(event.id());
        popupStageViewController = new PopupStageViewController();
        popupStageViewController.loadScene(FXMLPaths.PARTICIPANT_EDIT.PATH, this);
        popupStageViewController.show();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            participantDAO.openConnection();
            participantId = participantDAO.getNextIdValue();
            numberLabel.setText(String.format("%03d", participantId));
            participantDAO.closeConnection();
            genderComboBox.getItems().addAll(Lang.get("male"),Lang.get("female"));
            distanceDAO.openConnection();
            for (Distance d : distanceDAO.getAll()) {
                distanceComboBox.getItems().add(d.name());
            }
            distanceDAO.closeConnection();
            ageTextField.setTextFormatter(new TextFormatter<>(change->{
                String newText = change.getControlNewText();
                if(newText.isEmpty()){
                    return change;
                }
                int number = Integer.parseInt(newText);
                if(!newText.matches("\\d*")||number>120||number<1){
                    return null;
                }
                return change;
            }));
        }catch (SQLException e){
            throw new RuntimeException(e);
        }

        firstNameTextField.requestFocus();
        firstNameTextField.setOnAction(x->lastNameTextField.requestFocus());
        lastNameTextField.setOnAction(x->ageTextField.requestFocus());
        ageTextField.setOnAction(x-> genderComboBox.requestFocus());
        genderComboBox.setOnAction(x->distanceComboBox.requestFocus());

        loadLang();

    }

    @FXML
    private void add(){
        String firstName = firstNameTextField.getText();
        String lastName = lastNameTextField.getText();
        Distance distance;
        try {
            distanceDAO.openConnection();
            distance = distanceDAO.getDistanceViaName(distanceComboBox.getSelectionModel().getSelectedItem());
            distanceDAO.closeConnection();
        }catch (SQLException e){
            throw new RuntimeException(e);
        }

        clearBorder();

        if (firstName.isEmpty()) {
            firstNameTextField.setStyle("-fx-border-color:red;");
        } else if (lastName.isEmpty()) {
            lastNameTextField.setStyle("-fx-border-color:red;");
        } else if (distance == null) {
            distanceComboBox.setStyle("-fx-border-color:red;");
        }else if(ageTextField.getText().isBlank()){
            ageTextField.setStyle("-fx-border-color:red;");
        }else if(genderComboBox.getSelectedItem().isEmpty()){
            genderComboBox.setStyle("-fx-border-color:red;");
        }else {
            try {
                participantDAO.openConnection();
                Participant participant = new Participant(participantId,firstName,lastName, Integer.parseInt("0"+ageTextField.getText()), genderComboBox.getSelectedIndex() == 0, distance.id(), (short)0,0);
                participantDAO.insert(participant);
                participantDAO.closeConnection();
            }catch (SQLException e){
                throw new RuntimeException(e);
            }
            ParticipantTableManager.getInstance().refresh();
            close();
        }
    }

    private void clearBorder(){
        firstNameTextField.setStyle("");
        lastNameTextField.setStyle("");
        ageTextField.setStyle("");
        genderComboBox.setStyle("");
        distanceComboBox.setStyle("");
    }

    @FXML
    private void close(){
        popupStageViewController.close();
    }

    private void loadLang(){
        addParticipantLabel.setText(Lang.get("addParticipant"));
        firstNameTextField.setFloatingText(Lang.get("firstName"));
        lastNameTextField.setFloatingText(Lang.get("lastName"));
        ageTextField.setPromptText(Lang.get("age")+"[1-120]");
        genderComboBox.setFloatingText(Lang.get("gender"));
        distanceComboBox.setFloatingText(Lang.get("distance"));
        cancelButton.setText(Lang.get("cancel"));
        addButton.setText(Lang.get("add"));

        TextResize.resize(addParticipantLabel);
        TextResize.resize(addButton);
        TextResize.resize(cancelButton);

    }

}
