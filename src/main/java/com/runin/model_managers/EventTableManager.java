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

import com.runin.db.EventDAO;
import com.runin.event.EventsDetailedViewController;
import com.runin.models.EventTable;
import com.runin.record.Event;
import com.runin.shared.FXMLPaths;
import com.runin.shared.Lang;
import com.runin.shared.Save;
import com.runin.shared.Section;
import com.runin.stage.StageViewController;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.legacy.MFXLegacyTableRow;
import io.github.palexdev.materialfx.controls.legacy.MFXLegacyTableView;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import java.sql.SQLException;

public class EventTableManager extends TableManager<EventTable>{

    private static EventTableManager INSTANCE_EVENT, INSTANCE_HISTORY;
    private final Section section;

    public static EventTableManager getInstance(Section section){
        switch (section){
            case EVENT -> {
                if(INSTANCE_EVENT==null){
                    INSTANCE_EVENT = new EventTableManager(Section.EVENT);
                }
                return INSTANCE_EVENT;
            }
            case HISTORY -> {
                if(INSTANCE_HISTORY==null){
                    INSTANCE_HISTORY = new EventTableManager(Section.HISTORY);
                }
                return INSTANCE_HISTORY;
            }
        }
        return null;
    }

    public EventTableManager(Section section){
        this.section = section;
        if(section==Section.EVENT){
            INSTANCE_EVENT=this;
        }else if(section==Section.HISTORY){
            INSTANCE_HISTORY=this;
        }
    }

    public void loadTableView(MFXLegacyTableView<EventTable> tableView){

        TableColumn<EventTable, String> nameColumn = new TableColumn<>(Lang.get("name"));
        TableColumn<EventTable, Integer> locationColumn = new TableColumn<>(Lang.get("location"));
        TableColumn<EventTable, String> dateColumn = new TableColumn<>(Lang.get("date"));
        TableColumn<EventTable, MFXButton> removeColumn = new TableColumn<>("");

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        removeColumn.setCellValueFactory(new PropertyValueFactory<>("removeButton"));

        nameColumn.setMinWidth(250);
        nameColumn.setMaxWidth(Double.MAX_VALUE);
        locationColumn.setMinWidth(100);
        dateColumn.setMinWidth(150);
        removeColumn.setMinWidth(100);

        nameColumn.setResizable(true);
        locationColumn.setResizable(false);
        dateColumn.setResizable(false);
        removeColumn.setResizable(false);

        nameColumn.setReorderable(false);
        locationColumn.setReorderable(false);
        dateColumn.setReorderable(false);
        removeColumn.setReorderable(false);

        nameColumn.setSortable(false);
        locationColumn.setSortable(false);
        dateColumn.setSortable(false);
        removeColumn.setSortable(false);

        tableView.getColumns().add(nameColumn);
        tableView.getColumns().add(locationColumn);
        tableView.getColumns().add(dateColumn);
        tableView.getColumns().add(removeColumn);

        tableView.itemsProperty().set(getFilteredList());

        tableView.setPlaceholder(new Label(Lang.get("tableE1")));
        tableView.setEditable(false);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setFixedCellSize(40);

        tableView.setRowFactory(x->{
            MFXLegacyTableRow<EventTable> tableRow = new MFXLegacyTableRow<>();

            tableRow.setOnMouseClicked(mouseEvent -> {
                if(!tableRow.isEmpty()&&mouseEvent.getButton()== MouseButton.PRIMARY&&mouseEvent.getClickCount()>1) {
                    EventsDetailedViewController eventsDetailedViewController = new EventsDetailedViewController(tableRow.getItem().getEvent(), section);
                    StageViewController.getInstance().setScene(FXMLPaths.EVENT_DETAILED.PATH, eventsDetailedViewController, section, Save.NO);
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
        EventDAO eventDAO = new EventDAO();
        try{
            eventDAO.openConnection();
            for(Event e: eventDAO.getAll()){
                add(new EventTable(e,section));
            }
            eventDAO.closeConnection();
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    public void search(String searchText,short finished){
        getFilteredList().setPredicate(event->{
            boolean predicate = searchText.isEmpty() || event.getName().toLowerCase().contains(searchText.toLowerCase());
            if(!predicate){
                predicate = event.getLocation().toLowerCase().contains(searchText.toLowerCase());
            }

            if(predicate){
                switch(finished){
                    case 1 -> predicate = event.getEvent().finished();
                    case 2 -> predicate = !event.getEvent().finished();
                }
            }

            return predicate;
        });
    }

}
