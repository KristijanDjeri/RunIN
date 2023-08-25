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

package com.runin.participant;

import com.runin.event.EventsDetailedViewController;
import com.runin.model_managers.ParticipantTableManager;
import com.runin.models.ParticipantTable;
import com.runin.record.Event;
import com.runin.shared.*;
import com.runin.stage.StageViewController;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.controls.legacy.MFXLegacyTableView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

public class ParticipantsViewController implements Initializable {

    @FXML
    private MFXLegacyTableView<ParticipantTable> tableView;
    @FXML
    private MFXTextField searchTextField;
    @FXML
    private MFXButton backButton,searchButton,actionButton;
    private final Event event;
    private final ParticipantTableManager participantTableManager;

    public ParticipantsViewController(Event event){
        this.event = event;
        participantTableManager = new ParticipantTableManager(event);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        participantTableManager.loadTableView(tableView);
        participantTableManager.refresh();

        searchTextField.setPromptText(Lang.get("search"));
        searchButton.setText(Lang.get("search"));
        actionButton.setText(Lang.get("addParticipant"));

        searchButton.setGraphic(ImageManager.getImageView(ImageManager.getIcon("search"),20));
        actionButton.setGraphic(ImageManager.getImageView(ImageManager.getIcon("user-add"),25));

        TextResize.resize(actionButton);
        TextResize.resize(searchButton);

        backButton.setOnAction(x->back());
        searchTextField.setOnAction(x->search());
        searchButton.setOnAction(x->search());
        actionButton.setOnAction(x->addParticipant());

    }

    private void search(){
        participantTableManager.search(searchTextField.getText());
    }

    private void addParticipant(){
        new ParticipantsAddViewController(event);
    }

    private void back(){
        StageViewController.getInstance().setScene(FXMLPaths.EVENT_DETAILED.PATH, new EventsDetailedViewController(event,Section.EVENT), Section.EVENT, Save.NO);
    }
}
