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

import com.runin.db.CategoryDAO;
import com.runin.event.EventsEditViewController;
import com.runin.models.CategoryTable;
import com.runin.record.Category;
import com.runin.shared.Lang;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.legacy.MFXLegacyTableRow;
import io.github.palexdev.materialfx.controls.legacy.MFXLegacyTableView;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;

import java.sql.SQLException;

public class CategoryTableManager extends TableManager<CategoryTable> {

    private static CategoryTableManager INSTANCE;
    private final int eventId;

    public static CategoryTableManager getInstance() {
        return INSTANCE;
    }

    public CategoryTableManager(int eventId) {
        INSTANCE = this;
        this.eventId = eventId;
    }

    public void search(String searchText){
        getFilteredList().setPredicate(x->{
            boolean predicate;
            predicate = searchText.isEmpty() || x.getName().toLowerCase().contains(searchText);
            return predicate;
        });
    }

    @Override
    public void loadTableView(MFXLegacyTableView<CategoryTable> tableView) {

        TableColumn<CategoryTable, String> nameColumn = new TableColumn<>(Lang.get("name"));
        TableColumn<CategoryTable, Integer> aboveAgeColumn = new TableColumn<>(Lang.get("aboveAge"));
        TableColumn<CategoryTable, Integer> belowAgeColumn = new TableColumn<>(Lang.get("belowAge"));
        TableColumn<CategoryTable, MFXButton> removeColumn = new TableColumn<>("");

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        aboveAgeColumn.setCellValueFactory(new PropertyValueFactory<>("aboveAge"));
        belowAgeColumn.setCellValueFactory(new PropertyValueFactory<>("belowAge"));
        removeColumn.setCellValueFactory(new PropertyValueFactory<>("removeButton"));

        nameColumn.setMinWidth(150);
        nameColumn.setMaxWidth(Double.MAX_VALUE);
        aboveAgeColumn.setMinWidth(100);
        belowAgeColumn.setMinWidth(100);
        removeColumn.setMinWidth(100);

        nameColumn.setResizable(true);
        aboveAgeColumn.setResizable(false);
        belowAgeColumn.setResizable(false);
        removeColumn.setResizable(false);

        nameColumn.setReorderable(false);
        aboveAgeColumn.setReorderable(false);
        belowAgeColumn.setReorderable(false);
        removeColumn.setReorderable(false);

        nameColumn.setSortable(false);
        aboveAgeColumn.setSortable(false);
        belowAgeColumn.setSortable(false);
        removeColumn.setSortable(false);

        tableView.getColumns().add(nameColumn);
        tableView.getColumns().add(aboveAgeColumn);
        tableView.getColumns().add(belowAgeColumn);
        tableView.getColumns().add(removeColumn);

        tableView.itemsProperty().set(getFilteredList());

        tableView.setPlaceholder(new Label(Lang.get("tableC1")));
        tableView.setEditable(false);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setFixedCellSize(40);

        tableView.setRowFactory(x -> {
            MFXLegacyTableRow<CategoryTable> tableRow = new MFXLegacyTableRow<>();
            tableRow.setOnMouseClicked(mouseEvent -> {
                if (!tableRow.isEmpty() && mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2) {
                    EventsEditViewController eventsEditViewController = EventsEditViewController.getInstance();
                    if (eventsEditViewController != null) {
                        eventsEditViewController.editCategory();
                    }
                }
            });
            tableRow.setOnMouseEntered(mouseEvent -> {
                if (!tableRow.isEmpty()) {
                    tableRow.getItem().getRemoveButton().setVisible(true);
                }
            });
            tableRow.setOnMouseExited(mouseEvent -> {
                if (!tableRow.isEmpty()) {
                    tableRow.getItem().getRemoveButton().setVisible(false);
                }
            });
            return tableRow;
        });
    }

    @Override
    public void refresh() {
        clear();
        CategoryDAO categoryDAO = new CategoryDAO(eventId);
        try{
            categoryDAO.openConnection();
            for(Category c: categoryDAO.getAll()){
                add(new CategoryTable(eventId,c));
            }
            categoryDAO.closeConnection();
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
    }
}
