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

package com.runin.main;

import com.runin.shared.FileManager;
import com.runin.shared.ImageManager;
import com.runin.shared.Lang;
import com.runin.shared.ProjectPaths;
import com.runin.stage.StageViewController;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

 public class Main extends Application {

     private static Main INSTANCE;

    @Override
    public void start(Stage stage) throws IOException {

        INSTANCE = this;
        Lang.loadSelectedLanguage();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/runin/stage/stage.fxml"));
        StageViewController stageViewController = StageViewController.getInstance();
        fxmlLoader.setController(stageViewController);
        Scene scene = new Scene(fxmlLoader.load());
        stageViewController.homeView();
        stage.setTitle("RunIN");
        stage.setScene(scene);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.getIcons().add(ImageManager.getIcon("RunIN-Logo"));
        stage.show();
        StageViewController.setStage(stage);
    }

    public static void main(String[] args) {
        checkDirectories();
        launch();
    }

    public static Main getInstance(){
        return INSTANCE;
    }

    public HostServices getHostServicesObject(){
        return getHostServices();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void checkDirectories(){

        Path directoryPath = Paths.get(System.getProperty("user.home"),".RunIN");
        if(!Files.exists(directoryPath)){
            try {
                Files.createDirectory(directoryPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        String resourceDir = directoryPath.toString();

        File file;

        if(!(file = new File(resourceDir+"/resources/config")).exists()){
            file.mkdirs();
            new FileManager().copyToDirectory(ProjectPaths.JAR_DIRECTORY.getPath()+"/resources/config",resourceDir+"/resources/config","","");
        }
        if(!(file = new File(resourceDir+"/h2-db-library")).exists()){
            file.mkdirs();
            new FileManager().copyToDirectory(ProjectPaths.JAR_DIRECTORY.getPath()+"/h2-db-library",resourceDir+"/h2-db-library","","");
        }
        if(!(file = new File(resourceDir+"/backup")).exists()){
            file.mkdirs();
            new FileManager().copyToDirectory(ProjectPaths.JAR_DIRECTORY.getPath()+"/backup",resourceDir+"/backup","","");
        }

    }

}