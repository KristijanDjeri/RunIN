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

import com.runin.shared.*;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.SubScene;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.layout.Pane;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.net.URL;
import java.util.ResourceBundle;

public class StageViewController extends SubSceneManager implements Initializable {

    @FXML
    private SubScene subScene;
    @FXML
    private AnchorPane mainPane,subScenePane,topPane;
    @FXML
    private MFXButton homeButton,runnersButton,eventsButton,historyButton,settingsButton;
    @FXML
    private VBox menuVbox;
    @FXML
    private Pane shadowPane;
    private final ButtonSelect buttonSelect;
    private static StageViewController INSTANCE = null;
    private static Stage stage;
    private double width,height,xOffset,yOffset;
    private boolean maximized=false;

    public static StageViewController getInstance(){
        if(INSTANCE==null){
            INSTANCE = new StageViewController();
        }
        return INSTANCE;
    }

    public static Stage getStage(){
        return stage;
    }

    public static void setStage(Stage stage){
        StageViewController.stage = stage;
    }

    StageViewController(){
        buttonSelect = new ButtonSelect();
    }

    public void setScene(String fxmlLocation, Object controller, Section section, Save save){
        subScenePane.getChildren().remove(subScene);
        subScene = processRequest(fxmlLocation,controller,section, save);
        subScene.widthProperty().bind(subScenePane.widthProperty());
        subScene.heightProperty().bind(subScenePane.heightProperty());
        subScenePane.getChildren().add(subScene);

        switch(section){
            case HOME -> buttonSelect.setSelectedButton(homeButton);
            case RUNNER -> buttonSelect.setSelectedButton(runnersButton);
            case EVENT -> buttonSelect.setSelectedButton(eventsButton);
            case HISTORY -> buttonSelect.setSelectedButton(historyButton);
            case SETTINGS -> buttonSelect.setSelectedButton(settingsButton);
        }

    }

    @SuppressWarnings("unused")
    public void setSection(String fxmlLocation, Object controller, Section section, Save save){
        processRequest(fxmlLocation,controller,section, save);
    }

    public void setScene(SubScene scene){
        subScenePane.getChildren().remove(subScene);
        subScene = scene;
        resize();
        subScenePane.getChildren().add(subScene);
    }

    public void resize(){
        subScene.widthProperty().bind(subScenePane.widthProperty());
        subScene.heightProperty().bind(subScenePane.heightProperty());
    }

    public void setFullScreenStage(boolean fullScreen){
        if(!stage.isFullScreen()&&fullScreen){
            width = stage.getWidth();
            height = stage.getHeight();
            topPane.setVisible(false);
        }
        stage.setFullScreenExitHint("");
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        stage.setFullScreen(fullScreen);
        if(!stage.isFullScreen()){
            stage.setWidth(width);
            stage.setHeight(height);
            topPane.setVisible(true);
        }
        resize();
    }

    public void hideMenu(boolean hide){
        menuVbox.setVisible(!hide);
        if(hide) {
            AnchorPane.setLeftAnchor(subScenePane,0.0);
        }else{
            AnchorPane.setLeftAnchor(subScenePane,200.0);
        }
    }

    public void setShadow(boolean shadow){
        shadowPane.setVisible(shadow);
    }

    @FXML
    private void exit(){
        stage.close();
    }

    @FXML
    private void maximize(){
        if(!maximized){
            width = stage.getWidth();
            height = stage.getHeight();
        }
        maximized=!maximized;
        if(!maximized){
            stage.setWidth(width);
            stage.setHeight(height);
        }else{
            Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
            stage.setX(0);
            stage.setY(0);
            stage.setWidth(dimension.width);
            stage.setHeight(dimension.height);
        }
    }

    @FXML
    private void minimize(){
        stage.setIconified(true);
    }

    private void draggableStage(){
        topPane.setOnMousePressed(event->{
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        topPane.setOnMouseDragged(event->{
            stage.setX(event.getScreenX()-xOffset);
            stage.setY(event.getScreenY()-yOffset);
        });
    }

    @FXML
    public void homeView(){
        setScene(loadSection(Section.HOME));
        buttonSelect.setSelectedButton(homeButton);
    }
    @FXML
    public void runnersView(){
        setScene(loadSection(Section.RUNNER));
        buttonSelect.setSelectedButton(runnersButton);
    }
    @FXML
    public void eventsView(){
        setScene(loadSection(Section.EVENT));
        buttonSelect.setSelectedButton(eventsButton);
    }

    @FXML
    private void historyView(){
        setScene(loadSection(Section.HISTORY));
        buttonSelect.setSelectedButton(historyButton);
    }

    @FXML
    private void settingsView(){
        setScene(loadSection(Section.SETTINGS));
        buttonSelect.setSelectedButton(settingsButton);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        draggableStage();
        loadLang();
        loadIcons();
    }

    private void loadIcons(){
        homeButton.setGraphic(new ImageView(ImageManager.getIcon("home")));
        runnersButton.setGraphic(new ImageView(ImageManager.getIcon("users")));
        eventsButton.setGraphic(new ImageView(ImageManager.getIcon("calender")));
        historyButton.setGraphic(new ImageView(ImageManager.getIcon("update")));
        settingsButton.setGraphic(new ImageView(ImageManager.getIcon("setting")));
    }

    private void loadLang(){
        homeButton.setText(Lang.get("home"));
        runnersButton.setText(Lang.get("runners"));
        eventsButton.setText(Lang.get("events"));
        historyButton.setText(Lang.get("history"));
        settingsButton.setText(Lang.get("settings"));

        TextResize.resize(homeButton);
        TextResize.resize(runnersButton);
        TextResize.resize(eventsButton);
        TextResize.resize(historyButton);
        TextResize.resize(settingsButton);
    }

}
