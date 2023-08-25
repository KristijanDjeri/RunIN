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

package com.runin.result;

import com.runin.db.CategoryDAO;
import com.runin.db.EventDAO;
import com.runin.db.ParticipantDAO;
import com.runin.db.ResultDAO;
import com.runin.event.EventsDetailedViewController;
import com.runin.model_managers.EventTableManager;
import com.runin.model_managers.ResultTableManager;
import com.runin.models.ResultTable;
import com.runin.record.Category;
import com.runin.record.Event;
import com.runin.record.Participant;
import com.runin.record.Result;
import com.runin.shared.*;
import com.runin.stage.StageViewController;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.controls.legacy.MFXLegacyTableView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class ResultViewController implements Initializable {

    @FXML
    private MFXLegacyTableView<ResultTable> tableView;
    @FXML
    private MFXTextField searchTextField;
    @FXML
    private MFXButton backButton,searchButton,actionButton;
    private final ResultTableManager resultTableManager;
    private final Event event;
    private final Section section;
    private ResultFilter resultFilter;
    private final ResultFilterController resultFilterController;

    public ResultViewController(Event event,Section section){
        this.event = event;
        this.section = section;
        resultTableManager = new ResultTableManager(event);
        resultFilter = new ResultFilter((short)0,(short)0,(short)0);
        resultFilterController = new ResultFilterController(event);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        backButton.setOnAction(x->back());

        if (event.editable_results()) {
            actionButton.setVisible(true);
            actionButton.setText(Lang.get("finishEditing"));
            actionButton.setOnAction(x -> finishEditingResults());
            try {
                updateResults();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        }else if(event.finished()){
            actionButton.setVisible(true);
            actionButton.setText(Lang.get("filter"));
            actionButton.setGraphic(ImageManager.getImageView(ImageManager.getIcon("filter"),25));
            actionButton.setOnAction(x->{
                resultFilter = resultFilterController.showFilter(resultFilter);
                search();
            });
        }else {
            actionButton.setVisible(false);
        }

        resultTableManager.loadTableView(tableView);
        resultTableManager.refresh();

        if(event.editable_results()&&!event.finished()){
            tableView.getColumns().remove(4);
        }

        searchButton.setGraphic(ImageManager.getImageView(ImageManager.getIcon("search"),20));

        searchButton.setText(Lang.get("search"));
        searchTextField.setPromptText(Lang.get("search"));

        TextResize.resize(actionButton);
        TextResize.resize(searchButton);

        searchButton.setOnAction(x -> search());
        searchTextField.setOnAction(x->search());

    }

    private void updateResults() throws SQLException {

        ResultDAO resultDAO = new ResultDAO(event.id());
        ParticipantDAO participantDAO = new ParticipantDAO(event.id());

        resultDAO.openConnection();
        participantDAO.openConnection();

        for(Participant participant: participantDAO.getAll()){
            if(!resultDAO.participantExists(participant.id())){
                resultDAO.insert(new Result(0,participant.id(),participant.gender(),participant.distance_id(),(short)0,"00:00:00"));
            }
        }

        resultDAO.closeConnection();
        participantDAO.closeConnection();

    }

    private void loadCategories() throws SQLException{
        CategoryDAO categoryDAO = new CategoryDAO(event.id());
        List<short[]> categories = new ArrayList<>();
        try {
            categoryDAO.openConnection();
            for(Category category:categoryDAO.getAll()){
                short[] ages = new short[3];
                ages[0]=category.id();
                ages[1]=category.aboveAge();
                ages[2]=category.belowAge();
                categories.add(ages);
            }
            categoryDAO.closeConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        ResultDAO resultDAO = new ResultDAO(event.id());
        ParticipantDAO participantDAO = new ParticipantDAO(event.id());

        resultDAO.openConnection();
        participantDAO.openConnection();

        for(Participant participant: participantDAO.getAll()){
                short categoryID=0;
                for (short[] ages : categories) {
                    if (participant.age() >= ages[1] && participant.age() < ages[2]) {
                        categoryID = ages[0];
                        break;
                    }
                }
            if(resultDAO.participantExists(participant.id())) {
                int id = resultDAO.getIDFromParticipant(participant.id());
                resultDAO.update(new Result(id, participant.id(), participant.gender(), participant.distance_id(), categoryID,resultDAO.get(id).finish_time()));
            }else{
                resultDAO.insert(new Result(0,participant.id(),participant.gender(),participant.distance_id(),categoryID,"00:00:00"));
            }
        }

        resultDAO.closeConnection();
        participantDAO.closeConnection();
    }

    private void finishEditingResults(){
        DialogController dialogController = new DialogController(StageViewController.getStage());
        dialogController.setContentText(Lang.get("dialogRW1"));
        dialogController.getSecondaryButton().setText(Lang.get("no"));
        dialogController.getPrimaryButton().setText(Lang.get("yes"));
        dialogController.addAction(dialogController.getSecondaryButton(),e-> dialogController.close());
        dialogController.addAction(dialogController.getPrimaryButton(),e->{
            Event event1 = new Event(event.id(), event.name(), event.location(), event.date(),true,false);
            try{
                EventDAO eventDAO = new EventDAO();
                eventDAO.openConnection();
                eventDAO.update(event1);
                eventDAO.closeConnection();
                loadCategories();
            }catch(SQLException ex){
                throw new RuntimeException(ex);
            }
            Objects.requireNonNull(EventTableManager.getInstance(Section.EVENT)).refresh();
            Objects.requireNonNull(EventTableManager.getInstance(Section.HISTORY)).refresh();
            StageViewController.getInstance().setScene(FXMLPaths.EVENT_DETAILED.PATH, new EventsDetailedViewController(event1,Section.HISTORY),Section.HISTORY,Save.NO);
            dialogController.close();
        });

        dialogController.showWarning(Lang.get("warning"));

    }

    private void search(){

        short categoryID = resultFilter.getCategoryID();
        short distanceID = resultFilter.getDistanceID();
        short gender = resultFilter.getGender();
        String name = searchTextField.getText();

        resultTableManager.search(categoryID,distanceID,gender,name);

    }

    private void back(){
        if(section == Section.EVENT) {
            StageViewController.getInstance().setScene(FXMLPaths.EVENT_DETAILED.PATH, new EventsDetailedViewController(event, Section.EVENT), Section.EVENT, Save.NO);
        }else{
            StageViewController.getInstance().setScene(FXMLPaths.EVENT_DETAILED.PATH, new EventsDetailedViewController(event, Section.HISTORY), Section.HISTORY, Save.NO);
        }
    }

}
