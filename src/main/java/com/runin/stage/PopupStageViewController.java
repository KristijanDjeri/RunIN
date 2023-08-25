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

package com.runin.stage;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class PopupStageViewController {

    private final Stage stage;

    public PopupStageViewController(){
        stage = new Stage();
        stage.initOwner(StageViewController.getStage());
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
    }

    public void loadScene(String location,Object controller){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/runin/"+location));
            fxmlLoader.setController(controller);
            Scene scene = new Scene(fxmlLoader.load());
            setScene(scene);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public Stage getStage(){
        return stage;
    }

    public void setTitle(String title){
        stage.setTitle(title);
    }

    public void setScene(Scene scene){
        stage.setScene(scene);
    }

    public void show(){
        stage.show();
    }

    @SuppressWarnings("unused")
    public void hide(){
        stage.hide();
    }

    public void close(){
        stage.close();
    }

}
