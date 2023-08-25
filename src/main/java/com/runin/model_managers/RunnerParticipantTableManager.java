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

import com.runin.models.RunnerParticipantTable;
import com.runin.shared.Lang;
import io.github.palexdev.materialfx.controls.MFXCheckbox;
import io.github.palexdev.materialfx.controls.legacy.MFXLegacyTableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class RunnerParticipantTableManager extends TableManager<RunnerParticipantTable> {


    @Override
    public void loadTableView(MFXLegacyTableView<RunnerParticipantTable> tableView) {

        TableColumn<RunnerParticipantTable, MFXCheckbox> checkColumn = new TableColumn<>("");
        TableColumn<RunnerParticipantTable, String> nameColumn = new TableColumn<>(Lang.get("name"));
        TableColumn<RunnerParticipantTable, String> distanceColumn = new TableColumn<>(Lang.get("distance"));

        checkColumn.setCellValueFactory(new PropertyValueFactory<>("checkBox"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        distanceColumn.setCellValueFactory(new PropertyValueFactory<>("distance"));

        checkColumn.setMinWidth(70);
        nameColumn.setMinWidth(200);
        nameColumn.setMaxWidth(Double.MAX_VALUE);
        distanceColumn.setMinWidth(100);

        checkColumn.setResizable(false);
        nameColumn.setResizable(true);
        distanceColumn.setResizable(false);

        checkColumn.setReorderable(false);
        nameColumn.setReorderable(false);
        distanceColumn.setReorderable(false);

        checkColumn.setSortable(false);
        nameColumn.setSortable(false);
        distanceColumn.setSortable(false);

        tableView.getColumns().add(checkColumn);
        tableView.getColumns().add(nameColumn);
        tableView.getColumns().add(distanceColumn);

        tableView.itemsProperty().set(getFilteredList());

        tableView.setEditable(false);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setFixedCellSize(30);
        tableView.setMaxSize(Double.MAX_VALUE,Double.MAX_VALUE);

    }

    @Override
    public void refresh() {

    }
}
