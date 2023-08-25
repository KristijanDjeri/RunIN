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

import com.runin.db.RunnerDAO;
import com.runin.model_managers.RunnerTableManager;
import com.runin.record.Runner;
import com.runin.shared.*;
import com.runin.stage.StageViewController;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXCheckbox;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;

@SuppressWarnings("unused")
public class RunnerTable {

    public static BooleanProperty checked = new SimpleBooleanProperty(false);
    public static int numberOfChecks=0;
    public static ObservableList<RunnerTable> selectedRunners = FXCollections.observableArrayList();
    private final ObjectProperty<MFXCheckbox> checkBox = new SimpleObjectProperty<>();
    private final StringProperty name = new SimpleStringProperty("");
    private final IntegerProperty age = new SimpleIntegerProperty();
    private final StringProperty gender = new SimpleStringProperty("");
    private final ObjectProperty<MFXButton> removeButton = new SimpleObjectProperty<>();
    private final IntegerProperty id = new SimpleIntegerProperty();

    private final Runner runner;

    public RunnerTable(Runner runner){
        this.runner = runner;
        setId(runner.id());
        setName(runner.first_name()+" "+runner.last_name());
        setAge(LocalDate.now().minusYears(runner.date_of_birth().toLocalDate().getYear()).getYear());
        setGender(runner.gender()? Lang.get("male"):Lang.get("female"));
        setButton();
        loadCheckBox();
    }

    public Runner getRunner(){
        return runner;
    }

    private void setButton(){
        MFXButton button = new MFXButton("",ImageManager.getImageView(ImageManager.getIcon("delete"),30));
        button.setStyle("-fx-background-color:transparent;");
        button.setOnAction(e->{
            DialogController dialogController = new DialogController(StageViewController.getStage());
            dialogController.setContentText(Lang.get("dialogRT1")+" ["+getName()+"]");
            dialogController.getSecondaryButton().setText(Lang.get("close"));
            dialogController.getPrimaryButton().setText(Lang.get("continue"));
            dialogController.showWarning(Lang.get("warning"));
            dialogController.addAction(dialogController.getSecondaryButton(),evt->dialogController.close());
            dialogController.addAction(dialogController.getPrimaryButton(),evt->{
                try{
                    RunnerDAO runnerDAO = new RunnerDAO();
                    runnerDAO.openConnection();
                    runnerDAO.delete(getId());
                    runnerDAO.closeConnection();
                }catch (SQLException e1){
                    throw new RuntimeException(e1);
                }

                String dirPath = ProjectPaths.H2_DB_LIBRARY.getPath()+"/runners";
                String name = String.valueOf(getId());
                String[] extensions = new String[]{
                        "png","jpg","jpeg","bmp","gif","webp"
                };
                File[] foundFiles = new FileManager().findFilesByNameAndExtension(dirPath,name,extensions);

                if(foundFiles!=null&&foundFiles.length>0){
                    new FileManager().deleteFile(foundFiles[0].getAbsolutePath());
                }

                RunnerTableManager.getInstance().remove(this);
                dialogController.close();
            });
        });
        button.buttonTypeProperty().set(io.github.palexdev.materialfx.enums.ButtonType.RAISED);
        setRemoveButton(button);
        getRemoveButton().setVisible(false);
    }

    private void loadCheckBox(){
        MFXCheckbox checkbox = new MFXCheckbox();
        checkbox.setText("");
        checkbox.setSelected(false);
        checkbox.setVisible(false);
        checkbox.setScaleX(1.1);
        checkbox.setScaleY(1.1);
        checkbox.setOnAction(x->{
            if(checkbox.isSelected()){
                numberOfChecks++;
                selectedRunners.add(this);
            }else{
                numberOfChecks--;
                selectedRunners.remove(this);
            }
            checked.set(numberOfChecks>0);
        });
        checked.addListener((observableValue,oldValue,newValue)->{
            if(!newValue){
                checkbox.setSelected(false);
            }
            checkbox.setVisible(newValue);

        });
        setCheckBox(checkbox);
    }

    public static void unselectAll(){
        checked.set(false);
        selectedRunners.clear();
        numberOfChecks=0;
    }

    public BooleanProperty getChecked(){
        return checked;
    }

    public MFXCheckbox getCheckBox() {
        return checkBox.get();
    }

    public void setCheckBox(MFXCheckbox checkBox) {
        this.checkBox.set(checkBox);
    }

    public int getId(){
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public String getName(){
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public int getAge(){
        return age.get();
    }

    public void setAge(Integer age) {
        this.age.set(age);
    }

    public String getGender(){
        return gender.get();
    }

    public void setGender(String gender) {
        this.gender.set(gender);
    }

    public MFXButton getRemoveButton(){
        return removeButton.getValue();
    }

    public void setRemoveButton(MFXButton remove) {
        this.removeButton.setValue(remove);
    }

    public static boolean isChecked() {
        return checked.get();
    }

    public static BooleanProperty checkedProperty() {
        return checked;
    }

    public static void setChecked(boolean checked) {
        RunnerTable.checked.set(checked);
    }

    public static int getNumberOfChecks() {
        return numberOfChecks;
    }

    public static void setNumberOfChecks(int numberOfChecks) {
        RunnerTable.numberOfChecks = numberOfChecks;
    }

    public static ObservableList<RunnerTable> getSelectedRunners() {
        return selectedRunners;
    }

    public static void setSelectedRunners(ObservableList<RunnerTable> selectedRunners) {
        RunnerTable.selectedRunners = selectedRunners;
    }

    public ObjectProperty<MFXCheckbox> checkBoxProperty() {
        return checkBox;
    }

    public StringProperty nameProperty() {
        return name;
    }

    public IntegerProperty ageProperty() {
        return age;
    }

    public void setAge(int age) {
        this.age.set(age);
    }

    public StringProperty genderProperty() {
        return gender;
    }

    public ObjectProperty<MFXButton> removeButtonProperty() {
        return removeButton;
    }

    public IntegerProperty idProperty() {
        return id;
    }
}
