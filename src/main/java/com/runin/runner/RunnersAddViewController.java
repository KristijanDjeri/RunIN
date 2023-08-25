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

package com.runin.runner;

import com.runin.db.RunnerDAO;
import com.runin.model_managers.RunnerTableManager;
import com.runin.record.Runner;
import com.runin.shared.*;
import com.runin.stage.PopupStageViewController;
import com.runin.stage.StageViewController;
import io.github.palexdev.materialfx.beans.NumberRange;
import io.github.palexdev.materialfx.controls.*;
import io.github.palexdev.materialfx.controls.cell.MFXDateCell;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;

public class RunnersAddViewController implements Initializable {

    @FXML
    private MFXTextField firstNameTextField,lastNameTextField;
    @FXML
    private MFXDatePicker dateOfBirthPicker;
    @FXML
    private MFXRadioButton maleRadioButton,femaleRadioButton;
    @FXML
    private MFXToggleButton preciseAgeToggle;
    @FXML
    private TextField ageTextField;
    @FXML
    private Label addRunnerLabel,genderLabel;
    @FXML
    private MFXButton cancelButton,addRunnerButton;

    private PopupStageViewController popupStageViewController;
    private final RunnerDAO runnerDAO;

    RunnersAddViewController(){
        runnerDAO = new RunnerDAO();
        loadScene();
    }

    private void loadScene(){
        popupStageViewController = new PopupStageViewController();
        popupStageViewController.loadScene(FXMLPaths.RUNNER_ADD.PATH, this);
        popupStageViewController.show();
    }

    @FXML
    public void add(){

        String firstName = firstNameTextField.getText();
        String lastName = lastNameTextField.getText();

        if(Date.valueOf(dateOfBirthPicker.getValue()).toLocalDate().isAfter(LocalDate.now())||dateOfBirthPicker.getValue()==null){
            DialogController dialogController = new DialogController(StageViewController.getStage());
            dialogController.setContentText(Lang.get("dialogRA1"));
            dialogController.showWarning(Lang.get("warning"));
            return;
        }

        boolean nameExists;
        try{
            runnerDAO.openConnection();
            nameExists = runnerDAO.checkIfNameExists(firstName,lastName);
            runnerDAO.closeConnection();
        }catch(SQLException e){
            throw  new RuntimeException(e);
        }

        if(nameExists) {
            DialogController dialogController = new DialogController(StageViewController.getStage());
            dialogController.setTitle(Lang.get("warning"));
            dialogController.setContentText(Lang.get("dialogRA2")+"\n\n"+Lang.get("dialogText1"));

            dialogController.getSecondaryButton().setText(Lang.get("cancel"));
            dialogController.getPrimaryButton().setText(Lang.get("continue"));

            dialogController.showWarning(Lang.get("warning"));

            dialogController.addAction(dialogController.getSecondaryButton(),evt->dialogController.close());
            dialogController.addAction(dialogController.getPrimaryButton(),evt->{
                addRunner();
                dialogController.close();
            });

        }else{
            addRunner();
        }

    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void addRunner(){

        Date date = (!preciseAgeToggle.isSelected())?Date.valueOf(LocalDate.now().minusYears(Short.parseShort(ageTextField.getText()))):Date.valueOf(dateOfBirthPicker.getValue());

        int id;

        Runner runner = new Runner(0, firstNameTextField.getText(), lastNameTextField.getText(),
                date, maleRadioButton.isSelected(), null, null);
        try{
            runnerDAO.openConnection();
            id = runnerDAO.getNextIdValue();
            runnerDAO.insert(runner);
            runnerDAO.closeConnection();
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
        RunnerTableManager.getInstance().refresh();

        File profilePicsDir = new File(ProjectPaths.ASSETS.getPath()+"/profile-pictures");
        if(profilePicsDir.isDirectory()){
            List<File> icons = Arrays.stream(Objects.requireNonNull(profilePicsDir.listFiles((d, name) -> name.endsWith(".png")))).toList();
            Random rand = new Random();
            int index = rand.nextInt(icons.size());
            try {
                new File(ProjectPaths.H2_DB_LIBRARY.getPath()+"/runners").mkdirs();
                Files.copy(Paths.get(icons.get(index).getAbsolutePath()),Paths.get(ProjectPaths.H2_DB_LIBRARY.getPath()+"/runners/" + id + ".png"), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        close();
    }

    @FXML
    public void close(){
        popupStageViewController.close();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        preciseAgeToggle.onActionProperty().set(actionEvent -> {
            dateOfBirthPicker.setDisable(!preciseAgeToggle.isSelected());
            ageTextField.setDisable(preciseAgeToggle.isSelected());
        });
        dateOfBirthPicker.setDisable(!preciseAgeToggle.isSelected());
        ageTextField.setDisable(preciseAgeToggle.isSelected());
        ageTextField.setTextFormatter(new TextFormatter<>(change->{
            String newText = change.getControlNewText();
            if(newText.isEmpty()){
                return change;
            }
            if(!newText.matches("\\d*") && Integer.parseInt(newText)>120){
                return null;
            }
            return change;
        }));
        dateOfBirthPicker.setValue(LocalDate.now());
        dateOfBirthPicker.setClosePopupOnChange(true);
        dateOfBirthPicker.setYearsRange(NumberRange.of(LocalDate.now().minusYears(100).getYear(),LocalDate.now().getYear()));
        dateOfBirthPicker.setCellFactory(new Function<>() {
            @Override
            public MFXDateCell apply(LocalDate t) {
                return new MFXDateCell(dateOfBirthPicker, t) {
                    @Override
                    public void updateItem(LocalDate item) {
                        super.updateItem(item);
                        setDisable(item.isAfter(LocalDate.now())||item.isBefore(LocalDate.now().minusYears(100)));
                    }
                };
            }
        });

        firstNameTextField.requestFocus();
        firstNameTextField.setOnAction(x->lastNameTextField.requestFocus());
        lastNameTextField.setOnAction(x->{
            if(preciseAgeToggle.isSelected()){
                dateOfBirthPicker.requestFocus();
            }else{
                ageTextField.requestFocus();
            }
        });
        ageTextField.setOnAction(x->add());
        dateOfBirthPicker.setOnKeyPressed(keyEvent -> {
            if(keyEvent.getCode()==KeyCode.ENTER){
                add();
            }
        });

        loadLang();

    }

    private void loadLang(){
        addRunnerLabel.setText(Lang.get("addRunner"));
        firstNameTextField.setFloatingText(Lang.get("firstName"));
        lastNameTextField.setFloatingText(Lang.get("lastName"));
        genderLabel.setText(Lang.get("gender"));
        maleRadioButton.setText(Lang.get("male"));
        femaleRadioButton.setText(Lang.get("female"));
        preciseAgeToggle.setText(Lang.get("preciseAge"));
        dateOfBirthPicker.setFloatingText(Lang.get("dateOfBirth"));
        ageTextField.setPromptText(Lang.get("age"));
        cancelButton.setText(Lang.get("cancel"));
        addRunnerButton.setText(Lang.get("addRunner"));

        TextResize.resize(addRunnerLabel);
        TextResize.resize(addRunnerButton);
        TextResize.resize(cancelButton);

    }

}
