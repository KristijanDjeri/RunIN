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

import com.runin.db.RunnerDAO;
import com.runin.models.RunnerTable;
import com.runin.record.Runner;
import com.runin.runner.RunnersProfileViewController;
import com.runin.shared.FXMLPaths;
import com.runin.shared.Lang;
import com.runin.shared.Save;
import com.runin.shared.Section;
import com.runin.stage.StageViewController;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXCheckbox;
import io.github.palexdev.materialfx.controls.legacy.MFXLegacyTableRow;
import io.github.palexdev.materialfx.controls.legacy.MFXLegacyTableView;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import java.sql.SQLException;

public class RunnerTableManager extends TableManager<RunnerTable>{

    private static RunnerTableManager INSTANCE;

    public static RunnerTableManager getInstance(){
        if(INSTANCE == null){
            INSTANCE = new RunnerTableManager();
        }
        return INSTANCE;
    }

    public void loadTableView(MFXLegacyTableView<RunnerTable> tableView){

        TableColumn<RunnerTable, MFXCheckbox> checkBoxColumn = new TableColumn<>("");
        TableColumn<RunnerTable, String> nameColumn = new TableColumn<>(Lang.get("name"));
        TableColumn<RunnerTable, Integer> ageColumn = new TableColumn<>(Lang.get("age"));
        TableColumn<RunnerTable, String> genderColumn = new TableColumn<>(Lang.get("gender"));
        TableColumn<RunnerTable, MFXButton> removeColumn = new TableColumn<>("");

        checkBoxColumn.setCellValueFactory(new PropertyValueFactory<>("checkBox"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        ageColumn.setCellValueFactory(new PropertyValueFactory<>("age"));
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        removeColumn.setCellValueFactory(new PropertyValueFactory<>("removeButton"));

        checkBoxColumn.setMinWidth(100);
        nameColumn.setMinWidth(250);
        nameColumn.setMaxWidth(Double.MAX_VALUE);
        ageColumn.setMinWidth(100);
        genderColumn.setMinWidth(150);
        removeColumn.setMinWidth(100);

        checkBoxColumn.setResizable(false);
        nameColumn.setResizable(true);
        ageColumn.setResizable(false);
        genderColumn.setResizable(false);
        removeColumn.setResizable(false);

        checkBoxColumn.setReorderable(false);
        nameColumn.setReorderable(false);
        ageColumn.setReorderable(false);
        genderColumn.setReorderable(false);
        removeColumn.setReorderable(false);

        checkBoxColumn.setSortable(false);
        nameColumn.setSortable(false);
        ageColumn.setSortable(false);
        genderColumn.setSortable(false);
        removeColumn.setSortable(false);

        tableView.getColumns().add(checkBoxColumn);
        tableView.getColumns().add(nameColumn);
        tableView.getColumns().add(ageColumn);
        tableView.getColumns().add(genderColumn);
        tableView.getColumns().add(removeColumn);

        tableView.itemsProperty().set(getFilteredList());

        tableView.setPlaceholder(new Label(Lang.get("tableR1")));
        tableView.setEditable(false);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setFixedCellSize(40);
        tableView.setMaxSize(Double.MAX_VALUE,Double.MAX_VALUE);

        tableView.setRowFactory(x->{
            MFXLegacyTableRow<RunnerTable> tableRow = new MFXLegacyTableRow<>();
            tableRow.setOnMouseClicked(mouseEvent -> {
                if (!tableRow.isEmpty()&&mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2) {
                    RunnersProfileViewController runnersProfileViewController = new RunnersProfileViewController(tableRow.getItem().getRunner(),null,Section.RUNNER);
                    StageViewController.getInstance().setScene(FXMLPaths.RUNNER_PROFILE.PATH, runnersProfileViewController, Section.RUNNER, Save.NO);
                }
            });
            tableRow.setOnMouseEntered(mouseEvent -> {
                if (!tableRow.isEmpty()) {
                    tableRow.getItem();
                    if(!tableRow.getItem().getChecked().get()) {
                        tableRow.getItem().getRemoveButton().setVisible(true);
                    }
                    tableRow.getItem().getCheckBox().setVisible(true);
                }
            });
            tableRow.setOnMouseExited(mouseEvent -> {
                if (!tableRow.isEmpty()) {
                    tableRow.getItem().getRemoveButton().setVisible(false);
                    if(!tableRow.getItem().getChecked().get()) {
                        tableRow.getItem().getCheckBox().setVisible(false);
                    }
                }
            });
            return tableRow;
        });
    }

    public ObservableList<RunnerTable> getSelectedRunners(){
        return RunnerTable.selectedRunners;
    }

    public void unselectAll(){
        RunnerTable.unselectAll();
    }

    @Override
    public void refresh() {
        clear();
        RunnerDAO runnerDAO = new RunnerDAO();
        try{
            runnerDAO.openConnection();
            for(Runner runner: runnerDAO.getAll()){
                add(new RunnerTable(runner));
            }
            runnerDAO.closeConnection();
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    public void search(String searchText) {
        getFilteredList().setPredicate(runner->{
            boolean b;
            b = searchText.isEmpty() || runner.getName().toLowerCase().contains(searchText.toLowerCase());
            return b;
        });
    }
}
