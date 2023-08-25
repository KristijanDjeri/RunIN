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

package com.runin.distance;

import com.runin.db.DistanceDAO;
import com.runin.model_managers.DistanceTableManager;
import com.runin.record.Distance;
import com.runin.record.Event;
import com.runin.shared.DialogController;
import com.runin.shared.FXMLPaths;
import com.runin.shared.Lang;
import com.runin.shared.TextResize;
import com.runin.stage.PopupStageViewController;
import com.runin.stage.StageViewController;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class DistanceViewController implements Initializable {

    @FXML
    private MFXTextField nameTextField;
    @FXML
    private TextField distanceTextField;
    @FXML
    private MFXButton actionButton,cancelButton;
    @FXML
    private Label addDistanceLabel;

    private final DistanceDAO distanceDAO;
    private final PopupStageViewController popupStageViewController;
    private final Distance distance;
    private final boolean addView;

    public DistanceViewController(Event event, Distance distance){
        addView = distance == null;
        this.distance = distance;
        distanceDAO = new DistanceDAO(event.id());
        popupStageViewController = new PopupStageViewController();
        popupStageViewController.loadScene(FXMLPaths.DISTANCE_EDIT.PATH,this);
        popupStageViewController.setTitle(Lang.get("distance"));
        popupStageViewController.show();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        actionButton.setText(addView?Lang.get("add"):Lang.get("update"));

        nameTextField.requestFocus();
        nameTextField.setOnAction(x->distanceTextField.requestFocus());
        distanceTextField.setOnKeyPressed(keyEvent -> {
            if(keyEvent.getCode()==KeyCode.ENTER){
                insert();
            }
        });

        distanceTextField.setTextFormatter(new TextFormatter<>(change->{
            String newText = change.getControlNewText();
            if(newText.matches("\\d*\\.?\\d{0,2}")){
                return change;
            }
            return null;
        }));

        if(!addView){
            nameTextField.setText(distance.name());
            distanceTextField.setText(String.valueOf(distance.distance_in_km()));
        }

        nameTextField.setFloatingText(Lang.get("name"));
        distanceTextField.setPromptText(Lang.get("distanceInKm"));
        cancelButton.setText(Lang.get("cancel"));
        if(addView) {
            addDistanceLabel.setText(Lang.get("addDistance"));
            actionButton.setText(Lang.get("add"));
        }else{
            addDistanceLabel.setText(Lang.get("editDistance"));
            actionButton.setText(Lang.get("edit"));
        }

        TextResize.resize(addDistanceLabel);
        TextResize.resize(actionButton);
        TextResize.resize(cancelButton);

    }

    @FXML
    private void insert(){
        if(addView){
            add();
        }else{
            update();
        }
    }

    private void update(){
        Distance distance = getDistance();
        if(distance!=null) {
            try{
                distanceDAO.openConnection();
                distanceDAO.update(distance);
                distanceDAO.closeConnection();
            }catch (SQLException e){
                throw new RuntimeException(e);
            }
            DistanceTableManager.getInstance().refresh();
            close();
        }
    }

    private void add(){
        Distance distance = getDistance();
        if(distance!=null) {
            try{
                distanceDAO.openConnection();
                distanceDAO.insert(distance);
                distanceDAO.closeConnection();
            }catch (SQLException e){
                throw new RuntimeException(e);
            }
            DistanceTableManager.getInstance().refresh();
            close();
        }
    }

    private Distance getDistance(){
        String name = nameTextField.getText();

        nameTextField.setStyle("");

        if(name.isEmpty()){
            nameTextField.setStyle("-fx-border-color:red;");
            return null;
        }

        boolean nameExists;
        try{
            distanceDAO.openConnection();
            nameExists = distanceDAO.checkIfNameExists(name);
            distanceDAO.closeConnection();
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
        if(nameExists&&addView){
            DialogController dialogController = new DialogController(StageViewController.getStage());
            dialogController.setContentText(Lang.get("dialogD1"));
            dialogController.showError(Lang.get("invalidInput"));
            return null;
        }

        distanceTextField.setStyle("");
        double inKm = Double.parseDouble(distanceTextField.getText());
        if(inKm<=0.0||inKm>=1000){
            distanceTextField.setStyle("-fx-border-color:red;");
            return null;
        }
        short distanceId = (addView)?0:distance.id();
        return new Distance(distanceId,name,inKm);
    }

    @FXML
    private void close(){
        popupStageViewController.close();
    }


}
