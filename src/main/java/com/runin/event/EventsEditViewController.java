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

import com.runin.category.CategoryViewController;
import com.runin.db.EventDAO;
import com.runin.db.ParticipantDAO;
import com.runin.distance.DistanceViewController;
import com.runin.model_managers.*;
import com.runin.models.CategoryTable;
import com.runin.models.DistanceTable;
import com.runin.record.Event;
import com.runin.shared.*;
import com.runin.stage.StageViewController;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXDatePicker;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.controls.legacy.MFXLegacyTableView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

public class EventsEditViewController implements Initializable {

    @FXML
    private MFXTextField nameTextField, locationTextField;
    @FXML
    private MFXDatePicker datePicker;
    @FXML
    private MFXLegacyTableView<DistanceTable> distanceTableView;
    @FXML
    private MFXLegacyTableView<CategoryTable> categoryTableView;
    @FXML
    private MFXButton categoryButton, distanceEditButton, distanceAddButton, saveButton;

    private Event event;

    private final CategoryTableManager categoryTableManager;
    private final DistanceTableManager distanceTableManager;

    private static EventsEditViewController INSTANCE;

    public static EventsEditViewController getInstance(){
        return INSTANCE;
    }

    public EventsEditViewController(Event event){
        INSTANCE = this;
        this.event = event;
        categoryTableManager = new CategoryTableManager(event.id());
        distanceTableManager = new DistanceTableManager(event.id());

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        nameTextField.setText(event.name());
        locationTextField.setText(event.location());
        datePicker.setValue(event.date().toLocalDate());

        categoryTableManager.loadTableView(categoryTableView);
        categoryTableView.getColumns().remove(categoryTableView.getColumns().size()-1);
        categoryTableManager.refresh();

        distanceTableManager.loadTableView(distanceTableView);
        ParticipantDAO participantDAO = new ParticipantDAO(event.id());
        try {
            participantDAO.openConnection();
            if(!participantDAO.getAll().isEmpty()){
                distanceTableView.getColumns().remove(distanceTableView.getColumns().size()-1);
            }
            participantDAO.closeConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        distanceTableManager.refresh();

        categoryAction();

        distanceEditButton.setGraphic(ImageManager.getImageView(ImageManager.getIcon("edit"),30));
        distanceAddButton.setGraphic(ImageManager.getImageView(ImageManager.getIcon("emergency"),30));

        distanceEditButton.setText(Lang.get("edit"));
        distanceAddButton.setText(Lang.get("add"));

        nameTextField.setFloatingText(Lang.get("name"));
        locationTextField.setFloatingText(Lang.get("location"));
        datePicker.setFloatingText(Lang.get("date"));
        saveButton.setText(Lang.get("save"));

        TextResize.resize(distanceEditButton);
        TextResize.resize(distanceAddButton);

    }

    public void categoryAction(){
        if(categoryTableView.getItems().isEmpty()){
            categoryButton.setText(Lang.get("add"));
            categoryButton.setGraphic(ImageManager.getImageView(ImageManager.getIcon("emergency"),30));
            categoryButton.setId("primary");
            categoryButton.setOnAction(x->addCategory());
        }else{
            categoryButton.setText(Lang.get("edit"));
            categoryButton.setGraphic(ImageManager.getImageView(ImageManager.getIcon("edit"),30));
            categoryButton.setId("secondary");
            categoryButton.setOnAction(x->editCategory());
        }
    }

    @FXML
    private void addDistance(){
        new DistanceViewController(event,null);
    }

    @FXML
    public void editDistance(){
        if(!distanceTableView.getSelectionModel().isEmpty()){
            new DistanceViewController(event,distanceTableView.getSelectionModel().getSelectedItem().getDistance());
        }
    }

    @FXML
    private void addCategory(){
        new CategoryViewController(event,false,this);
    }

    @FXML
    public void editCategory(){
        new CategoryViewController(event,true,this);
    }

    @FXML
    private void saveChanges(){
        Event event1 = save();
        if(event1==null){
            return;
        }
        Objects.requireNonNull(EventTableManager.getInstance(Section.EVENT)).refresh();
        event = event1;
        DialogController dialogController = new DialogController(StageViewController.getStage());
        dialogController.getPrimaryButton().setText(Lang.get("ok"));
        dialogController.addAction(dialogController.getPrimaryButton(),e->dialogController.close());
        dialogController.showGeneric(Lang.get("dialogEE2"));
    }

    private Event save(){
        String name = nameTextField.getText();
        String location = locationTextField.getText();
        Date date = Date.valueOf(datePicker.getValue());

        nameTextField.setStyle("");
        locationTextField.setStyle("");
        datePicker.setStyle("");

        if(name.isEmpty()){
            nameTextField.setStyle("-fx-border-color:red;");
            return null;
        }else if(location.isEmpty()){
            locationTextField.setStyle("-fx-border-color:red;");
            return null;
        }else if(date.toLocalDate()==null){
            datePicker.setStyle("-fx-border-color:red;");
            return null;
        }
        if(name.equals(event.name()) && location.equals(event.location()) && date.equals(event.date())){
            return null;
        }

        AtomicBoolean editable_results= new AtomicBoolean(event.editable_results());

        if(editable_results.get()&&!event.finished()&&(date.toLocalDate().isEqual(LocalDate.now())||date.toLocalDate().isAfter(LocalDate.now()))){
            DialogController dialogController = new DialogController(StageViewController.getStage());
            dialogController.setContentText(Lang.get("dialogEE1"));
            dialogController.getSecondaryButton().setText(Lang.get("no"));
            dialogController.getPrimaryButton().setText(Lang.get("yes"));
            dialogController.addAction(dialogController.getSecondaryButton(), e->dialogController.close());
            dialogController.addAction(dialogController.getPrimaryButton(), e->{
                editable_results.set(false);
                dialogController.close();
            });
            dialogController.showAndWaitWarning(Lang.get("warning"));
        }

        Event event1 = new Event(event.id(), name, location, date, event.finished(), editable_results.get());
        EventDAO eventDAO = new EventDAO();
        try{
            eventDAO.openConnection();
            eventDAO.update(event1);
            eventDAO.closeConnection();
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
        return event1;
    }

    @FXML
    private void back(){
        save();
        StageViewController.getInstance().setScene(FXMLPaths.EVENT_DETAILED.PATH, new EventsDetailedViewController(event,Section.EVENT), Section.EVENT, Save.NO);
    }

}
