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

import com.runin.db.DistanceDAO;
import com.runin.event.EventsEditViewController;
import com.runin.models.DistanceTable;
import com.runin.record.Distance;
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

public class DistanceTableManager extends TableManager<DistanceTable>{

    private static DistanceTableManager INSTANCE;
    private final int eventId;

    public static DistanceTableManager getInstance(){
        return INSTANCE;
    }

    public DistanceTableManager(int eventId){
        INSTANCE = this;
        this.eventId = eventId;
    }


    public void loadTableView(MFXLegacyTableView<DistanceTable> tableView){

        TableColumn<DistanceTable, String> nameColumn = new TableColumn<>(Lang.get("name"));
        TableColumn<DistanceTable, Double> inKmColumn = new TableColumn<>(Lang.get("distanceInKm"));
        TableColumn<DistanceTable, MFXButton> removeColumn = new TableColumn<>("");

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        inKmColumn.setCellValueFactory(new PropertyValueFactory<>("distanceInKm"));
        removeColumn.setCellValueFactory(new PropertyValueFactory<>("removeButton"));

        nameColumn.setMinWidth(150);
        nameColumn.setMaxWidth(Double.MAX_VALUE);
        inKmColumn.setMinWidth(100);
        removeColumn.setMinWidth(100);

        nameColumn.setResizable(true);
        inKmColumn.setResizable(false);
        removeColumn.setResizable(false);

        nameColumn.setReorderable(false);
        inKmColumn.setReorderable(false);
        removeColumn.setReorderable(false);

        nameColumn.setSortable(false);
        inKmColumn.setSortable(false);
        removeColumn.setSortable(false);

        tableView.getColumns().add(nameColumn);
        tableView.getColumns().add(inKmColumn);
        tableView.getColumns().add(removeColumn);

        tableView.itemsProperty().set(getFilteredList());

        tableView.setPlaceholder(new Label(Lang.get("tableD1")));
        tableView.setEditable(false);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setFixedCellSize(40);

        tableView.setRowFactory(x->{
            MFXLegacyTableRow<DistanceTable> tableRow = new MFXLegacyTableRow<>();
            tableRow.setOnMouseClicked(mouseEvent -> {
                if(!tableRow.isEmpty()&&mouseEvent.getButton()== MouseButton.PRIMARY&&mouseEvent.getClickCount()==2){
                    EventsEditViewController eventsEditViewController = EventsEditViewController.getInstance();
                    if(eventsEditViewController!=null){
                        eventsEditViewController.editDistance();
                    }
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
    }

    @Override
    public void refresh() {
        clear();
        DistanceDAO distanceDAO = new DistanceDAO(eventId);
        try{
            distanceDAO.openConnection();
            for(Distance d:distanceDAO.getAll()){
                add(new DistanceTable(eventId, d));
            }
            distanceDAO.closeConnection();
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    public void search(String searchText){
        getFilteredList().setPredicate(x->{
            boolean predicate;
            predicate = searchText.isEmpty() || x.getName().toLowerCase().contains(searchText);
            return predicate;
        });
    }

}
