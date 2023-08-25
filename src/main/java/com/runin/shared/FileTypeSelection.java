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
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.stage.StageStyle;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ResourceBundle;

public class FileTypeSelection implements Initializable {

    @FXML
    private MFXButton imageButton, documentButton, folderButton;

    private final Object object;
    private final PopupStageViewController popupStageViewController;

    public FileTypeSelection(Object object){
        this.object = object;
        popupStageViewController = new PopupStageViewController();
        popupStageViewController.loadScene(FXMLPaths.FILE_TYPE_SELECTION.PATH, this);
        popupStageViewController.getStage().initStyle(StageStyle.TRANSPARENT);
        StageViewController.getInstance().setShadow(true);
        popupStageViewController.show();
    }

    @FXML
    private void uploadImage(){
        try {
            object.getClass().getMethod("uploadImage").invoke(object);
            close();
        }catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e){
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void uploadDocument(){
        try {
            object.getClass().getMethod("uploadDocument").invoke(object);
            close();
        }catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e){
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void makeNewFolder(){
        try {
            object.getClass().getMethod("makeNewFolder").invoke(object);
            close();
        }catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e){
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void close(){
        StageViewController.getInstance().setShadow(false);
        popupStageViewController.close();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        imageButton.setGraphic(ImageManager.getImageView(ImageManager.getIcon("photo"),50));
        documentButton.setGraphic(ImageManager.getImageView(ImageManager.getIcon("file"),50));
        folderButton.setGraphic(ImageManager.getImageView(ImageManager.getIcon("folder-add"),50));

    }
}
