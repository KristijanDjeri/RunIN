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

package com.runin.event;

import com.runin.db.EventDAO;
import com.runin.model_managers.EventTableManager;
import com.runin.models.EventTable;
import com.runin.record.Event;
import com.runin.shared.*;
import com.runin.stage.PopupStageViewController;
import com.runin.stage.StageViewController;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXDatePicker;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;
import java.util.ResourceBundle;

public class EventsAddViewController implements Initializable {

    @FXML
    private MFXTextField nameTextField,locationTextField;
    @FXML
    private MFXDatePicker datePicker;
    @FXML
    private Label addEventLabel;
    @FXML
    private MFXButton cancelButton, addButton;

    private PopupStageViewController popupStageViewController;
    private final EventDAO eventDAO;

    public EventsAddViewController(){
        eventDAO = new EventDAO();
        loadScene();
    }

    private void loadScene(){
        popupStageViewController = new PopupStageViewController();
        popupStageViewController.loadScene(FXMLPaths.EVENT_ADD.PATH, this);
        popupStageViewController.show();
    }

    public void add(){
        String name = nameTextField.getText();
        String location = locationTextField.getText();
        Date date = Date.valueOf(datePicker.getValue());

        boolean eventExists;
        try{
            eventDAO.openConnection();
            eventExists = eventDAO.checkIfEventExists(name,location,date);
            eventDAO.closeConnection();
        }catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if(eventExists){
            DialogController dialogController = new DialogController(StageViewController.getStage());
            dialogController.setContentText(Lang.get("dialogEA2"));
            dialogController.getPrimaryButton().setText(Lang.get("ok"));
            dialogController.showWarning(Lang.get("warning"));
            dialogController.addAction(dialogController.getPrimaryButton(), e->dialogController.close());
            return;
        }

        if(date.toLocalDate()==null||name.isEmpty()||location.isEmpty()){
            DialogController dialogController = new DialogController(StageViewController.getStage());
            dialogController.setContentText("");
            dialogController.showWarning(Lang.get("invalidInput"));
        }else if(date.toLocalDate().isBefore(LocalDate.now())){
            DialogController dialogController = new DialogController(StageViewController.getStage());
            dialogController.setContentText(Lang.get("dialogEA1"));
            dialogController.showWarning(Lang.get("warning"));
            dialogController.getSecondaryButton().setText(Lang.get("no"));
            dialogController.getPrimaryButton().setText(Lang.get("yes"));

            dialogController.addAction(dialogController.getSecondaryButton(),e->dialogController.close());
            dialogController.addAction(dialogController.getPrimaryButton(),e->{
                addEvent(true);
                dialogController.close();
            });
        }else{
            addEvent(false);
        }
    }

    private void addEvent(boolean editableResults){
        Event event = new Event(0, nameTextField.getText(), locationTextField.getText(), Date.valueOf(datePicker.getValue()), false, editableResults);
        Event event1;
        try{
            eventDAO.openConnection();
            eventDAO.insert(event);
            event1 = eventDAO.get(eventDAO.getIDFromData(event.name(), event.location(), event.date()));
            eventDAO.closeConnection();
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
        Objects.requireNonNull(EventTableManager.getInstance(Section.EVENT)).add(new EventTable(event1,Section.EVENT));
        StageViewController.getInstance().setScene(FXMLPaths.EVENT_EDIT.PATH,new EventsEditViewController(event1), Section.EVENT, Save.NO);
        cancel();
    }

    public void cancel(){
        popupStageViewController.close();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        nameTextField.requestFocus();
        nameTextField.setOnAction(x->locationTextField.requestFocus());
        locationTextField.setOnAction(x->datePicker.requestFocus());

        nameTextField.setFloatingText(Lang.get("name"));
        locationTextField.setFloatingText(Lang.get("location"));
        datePicker.setFloatingText(Lang.get("date"));
        addEventLabel.setText(Lang.get("addEvent"));
        cancelButton.setText(Lang.get("cancel"));
        addButton.setText(Lang.get("add"));

        TextResize.resize(addEventLabel);
        TextResize.resize(addButton);
        TextResize.resize(cancelButton);

    }
}
