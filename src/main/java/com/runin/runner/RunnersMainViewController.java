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

import com.runin.participant.ParticipantsAddRunnersViewController;
import com.runin.model_managers.RunnerTableManager;
import com.runin.models.RunnerTable;
import com.runin.shared.ImageManager;
import com.runin.shared.Lang;
import com.runin.shared.ProjectPaths;
import com.runin.shared.TextResize;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.controls.legacy.MFXLegacyTableView;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.HBox;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class RunnersMainViewController implements Initializable {

    @FXML
    private MFXLegacyTableView<RunnerTable> tableView;
    @FXML
    private MFXTextField searchTextField;
    @FXML
    private HBox bottomHBox;
    @FXML
    private MFXButton backButton,searchButton,actionButton,secondaryButton,primaryButton;
    private static RunnersMainViewController INSTANCE;
    private RunnerTableManager runnerTableManager;
    private ObservableList<RunnerTable> selectedRunners;

    public static RunnersMainViewController getInstance(){
        if(INSTANCE==null){
            INSTANCE = new RunnersMainViewController();
        }
        return INSTANCE;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if(runnerTableManager == null){
            runnerTableManager = RunnerTableManager.getInstance();
            runnerTableManager.loadTableView(tableView);
            selectedRunners = runnerTableManager.getSelectedRunners();
            runnerTableManager.refresh();
            selectedRunners.addListener((ListChangeListener<? super RunnerTable>) change -> bottomHBox.setVisible(!selectedRunners.isEmpty()));
        }

        searchButton.setText(Lang.get("search"));
        actionButton.setText(Lang.get("addRunner"));
        secondaryButton.setText(Lang.get("unselectAll"));
        primaryButton.setText(Lang.get("insertInto"));
        searchTextField.setPromptText(Lang.get("search"));

        searchButton.setGraphic(ImageManager.getImageView(ImageManager.getIcon("search"),20));
        actionButton.setGraphic(ImageManager.getImageView(ImageManager.getIcon("user-add"),25));

        TextResize.resize(actionButton);
        TextResize.resize(searchButton);

        backButton.setVisible(false);
        secondaryButton.setOnAction(x->search());
        actionButton.setOnAction(x->addRunner());
        secondaryButton.setOnAction(x->unselectAll());
        primaryButton.setOnAction(x->addTo());
        searchTextField.setOnAction(x->search());

        new File(ProjectPaths.H2_DB_LIBRARY.getPath()+"/runners").mkdirs();

    }

    private void search(){
        runnerTableManager.search(searchTextField.getText());
    }

    public void addRunner(){
        runnerTableManager.unselectAll();
        new RunnersAddViewController();
    }

    public void addTo(){
        for(RunnerTable rt:tableView.getItems()){
            if(rt.getCheckBox().isSelected()){
                rt.getCheckBox().setSelected(false);
            }
        }
        new ParticipantsAddRunnersViewController(selectedRunners);
        runnerTableManager.unselectAll();
    }

    public void unselectAll(){
        runnerTableManager.unselectAll();
    }

}
