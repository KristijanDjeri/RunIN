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

import io.github.palexdev.materialfx.controls.legacy.MFXLegacyTableView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

public abstract class TableManager<T>{

    private final ObservableList<T> observableList = FXCollections.observableArrayList();
    private final FilteredList<T> filteredList = new FilteredList<>(getObservableList(),p->true);

    protected ObservableList<T> getObservableList(){
        return observableList;
    }
    protected FilteredList<T> getFilteredList(){
        return filteredList;
    }
    public abstract void loadTableView(MFXLegacyTableView<T> tableView);
    public abstract void refresh();
    public void add(T tableModel){
        observableList.add(tableModel);
    }
    public void remove(T tableModel){
        observableList.remove(tableModel);
    }
    public void set(int index, T tableModel){
        observableList.set(index,tableModel);
    }
    public void clear(){
        observableList.clear();
        filteredList.clear();
    }

}
