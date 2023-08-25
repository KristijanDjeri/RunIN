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
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.stage.StageStyle;

import java.net.URL;
import java.util.ResourceBundle;

public class TextInputController implements Initializable {

    @FXML
    private MFXTextField textField;
    @FXML
    private Label headerText;
    @FXML
    private MFXButton enterButton;
    private final StringBuilder string;
    private final String ht,ft;
    private final PopupStageViewController popupStageViewController;

    public TextInputController(String headerText, String floatingText, StringBuilder string){
        this.string = string;
        ht = headerText;
        ft = floatingText;
        popupStageViewController = new PopupStageViewController();
        popupStageViewController.loadScene(FXMLPaths.TEXT_INPUT.PATH, this);
        popupStageViewController.getStage().initStyle(StageStyle.TRANSPARENT);
        popupStageViewController.getStage().showAndWait();
    }

    @FXML
    private void enter(){
        string.delete(0,string.length());
        string.append(textField.getText());
        popupStageViewController.close();
    }

    @FXML
    private void close(){
        popupStageViewController.close();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        textField.setFloatingText(ft);
        headerText.setText(ht);
        textField.setOnAction(x->enter());

        enterButton.setText(Lang.get("enter"));

    }

}
