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

package com.runin.shared;

import com.runin.stage.PopupStageViewController;
import com.runin.stage.StageViewController;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.stage.StageStyle;
import java.net.URL;
import java.util.ResourceBundle;

public class DatabaseChooserController implements Initializable {

    @FXML
    private MFXButton runnersButton, eventsButton, bothButton;

    private final PopupStageViewController popupStageViewController;
    private final SimpleIntegerProperty type;

    public DatabaseChooserController(SimpleIntegerProperty type){
        this.type = type;
        popupStageViewController = new PopupStageViewController();
        popupStageViewController.loadScene(FXMLPaths.DATABASE_CHOOSER.PATH, this);
        popupStageViewController.getStage().initStyle(StageStyle.TRANSPARENT);
        StageViewController.getInstance().setShadow(true);
        popupStageViewController.getStage().showAndWait();
    }

    @FXML
    private void runners(){
        type.set(1);
        close();
    }

    @FXML
    private void everything(){
        type.set(0);
        close();
    }

    @FXML
    private void events(){
        type.set(2);
        close();
    }

    @FXML
    private void close(){
        StageViewController.getInstance().setShadow(false);
        popupStageViewController.close();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        runnersButton.setGraphic(ImageManager.getImageView(ImageManager.getIcon("users"),50));
        eventsButton.setGraphic(ImageManager.getImageView(ImageManager.getIcon("calender"),50));
        bothButton.setGraphic(ImageManager.getImageView(ImageManager.getIcon("box"),50));

        runnersButton.setText(Lang.get("runners"));
        bothButton.setText(Lang.get("both"));
        eventsButton.setText(Lang.get("events"));

        TextResize.resize(runnersButton);
        TextResize.resize(bothButton);
        TextResize.resize(eventsButton);

    }

}
