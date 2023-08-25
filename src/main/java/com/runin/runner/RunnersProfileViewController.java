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
import com.runin.participant.ParticipantsViewController;
import com.runin.record.Event;
import com.runin.record.Runner;
import com.runin.result.ResultViewController;
import com.runin.shared.*;
import com.runin.stage.StageViewController;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXDatePicker;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.controls.cell.MFXDateCell;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
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

public class RunnersProfileViewController implements Initializable {

    @FXML
    private ImageView profileImageView;
    @FXML
    private MFXTextField firstNameTextField,lastNameTextField,emailTextField,phoneTextField;
    @FXML
    private MFXDatePicker dateOfBirthPicker;
    @FXML
    private MFXComboBox<String> genderComboBox;

    private final Runner runner;
    private final Event event;
    private final Section section;

    public RunnersProfileViewController(Runner runner, Event event, Section section){
        this.runner = runner;
        this.event = event;
        this.section = section;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadImageView();
        loadInput();
        loadLang();
    }

    private void loadInput(){
        firstNameTextField.setText(runner.first_name());
        lastNameTextField.setText(runner.last_name());
        if(runner.date_of_birth()!=null) {
            dateOfBirthPicker.setValue(runner.date_of_birth().toLocalDate());
        }
        if(runner.email()!=null) {
            emailTextField.setText(runner.email());
        }
        if(runner.phone_number()!=null) {
            phoneTextField.setText(runner.phone_number());
        }

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

        genderComboBox.getItems().add(Lang.get("male"));
        genderComboBox.getItems().add(Lang.get("female"));

        if(runner.gender()!=null) {
            genderComboBox.selectIndex(runner.gender() ? 0 : 1);
        }

        phoneTextField.setTextLimit(20);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void loadImageView(){
        profileImageView.setClip(new Circle(85,85,85));
        profileImageView.setStyle("-fx-border-color:lightgray;-fx-border-radius:50%;-fx-background-color:lightgray;");

        File[] foundFiles = getFoundFiles(String.valueOf(runner.id()));

        if(foundFiles!=null&&foundFiles.length>0){
            profileImageView.setImage(ImageManager.getImageFromSystem(foundFiles[0].getAbsolutePath()));
        }else{
            File profilePicsDir = new File(ProjectPaths.ASSETS.getPath()+"/profile-pictures");
            if(profilePicsDir.isDirectory()){
                List<File> icons = Arrays.stream(Objects.requireNonNull(profilePicsDir.listFiles((d, name) -> name.endsWith(".png")))).toList();
                Random rand = new Random();
                int index = rand.nextInt(icons.size());
                try {
                    new File(ProjectPaths.H2_DB_LIBRARY.getPath()+"/runners").mkdirs();
                    Files.copy(Paths.get(icons.get(index).getAbsolutePath()),Paths.get(ProjectPaths.H2_DB_LIBRARY.getPath()+"/runners/" + runner.id() + ".png"), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            foundFiles = getFoundFiles(String.valueOf(runner.id()));
            if(foundFiles!=null&&foundFiles.length>0){
                profileImageView.setImage(ImageManager.getImageFromSystem(foundFiles[0].getAbsolutePath()));
            }
        }

        if(runner.id()!=0) {
            profileImageView.setCursor(Cursor.HAND);
            profileImageView.setOnMouseClicked(mouseEvent -> {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setSelectedExtensionFilter(
                        new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif", "*.webp")
                );
                File file = fileChooser.showOpenDialog(StageViewController.getStage());

                File outputDir = new File(ProjectPaths.H2_DB_LIBRARY.getPath() + "/runners");
                if (!outputDir.exists()) {
                    outputDir.mkdirs();
                }

                try {
                    Files.copy(Paths.get(file.getAbsolutePath()), Paths.get(outputDir.getAbsolutePath() + "/" + runner.id() + file.getName().substring(file.getName().lastIndexOf('.'))), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                profileImageView.setImage(ImageManager.getImageFromSystem(file.getAbsolutePath()));
            });
        }
    }

    private File[] getFoundFiles(String name) {
        String dirPath = ProjectPaths.H2_DB_LIBRARY.getPath()+"/runners";
        String[] extensions = new String[]{
          "png","jpg","jpeg","bmp","gif","webp"
        };
        return new FileManager().findFilesByNameAndExtension(dirPath,name,extensions);
    }

    private void saveChanges(){
        if(runner.id()==0) {
            return;
        }
        int id = runner.id();
        String firstName = firstNameTextField.getText().length()<5?runner.first_name():firstNameTextField.getText();
        String lastName = lastNameTextField.getText().length()<5?runner.first_name():lastNameTextField.getText();
        Date dateOfBirth = Date.valueOf(dateOfBirthPicker.getValue());
        boolean gender = genderComboBox.getSelectedIndex()==0;
        String email = emailTextField.getText();
        String phone = phoneTextField.getText();

        Runner runner1 = new Runner(id, firstName, lastName, dateOfBirth, gender, email, phone);
        RunnerDAO runnerDAO = new RunnerDAO();
        try {
            runnerDAO.openConnection();
            runnerDAO.update(runner1);
            runnerDAO.closeConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        RunnerTableManager.getInstance().refresh();
    }

    @FXML
    private void back(){
        saveChanges();
        if(section == Section.RUNNER) {
            StageViewController.getInstance().setScene(FXMLPaths.TABLE_VIEW.PATH, new RunnersMainViewController(), section, Save.NO);
        }else if(section == Section.EVENT){
            StageViewController.getInstance().setScene(FXMLPaths.TABLE_VIEW.PATH, new ParticipantsViewController(event), section, Save.NO);
        }else if(section == Section.HISTORY){
            StageViewController.getInstance().setScene(FXMLPaths.TABLE_VIEW.PATH, new ResultViewController(event,section), section, Save.NO);
        }
    }

    private void loadLang(){
        firstNameTextField.setFloatingText(Lang.get("firstName"));
        lastNameTextField.setFloatingText(Lang.get("lastName"));
        dateOfBirthPicker.setFloatingText(Lang.get("dateOfBirth"));
        emailTextField.setFloatingText(Lang.get("emailAddress"));
        genderComboBox.setFloatingText(Lang.get("gender"));
        phoneTextField.setFloatingText(Lang.get("phoneNumber"));
    }

}
