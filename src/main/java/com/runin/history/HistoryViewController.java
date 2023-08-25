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

package com.runin.history;

import com.runin.model_managers.EventTableManager;
import com.runin.models.EventTable;
import com.runin.shared.ImageManager;
import com.runin.shared.Lang;
import com.runin.shared.Section;
import com.runin.shared.TextResize;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.controls.legacy.MFXLegacyTableView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

public class HistoryViewController implements Initializable {

    @FXML
    private MFXLegacyTableView<EventTable> tableView;
    @FXML
    private MFXTextField searchTextField;
    @FXML
    private MFXButton backButton,searchButton,actionButton;

    private EventTableManager eventTableManager;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        eventTableManager = new EventTableManager(Section.HISTORY);
        eventTableManager.loadTableView(tableView);
        eventTableManager.refresh();
        eventTableManager.search("",(short)1);

        backButton.setVisible(false);
        actionButton.setVisible(false);

        searchButton.setGraphic(ImageManager.getImageView(ImageManager.getIcon("search"),20));

        searchTextField.setOnAction(x->search());
        searchButton.setOnAction(x->search());

        searchButton.setText(Lang.get("search"));
        searchTextField.setPromptText(Lang.get("search"));

        TextResize.resize(searchButton);

    }

    private void search(){
        eventTableManager.search(searchTextField.getText(),(short)1);
    }


}
