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

import com.runin.db.ParticipantDAO;
import com.runin.models.ParticipantTable;
import com.runin.record.Event;
import com.runin.record.Participant;
import com.runin.shared.Lang;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.legacy.MFXLegacyTableRow;
import io.github.palexdev.materialfx.controls.legacy.MFXLegacyTableView;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;

public class ParticipantTableManager extends TableManager<ParticipantTable>{

    private static ParticipantTableManager INSTANCE;
    private final Event event;

    public static ParticipantTableManager getInstance(){
        return INSTANCE;
    }

    public ParticipantTableManager(Event event){
        INSTANCE = this;
        this.event = event;
    }

    public void loadTableView(MFXLegacyTableView<ParticipantTable> tableView){
        TableColumn<ParticipantTable,Integer> numberColumn = new TableColumn<>(Lang.get("number"));
        TableColumn<ParticipantTable,String> nameColumn = new TableColumn<>(Lang.get("name"));
        TableColumn<ParticipantTable,Double> distanceColumn = new TableColumn<>(Lang.get("distanceInKm"));
        TableColumn<ParticipantTable, MFXButton> removeColumn = new TableColumn<>("");

        numberColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        distanceColumn.setCellValueFactory(new PropertyValueFactory<>("distance"));
        removeColumn.setCellValueFactory(new PropertyValueFactory<>("removeButton"));

        numberColumn.setMinWidth(150);
        nameColumn.setMinWidth(250);
        nameColumn.setMaxWidth(Double.MAX_VALUE);
        distanceColumn.setMinWidth(150);
        removeColumn.setMinWidth(100);

        numberColumn.setResizable(false);
        nameColumn.setResizable(true);
        distanceColumn.setResizable(false);
        removeColumn.setResizable(false);

        numberColumn.setReorderable(false);
        nameColumn.setReorderable(false);
        distanceColumn.setReorderable(false);
        removeColumn.setReorderable(false);

        numberColumn.setSortable(false);
        nameColumn.setSortable(false);
        distanceColumn.setSortable(false);
        removeColumn.setSortable(false);

        tableView.getColumns().add(numberColumn);
        tableView.getColumns().add(nameColumn);
        tableView.getColumns().add(distanceColumn);
        tableView.getColumns().add(removeColumn);

        tableView.itemsProperty().set(getFilteredList());

        tableView.setPlaceholder(new Label(Lang.get("tableP1")));
        tableView.setEditable(false);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setFixedCellSize(40);

        tableView.setRowFactory(x->{
            MFXLegacyTableRow<ParticipantTable> tableRow = new MFXLegacyTableRow<>();
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
        ParticipantDAO participantDAO = new ParticipantDAO(event.id());
        try{
            participantDAO.openConnection();
            for(Participant p:participantDAO.getAll()){
                add(new ParticipantTable(event.id(), p));
            }
            participantDAO.closeConnection();
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    public void search(String searchText){
        getFilteredList().setPredicate(x->{
            boolean predicate;
            predicate = searchText.isEmpty() || x.getName().toLowerCase().contains(searchText.toLowerCase());
            return predicate;
        });
    }

}
