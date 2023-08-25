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

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.dialogs.MFXGenericDialog;
import io.github.palexdev.materialfx.dialogs.MFXGenericDialogBuilder;
import io.github.palexdev.materialfx.dialogs.MFXStageDialog;
import io.github.palexdev.materialfx.effects.DepthLevel;
import io.github.palexdev.materialfx.enums.ButtonType;
import io.github.palexdev.materialfx.font.MFXFontIcon;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.Map;

public class DialogController {

    private final MFXGenericDialog dialogContent;
    private final MFXStageDialog dialog;
    private final MFXButton primaryButton, secondaryButton;

    public DialogController(Stage stage){
        this.dialogContent = MFXGenericDialogBuilder.build()
                .setShowAlwaysOnTop(false)
                .get();
        this.dialog = MFXGenericDialogBuilder.build(dialogContent)
                .toStageDialogBuilder()
                .initOwner(stage)
                .initModality(Modality.APPLICATION_MODAL)
                .setDraggable(true)
                .setAlwaysOnTop(true)
                .get();

        dialogContent.setMaxSize(400, 200);

        secondaryButton = new MFXButton();
        secondaryButton.setPrefWidth(100);
        secondaryButton.setPrefHeight(50);
        secondaryButton.setButtonType(ButtonType.FLAT);
        secondaryButton.setDepthLevel(DepthLevel.LEVEL2);
        secondaryButton.setStyle("-fx-border-color:orange;-fx-border-width:2px;-fx-border-radius:5pt;");

        primaryButton = new MFXButton();
        primaryButton.setPrefWidth(100);
        primaryButton.setPrefHeight(50);
        primaryButton.setButtonType(ButtonType.RAISED);
        primaryButton.setDepthLevel(DepthLevel.LEVEL3);
        primaryButton.setStyle("-fx-background-color:linear-gradient(to top right, #ffca69, orange);-fx-background-radius:5pt;");

    }

    public MFXStageDialog getDialog(){
        return dialog;
    }

    public MFXGenericDialog getDialogContent(){
        return dialogContent;
    }

    public void close(){
        getDialog().close();
    }

    public void addAction(MFXButton button, EventHandler<MouseEvent> mouseEvent){
        getDialogContent().addActions(Map.entry(button, mouseEvent));
    }

    public MFXButton getPrimaryButton(){
        return primaryButton;
    }

    public MFXButton getSecondaryButton(){
        return secondaryButton;
    }

    public void setContentText(String contentText){
        dialogContent.setContentText(contentText);
    }

    public void setTitle(String title){
        dialog.setTitle(title);
    }

    @FXML
    public void showInfo(String header) {
        MFXFontIcon infoIcon = new MFXFontIcon("mfx-info-circle-filled", 18);
        dialogContent.setHeaderIcon(infoIcon);
        dialogContent.setHeaderText(header);
        convertDialogTo("mfx-info-dialog");
        dialog.showDialog();
    }

    @FXML
    public void showWarning(String header) {
        MFXFontIcon warnIcon = new MFXFontIcon("mfx-do-not-enter-circle", 18);
        dialogContent.setHeaderIcon(warnIcon);
        dialogContent.setHeaderText(header);
        convertDialogTo("mfx-warn-dialog");
        dialog.showDialog();
    }

    @FXML
    public void showAndWaitWarning(String header){
        MFXFontIcon warnIcon = new MFXFontIcon("mfx-do-not-enter-circle", 18);
        dialogContent.setHeaderIcon(warnIcon);
        dialogContent.setHeaderText(header);
        convertDialogTo("mfx-warn-dialog");
        dialog.showAndWait();
    }

    @FXML
    public void showError(String header) {
        MFXFontIcon errorIcon = new MFXFontIcon("mfx-exclamation-circle-filled", 18);
        dialogContent.setHeaderIcon(errorIcon);
        dialogContent.setHeaderText(header);
        convertDialogTo("mfx-error-dialog");
        dialog.showDialog();
    }

    @FXML
    public void showGeneric(String header) {
        dialogContent.setHeaderIcon(null);
        dialogContent.setHeaderText(header);
        convertDialogTo(null);
        dialog.showDialog();
    }

    public void convertDialogTo(String styleClass) {
        dialogContent.getStyleClass().removeIf(
                s -> s.equals("mfx-info-dialog") || s.equals("mfx-warn-dialog") || s.equals("mfx-error-dialog")
        );

        if (styleClass != null) {
            dialogContent.getStyleClass().add(styleClass);
        }

    }

}
