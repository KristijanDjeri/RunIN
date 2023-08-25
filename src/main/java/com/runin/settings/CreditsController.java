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

package com.runin.settings;

import com.runin.shared.FXMLPaths;
import com.runin.shared.Lang;
import com.runin.stage.PopupStageViewController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.net.URL;
import java.util.ResourceBundle;

public class CreditsController implements Initializable {

    @FXML
    private VBox creditsBox;

    public CreditsController(){
        PopupStageViewController popupStageViewController = new PopupStageViewController();
        popupStageViewController.setTitle(Lang.get("credits"));
        popupStageViewController.loadScene(FXMLPaths.CREDITS.PATH, this);
        popupStageViewController.show();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        String[] credits = {
                "Application Credits",
                "Application Name: RunIN",
                "Developed by: Kristijan Đeri",
                "   -Programming & Logic: Kristijan Đeri",
                "   -GUI Design: Kristijan Đeri",
                "   -Testing: Kristijan Đeri",
                "Technology Used:",
                "Language: Java",
                "   -GUI Framework: JavaFX",
                "   -Database: H2 Database",
                "   -Libraries: MaterialFX v11.13.5 by palexdev\n(I adapted the style to the application)",
                "Special Thanks:",
                """
I wanna thank me for believing in me
I wanna thank me for doing all this hard work
I wanna thank me for having no days off
I wanna thank me for never quitting
I wanna thank me for always being a giver
And tryna give more than I receive""",
                "License: GNU General Public License v3.0 (GPLv3)",
                "Copyright: © 2023 Kristijan Đeri. All rights reserved.",
                "The application was translated using Google Translate.",
                "PS. credits are not translated because translation\n is not accurate enough!"
        };

        for(String credit:credits){
            Text text = new Text(credit);
            text.setStyle("-fx-max-width:400px;-fx-max-height:1000px;-fx-font-size:19;");
            text.setTextAlignment(TextAlignment.CENTER);
            creditsBox.getChildren().add(text);
        }

        creditsBox.setSpacing(10);
        creditsBox.getChildren().get(6).setStyle(creditsBox.getChildren().get(11).getStyle()+"-fx-font-weight:bold;-fx-font-size:22;");
        creditsBox.getChildren().get(0).setStyle(creditsBox.getChildren().get(0).getStyle()+"-fx-font-weight:bold;-fx-underline:true;-fx-font-size:25;");
        creditsBox.getChildren().get(11).setStyle(creditsBox.getChildren().get(11).getStyle()+"-fx-font-weight:bold;-fx-font-size:22;");
        creditsBox.getChildren().get(12).setStyle(creditsBox.getChildren().get(12).getStyle()+"-fx-font-weight:bold;");
        ((Text)creditsBox.getChildren().get(12)).setTextAlignment(TextAlignment.LEFT);
        creditsBox.getChildren().get(16).setStyle(creditsBox.getChildren().get(15).getStyle()+"-fx-font-style:italic;");

    }

}
