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

package com.runin.settings;

import com.runin.model_managers.EventTableManager;
import com.runin.model_managers.RunnerTableManager;
import com.runin.shared.*;
import com.runin.stage.StageViewController;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.net.URL;
import java.util.*;

public class SettingsViewController extends ImportExportBackup implements Initializable {

    @FXML
    private MFXButton backupButton,importButton, exportButton, deleteButton, creditsButton;
    @FXML
    private MFXComboBox<String> languageComboBox;
    @FXML
    private Label settingsLabel, languageLabel, backupLabel;
    @FXML
    private MFXTextField backupTextField;

    private final HashMap<String,Locale> languages = new HashMap<>();
    private PropertiesIO propertiesIO;
    private String currentLanguageCode;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        backupButton.setGraphic(ImageManager.getImageView(ImageManager.getIcon("folder"),30));
        importButton.setGraphic(ImageManager.getImageView(ImageManager.getIcon("download"),30));
        exportButton.setGraphic(ImageManager.getImageView(ImageManager.getIcon("upload"),30));
        creditsButton.setGraphic(ImageManager.getImageView(ImageManager.getIcon("post"),30));

        for(Locale locale:Lang.getAllLocales()){
            languages.put(locale.getDisplayLanguage(), locale);
        }

        propertiesIO = new PropertiesIO(ProjectPaths.CONFIG.getPath()+"/settings.properties");
        String[] split = propertiesIO.read("language").split("_");
        String selectedLang = Locale.of(split[0],split[1]).getDisplayLanguage();

        languageComboBox.getItems().addAll(languages.keySet());
        if(languageComboBox.getItems().contains(selectedLang)){
            languageComboBox.selectItem(selectedLang);
        }

        currentLanguageCode = Lang.getLocale().getLanguage()+"_"+Lang.getLocale().getCountry().toUpperCase();

        languageComboBox.selectedItemProperty().addListener((obs,oldVal,newVal)->{
            if(!oldVal.equals(newVal)){
                Locale locale = languages.get(newVal);
                String languageCode = locale.getLanguage()+"_"+locale.getCountry().toUpperCase();
                if(!currentLanguageCode.equals(languageCode)) {
                    Lang.setLanguage(languageCode);
                    Lang.loadSelectedLanguage();
                    DialogController dialogController = new DialogController(StageViewController.getStage());
                    dialogController.getPrimaryButton().setText(Lang.get("ok"));
                    dialogController.setContentText(Lang.get("dialogS1"));
                    dialogController.addAction(dialogController.getPrimaryButton(), e -> dialogController.close());
                    dialogController.showWarning(Lang.get("warning"));
                    Lang.setLanguage(currentLanguageCode);
                    Lang.loadSelectedLanguage();
                }
                propertiesIO.write("language",languageCode);
            }
        });

        backupTextField.setOnAction(x->backupTextField.setText(backupAction(backupTextField.getText())));

        backupTextField.focusedProperty().addListener((observable,wasFocused,isNowFocused)->{
            if(wasFocused&&!isNowFocused){
                backupTextField.setText(backupAction(backupTextField.getText()));
            }
        });

        deleteButton.setOnAction(x->{
            DialogController dialogController = new DialogController(StageViewController.getStage());
            dialogController.getSecondaryButton().setText(Lang.get("cancel"));
            dialogController.getPrimaryButton().setText(Lang.get("iAmSure"));
            dialogController.setContentText(Lang.get("dialogS3"));
            dialogController.addAction(dialogController.getSecondaryButton(),y->dialogController.close());
            dialogController.addAction(dialogController.getPrimaryButton(),y->{
                new FileManager().deleteDirectory(ProjectPaths.H2_DB_LIBRARY.getPath());
                RunnerTableManager.getInstance().refresh();
                Objects.requireNonNull(EventTableManager.getInstance(Section.EVENT)).refresh();
                Objects.requireNonNull(EventTableManager.getInstance(Section.HISTORY)).refresh();
                dialogController.close();
            });
            dialogController.showWarning(Lang.get("warning"));
        });

        backupButton.setOnAction(x->{
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle(Lang.get("selectDirectory"));
            File directory = directoryChooser.showDialog(StageViewController.getStage());
            backupTextField.setText(backupAction(directory.getAbsolutePath()));
        });

        backupTextField.setEditable(true);

        backupTextField.setText(propertiesIO.read("backup"));

        creditsButton.setOnAction(x->new CreditsController());

        importButton.setOnAction(x->importOption());
        exportButton.setOnAction(x->exportOption());

        loadLang();
        new Thread(this::backup).start();

    }

    private String backupAction(String text){
        String previousBackupLocation = propertiesIO.read("backup");
        if(text.isEmpty()){
            propertiesIO.write("backup","");
            new FileManager().copyToDirectory(previousBackupLocation,text,"RunINBackup_",".zip");
            deletePastBackupLocation(previousBackupLocation);
            backup();
            return "";
        }
        File file = new File(text);
        if(!file.isDirectory()){
            DialogController dialogController = new DialogController(StageViewController.getStage());
            dialogController.getPrimaryButton().setText(Lang.get("ok"));
            dialogController.addAction(dialogController.getPrimaryButton(),y->dialogController.close());
            dialogController.setContentText(Lang.get("dialogS2"));
            dialogController.showWarning(Lang.get("warning"));
            return propertiesIO.read("backup");
        }
        propertiesIO.write("backup",text);
        if(!previousBackupLocation.equals(text)){
            new FileManager().copyToDirectory(previousBackupLocation,text,"RunINBackup_",".zip");
            deletePastBackupLocation(previousBackupLocation);
            backup();
        }
        return text;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void deletePastBackupLocation(String previousBackupLocation){
        File previousBackupDir = new File(previousBackupLocation);
        List<File> backups = Arrays.stream(Objects.requireNonNull(previousBackupDir.listFiles((d, name) -> name.startsWith("RunINBackup_") && name.endsWith(".zip")))).toList();
        for(File f:backups){
            f.delete();
        }
    }

    private void importOption(){

        SimpleIntegerProperty option = new SimpleIntegerProperty(3);
        new DatabaseChooserController(option);

        switch (option.get()){
            case 0 -> importEverything();
            case 1 -> importRunners();
            case 2 -> importEvents();
        }

    }

    private void exportOption(){

        SimpleIntegerProperty option = new SimpleIntegerProperty(3);
        new DatabaseChooserController(option);

        switch(option.get()){
            case 0 -> exportEverything();
            case 1 -> exportRunners();
            case 2 -> exportEvents();
        }

    }

    private void loadLang(){
        settingsLabel.setText(Lang.get("settings"));
        languageLabel.setText(Lang.get("language")+":");
        backupLabel.setText(Lang.get("backup")+":");
        importButton.setText(Lang.get("import"));
        exportButton.setText(Lang.get("export"));
        creditsButton.setText(Lang.get("credits"));
        deleteButton.setText(Lang.get("deleteAll"));

        TextResize.resize(importButton);
        TextResize.resize(exportButton);
        TextResize.resize(creditsButton);
        TextResize.resize(deleteButton);

    }

}
