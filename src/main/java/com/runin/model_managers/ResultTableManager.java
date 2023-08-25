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

package com.runin.model_managers;

import com.runin.db.ResultDAO;
import com.runin.models.ResultTable;
import com.runin.record.Event;
import com.runin.record.Result;
import com.runin.shared.DialogController;
import com.runin.shared.Lang;
import com.runin.stage.StageViewController;
import io.github.palexdev.materialfx.controls.legacy.MFXLegacyTableRow;
import io.github.palexdev.materialfx.controls.legacy.MFXLegacyTableView;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;
import java.sql.SQLException;
import java.util.List;

 public class ResultTableManager extends TableManager<ResultTable>{

    private static ResultTableManager INSTANCE;
    private final Event event;


    public static ResultTableManager getInstance(){
        return INSTANCE;
    }

    public ResultTableManager(Event event){
        INSTANCE = this;
        this.event = event;
    }

    public void loadTableView(MFXLegacyTableView<ResultTable> tableView){

        TableColumn<ResultTable,Integer> positionColumn = new TableColumn<>(Lang.get("position"));
        TableColumn<ResultTable,Integer> nameColumn = new TableColumn<>(Lang.get("name"));
        TableColumn<ResultTable,String> genderColumn = new TableColumn<>(Lang.get("gender"));
        TableColumn<ResultTable,String> distanceColumn = new TableColumn<>(Lang.get("distance"));
        TableColumn<ResultTable,String> categoryColumn = new TableColumn<>(Lang.get("category"));
        TableColumn<ResultTable,String> timeColumn = new TableColumn<>(Lang.get("time"));

        positionColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(getFilteredList().indexOf(cellData.getValue())+1));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("participantName"));
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        distanceColumn.setCellValueFactory(new PropertyValueFactory<>("distance"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("finishTime"));

        if(event.editable_results()) {

            timeColumn.setCellFactory(tc->{
                TextFieldTableCell<ResultTable,String> cell = new TextFieldTableCell<>();
                StringConverter<String> converter = new StringConverter<>() {
                    @Override
                    public String toString(String time) {
                        return time;
                    }

                    @Override
                    public String fromString(String string) {
                        return checkTime(cell,string);
                    }
                };

                cell.setId("editableTableCell");
                cell.setConverter(converter);
                cell.setEditable(true);
                cell.setOnMouseClicked(x->cell.startEdit());
                return cell;
            });

            timeColumn.setOnEditCommit(e -> e.getTableView().getItems().get(e.getTablePosition().getRow()).setFinishTime(e.getNewValue()));
            timeColumn.setEditable(true);
        }else if(event.finished()){
            tableView.setRowFactory(x->{
                MFXLegacyTableRow<ResultTable> tableRow = new MFXLegacyTableRow<>();
                tableRow.indexProperty().addListener((obs, oldIndex, newIndex) -> {
                    if(tableRow.getItem()!=null&&!tableRow.getItem().getFinishTime().equals("DNF")) {
                        switch (newIndex.intValue()) {
                            case 0 -> tableRow.setId("gold");
                            case 1 -> tableRow.setId("silver");
                            case 2 -> tableRow.setId("bronze");
                            default -> tableRow.setId("");
                        }
                    }
                });
                return tableRow;
            });
        }

        positionColumn.setMinWidth(50);
        nameColumn.setMinWidth(200);
        nameColumn.setMaxWidth(Double.MAX_VALUE);
        genderColumn.setMinWidth(70);
        distanceColumn.setMinWidth(120);
        categoryColumn.setMinWidth(120);
        timeColumn.setMinWidth(100);

        positionColumn.setResizable(false);
        nameColumn.setResizable(true);
        genderColumn.setResizable(false);
        distanceColumn.setResizable(false);
        categoryColumn.setResizable(false);
        timeColumn.setResizable(false);

        positionColumn.setReorderable(true);
        nameColumn.setReorderable(true);
        genderColumn.setReorderable(true);
        distanceColumn.setReorderable(true);
        categoryColumn.setReorderable(true);
        timeColumn.setReorderable(true);

        positionColumn.setSortable(true);
        nameColumn.setSortable(false);
        genderColumn.setSortable(false);
        distanceColumn.setSortable(false);
        categoryColumn.setSortable(false);
        timeColumn.setSortable(true);

        tableView.setPlaceholder(new Label(Lang.get("tableR2")));
        tableView.setEditable(true);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setFixedCellSize(40);
        tableView.autosize();

        tableView.getColumns().add(positionColumn);
        tableView.getColumns().add(nameColumn);
        tableView.getColumns().add(genderColumn);
        tableView.getColumns().add(distanceColumn);
        tableView.getColumns().add(categoryColumn);
        tableView.getColumns().add(timeColumn);

        tableView.itemsProperty().set(getFilteredList());

    }

    private String checkTime(TextFieldTableCell<ResultTable,String> cell,String string){
        if(string.matches("([01]?[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]")){
            ResultDAO resultDAO = new ResultDAO(event.id());
            try {
                Result result = cell.getTableRow().getItem().getResult();
                resultDAO.openConnection();
                resultDAO.update(new Result(result.id(),result.participant_id(),result.gender(),result.distance_id(),result.category_id(),string));
                resultDAO.closeConnection();
            }catch (SQLException e){
                throw new RuntimeException(e);
            }
            return string;
        }else{
            DialogController dialogController = new DialogController(StageViewController.getStage());
            dialogController.setContentText(Lang.get("dialogRTM1"));
            dialogController.showWarning(Lang.get("invalidInput"));
            return cell.getTableRow().getItem().getFinishTime();
        }
    }

    @Override
    public void refresh() {
        clear();
        ResultDAO resultDAO = new ResultDAO(event.id());
        try{
            resultDAO.openConnection();
            List<Result> resultList = resultDAO.getAll();
            resultList.sort((r1,r2)->{
                if(r1.finish_time().equals("DNF")||r1.finish_time().equals("00:00:00")){
                    return 1;
                }
                if(r2.finish_time().equals("DNF")||r2.finish_time().equals("00:00:00")){
                    return -1;
                }
                return r1.finish_time().compareTo(r2.finish_time());
            });
            for(Result result: resultList){
                add(new ResultTable(event,result));
            }
            resultDAO.closeConnection();
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    public void search(short categoryID, short distanceID, short gender, String name){
        getFilteredList().setPredicate(x-> {
            boolean predicate=true;

            if(categoryID!=0){
                predicate = categoryID == x.getResult().category_id();
            }

            if(predicate){
                if(distanceID!=0){
                    predicate = distanceID == x.getResult().distance_id();
                }
            }

            if(predicate){
                switch (gender) {
                    case 1 -> predicate = x.getResult().gender();
                    case 2 -> predicate = !x.getResult().gender();
                }
            }

            if(predicate && !name.isEmpty()){
                predicate = x.getParticipantName().contains(name);
            }
            return predicate;
        });
    }

}
