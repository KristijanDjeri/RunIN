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

package com.runin.models;

import com.runin.db.EventDAO;
import com.runin.model_managers.EventTableManager;
import com.runin.record.Event;
import com.runin.shared.DialogController;
import com.runin.shared.ImageManager;
import com.runin.shared.Lang;
import com.runin.shared.Section;
import com.runin.stage.StageViewController;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.beans.property.*;
import java.sql.SQLException;
import java.util.Objects;

@SuppressWarnings("unused")
public class EventTable {

    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty location = new SimpleStringProperty();
    private final StringProperty date = new SimpleStringProperty();
    private final ObjectProperty<MFXButton> removeButton = new SimpleObjectProperty<>();

    private final Event event;

    public EventTable(Event event,Section section) {
        this.event = event;
        setId(event.id());
        setName(event.name());
        setLocation(event.location());
        setDate(event.date().toString());

        MFXButton button = new MFXButton("",ImageManager.getImageView(ImageManager.getIcon("delete"),30));
        button.setStyle("-fx-background-color:transparent;");
        button.setOnAction(e->{
            DialogController dialogController = new DialogController(StageViewController.getStage());
            dialogController.setContentText(Lang.get("dialogET1")+" ["+getName()+"]");

            dialogController.getSecondaryButton().setText(Lang.get("cancel"));
            dialogController.getPrimaryButton().setText(Lang.get("continue"));

            dialogController.showWarning(Lang.get("warning"));

            dialogController.addAction(dialogController.getSecondaryButton(),evt->dialogController.close());
            dialogController.addAction(dialogController.getPrimaryButton(),evt->{
                try{
                    EventDAO eventDAO = new EventDAO();
                    eventDAO.openConnection();
                    eventDAO.delete(getId());
                    eventDAO.closeConnection();
                }catch(SQLException e1){
                    throw new RuntimeException(e1);
                }
                Objects.requireNonNull(EventTableManager.getInstance(section)).refresh();
                dialogController.close();
            });
        });
        button.buttonTypeProperty().set(io.github.palexdev.materialfx.enums.ButtonType.RAISED);
        setRemoveButton(button);
        getRemoveButton().setVisible(false);

    }

    public Event getEvent(){
        return event;
    }

    public Integer getId() {
        return id.get();
    }

    public void setId(Integer id) {
        this.id.set(id);
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getLocation() {
        return location.get();
    }

    public void setLocation(String location) {
        this.location.set(location);
    }

    public String getDate() {
        return date.get();
    }

    public void setDate(String date) {
        this.date.set(date);
    }

    public void setRemoveButton(MFXButton removeButton){
        this.removeButton.set(removeButton);
    }

    public MFXButton getRemoveButton(){
        return removeButton.get();
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public StringProperty locationProperty() {
        return location;
    }

    public StringProperty dateProperty() {
        return date;
    }

    public ObjectProperty<MFXButton> removeButtonProperty() {
        return removeButton;
    }
}
