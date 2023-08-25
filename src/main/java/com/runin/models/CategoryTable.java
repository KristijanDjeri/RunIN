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

import com.runin.db.CategoryDAO;
import com.runin.model_managers.CategoryTableManager;
import com.runin.record.Category;
import com.runin.shared.DialogController;
import com.runin.shared.ImageManager;
import com.runin.shared.Lang;
import com.runin.stage.StageViewController;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.beans.property.*;
import java.sql.SQLException;

@SuppressWarnings("unused")
public class CategoryTable {

    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final IntegerProperty aboveAge = new SimpleIntegerProperty();
    private final IntegerProperty belowAge = new SimpleIntegerProperty();
    private final ObjectProperty<MFXButton> removeButton = new SimpleObjectProperty<>();
    private final int eventId;

    private final Category category;

    public CategoryTable(int eventId,Category category){
        this.eventId = eventId;
        this.category = category;
        setId(category.id());
        setName(category.name());
        setAboveAge(category.aboveAge());
        setBelowAge(category.belowAge());
        setButton();
    }

    public Category getCategory(){
        return category;
    }

    private void setButton(){
        MFXButton button = new MFXButton("",ImageManager.getImageView(ImageManager.getIcon("delete"),30));
        button.setStyle("-fx-background-color:transparent;");
        button.setOnAction(e->{
            DialogController dialogController = new DialogController(StageViewController.getStage());
            dialogController.setContentText(Lang.get("dialogCT1")+" ["+getName()+"]");
            dialogController.getSecondaryButton().setText(Lang.get("cancel"));
            dialogController.getPrimaryButton().setText(Lang.get("continue"));
            dialogController.showWarning(Lang.get("warning"));

            dialogController.addAction(dialogController.getSecondaryButton(),evt->dialogController.close());
            dialogController.addAction(dialogController.getPrimaryButton(),evt->{
                CategoryDAO categoryDAO = new CategoryDAO(eventId);
                try {
                    categoryDAO.openConnection();
                    categoryDAO.delete(getId());
                    categoryDAO.closeConnection();
                }catch(SQLException e1){
                    throw new RuntimeException(e1);
                }
                CategoryTableManager.getInstance().remove(this);
                dialogController.close();
            });

        });
        button.buttonTypeProperty().set(io.github.palexdev.materialfx.enums.ButtonType.RAISED);
        setRemoveButton(button);
        getRemoveButton().setVisible(false);
    }

    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public int getAboveAge() {
        return aboveAge.get();
    }

    public void setAboveAge(int aboveAge) {
        this.aboveAge.set(aboveAge);
    }

    public int getBelowAge() {
        return belowAge.get();
    }

    public void setBelowAge(int belowAge) {
        this.belowAge.set(belowAge);
    }

    public MFXButton getRemoveButton() {
        return removeButton.get();
    }

    public void setRemoveButton(MFXButton removeButton) {
        this.removeButton.set(removeButton);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public StringProperty nameProperty() {
        return name;
    }

    public IntegerProperty aboveAgeProperty() {
        return aboveAge;
    }

    public IntegerProperty belowAgeProperty() {
        return belowAge;
    }

    public ObjectProperty<MFXButton> removeButtonProperty() {
        return removeButton;
    }

    public int getEventId() {
        return eventId;
    }
}
