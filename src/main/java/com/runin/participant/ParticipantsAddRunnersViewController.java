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

import com.runin.db.DistanceDAO;
import com.runin.db.ParticipantDAO;
import com.runin.model_managers.EventTableManager;
import com.runin.model_managers.ParticipantTableManager;
import com.runin.model_managers.RunnerParticipantTableManager;
import com.runin.models.EventTable;
import com.runin.models.RunnerParticipantTable;
import com.runin.models.RunnerTable;
import com.runin.record.Distance;
import com.runin.record.Participant;
import com.runin.record.Runner;
import com.runin.shared.*;
import com.runin.stage.PopupStageViewController;
import com.runin.stage.StageViewController;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXCheckbox;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.controls.legacy.MFXLegacyTableRow;
import io.github.palexdev.materialfx.controls.legacy.MFXLegacyTableView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ParticipantsAddRunnersViewController implements Initializable {

    @FXML
    private MFXTextField searchTextField;
    @FXML
    private MFXLegacyTableView<EventTable> eventTableView;
    @FXML
    private MFXLegacyTableView<RunnerParticipantTable> participantTableView;
    @FXML
    private MFXComboBox<String> distanceComboBox;
    @FXML
    private VBox vbox1,vbox2;
    @FXML
    private MFXButton nextButton,backButton,searchButton,setButton;
    @FXML
    private MFXCheckbox checkAll;
    @FXML
    private Label selectEventLabel,selectDistanceLabel,setToLabel;


    private PopupStageViewController popupStageViewController;
    private EventTableManager eventTableManager;
    private final ObservableList<RunnerTable> selectedRunners = FXCollections.observableArrayList();
    private final ObservableList<String> distances = FXCollections.observableArrayList();

    public ParticipantsAddRunnersViewController(ObservableList<RunnerTable> selectedRunners){
        this.selectedRunners.addAll(selectedRunners);
        loadScene();
    }

    private void loadScene(){
        popupStageViewController = new PopupStageViewController();
        popupStageViewController.loadScene(FXMLPaths.PARTICIPANT_ADD_RUNNERS.PATH, this);
        popupStageViewController.show();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        eventTableManager = new EventTableManager(Section.NULL);

        eventTableManager.loadTableView(eventTableView);
        eventTableView.getColumns().remove(3);
        eventTableView.getColumns().get(0).setMinWidth(200);
        eventTableView.getColumns().get(0).setMaxWidth(Control.USE_COMPUTED_SIZE);
        eventTableView.getColumns().get(1).setMinWidth(150);
        eventTableView.getColumns().get(2).setMinWidth(80);
        eventTableView.getColumns().get(2).setMaxWidth(Control.USE_COMPUTED_SIZE);

        eventTableView.setRowFactory(x->{
            MFXLegacyTableRow<EventTable> tableRow = new MFXLegacyTableRow<>();
            tableRow.setOnMouseClicked(mouseEvent -> {
                if(mouseEvent.getButton()  == MouseButton.PRIMARY && mouseEvent.getClickCount()>1){
                    next();
                }
            });
            tableRow.setOnMouseEntered(mouseEvent -> {
                if(!tableRow.isEmpty()){
                    tableRow.getItem().getRemoveButton().setVisible(true);
                }
            });
            tableRow.setOnMouseExited(mouseEvent -> {
                if(!tableRow.isEmpty()){
                    tableRow.getItem().getRemoveButton().setVisible(false);
                }
            });
            return tableRow;
        });

        eventTableManager.refresh();
        eventTableManager.search("",(short)2);

        RunnerParticipantTableManager runnerParticipantTableManager = new RunnerParticipantTableManager();
        runnerParticipantTableManager.loadTableView(participantTableView);

        for(RunnerTable runnerTable:selectedRunners){
            runnerParticipantTableManager.add(new RunnerParticipantTable(runnerTable.getName()));
        }

        nextButton.setText(Lang.get("next"));
        backButton.setText(Lang.get("close"));
        setButton.setText(Lang.get("assign"));
        selectEventLabel.setText(Lang.get("selectEvent"));
        selectDistanceLabel.setText(Lang.get("selectDistance"));
        setToLabel.setText(Lang.get("assign"));
        searchTextField.setPromptText(Lang.get("search"));
        searchButton.setText(Lang.get("search"));
        checkAll.setText(Lang.get("selectAll"));

        TextResize.resize(selectEventLabel);
        TextResize.resize(selectDistanceLabel);
        TextResize.resize(nextButton);
        TextResize.resize(backButton);

    }

    @FXML
    private void setDistance(){
        DistanceDAO distanceDAO = new DistanceDAO(eventTableView.getSelectionModel().getSelectedItem().getId());
        Distance distance;
        try{
            distanceDAO.openConnection();
            distance = distanceDAO.getDistanceViaName(distanceComboBox.getSelectedItem());
            distanceDAO.closeConnection();
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
        for(RunnerParticipantTable rpt:participantTableView.getItems()){
            if(rpt.getCheckBox().isSelected()){
                rpt.setDistance(distance.name());
                rpt.getCheckBox().setSelected(false);
            }
        }
        checkAll.setSelected(false);
    }

    @FXML
    private void checkAll(){
        boolean check = checkAll.isSelected();
        for(RunnerParticipantTable rpt:participantTableView.getItems()){
            rpt.getCheckBox().setSelected(check);
        }
    }

    @FXML
    private void search(){
        eventTableManager.search(searchTextField.getText(),(short)2);
    }

    @FXML
    private void next(){
        if(eventTableView.getSelectionModel().getSelectedItem()==null){
            return;
        }
        if(vbox1.isVisible()){
            vbox1.setVisible(false);
            DistanceDAO distanceDAO = new DistanceDAO(eventTableView.getSelectionModel().getSelectedItem().getId());
            try {
                distances.clear();
                distanceComboBox.getItems().clear();
                distanceDAO.openConnection();
                for (Distance d : distanceDAO.getAll()) {
                    distances.add(d.name());
                }
                distanceComboBox.getItems().addAll(distances);
                distanceComboBox.getSelectionModel().selectFirst();
                distanceDAO.closeConnection();
            }catch (SQLException e){
                throw new RuntimeException(e);
            }
            vbox2.setVisible(true);
            backButton.setText(Lang.get("back"));
            nextButton.setText(Lang.get("add"));
        }else {
            if (eventTableView.getSelectionModel().getSelectedItem() != null) {
                for(RunnerParticipantTable rpt:participantTableView.getItems()){
                    if(rpt.getDistance().isEmpty()){
                        DialogController dialogController = new DialogController(StageViewController.getStage());
                        dialogController.setContentText(Lang.get("dialogPAR1"));
                        dialogController.showWarning(Lang.get("warning"));
                        return;
                    }
                }
                ParticipantDAO participantDAO = new ParticipantDAO(eventTableView.getSelectionModel().getSelectedItem().getId());
                try {
                    participantDAO.openConnection();
                    for(int i=0;i< selectedRunners.size();i++){
                        Runner runner = selectedRunners.get(i).getRunner();
                        if(!participantDAO.runnerExists(runner.id())) {
                            Participant participant = getParticipant(runner, i);
                            participantDAO.insert(participant);
                        }
                    }
                    participantDAO.closeConnection();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                popupStageViewController.close();
                StageViewController.getInstance().setScene(FXMLPaths.TABLE_VIEW.PATH, new ParticipantsViewController(eventTableView.getSelectionModel().getSelectedItem().getEvent()),Section.EVENT, Save.NO);
                ParticipantTableManager.getInstance().refresh();
            }
        }
    }

    private Participant getParticipant(Runner runner, int i) throws SQLException {
        DistanceDAO distanceDAO = new DistanceDAO(eventTableView.getSelectionModel().getSelectedItem().getId());
        distanceDAO.openConnection();
        Distance distance = distanceDAO.getDistanceViaName(participantTableView.getItems().get(i).getDistance());
        distanceDAO.closeConnection();
        return new Participant(0, runner.first_name(), runner.last_name(), selectedRunners.get(i).getAge(),selectedRunners.get(i).getRunner().gender(),distance.id(), (short)0, runner.id());
    }

    @FXML
    private void back(){
        if(vbox1.isVisible()) {
            popupStageViewController.close();
        }else{
            vbox2.setVisible(false);
            vbox1.setVisible(true);
            backButton.setText(Lang.get("close"));
            nextButton.setText(Lang.get("next"));
        }
    }

}
