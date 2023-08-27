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

package com.runin.event;

import com.runin.db.CategoryDAO;
import com.runin.db.EventDAO;
import com.runin.db.ParticipantDAO;
import com.runin.history.HistoryViewController;
import com.runin.main.Main;
import com.runin.model_managers.ResultTableManager;
import com.runin.models.ResultTable;
import com.runin.participant.ParticipantsViewController;
import com.runin.model_managers.ParticipantTableManager;
import com.runin.models.ParticipantTable;
import com.runin.race.RaceSetupViewController;
import com.runin.record.Event;
import com.runin.result.ResultViewController;
import com.runin.shared.*;
import com.runin.stage.StageViewController;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXScrollPane;
import io.github.palexdev.materialfx.controls.legacy.MFXLegacyTableView;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

public class EventsDetailedViewController implements Initializable {

    @FXML
    private PieChart pieChart;
    @FXML
    private MFXLegacyTableView<?> tableView;
    @FXML
    private Label nameLabel;
    @FXML
    private MFXButton editEventButton,participantButton,actionButton;
    @FXML
    private FlowPane flowPane;
    @FXML
    private MFXScrollPane scrollPane;

    private Event event;
    private final String PATH;
    private String currentPath;
    private ContextMenu contextMenu;
    private final Section section;

    public EventsDetailedViewController(Event event,Section section){
        this.event = event;
        this.section = section;
        PATH = ProjectPaths.H2_DB_LIBRARY.getPath()+"/events/"+event.id()+"/user-data";
        currentPath = PATH;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        if(event.date().toLocalDate().isBefore(LocalDate.now())&&!event.editable_results()&&!event.finished()){
            DialogController dialogController = new DialogController(StageViewController.getStage());
            dialogController.setTitle(Lang.get("warning"));
            dialogController.setContentText(Lang.get("dialogED4"));
            dialogController.getSecondaryButton().setText(Lang.get("leaveIt"));
            dialogController.getPrimaryButton().setText(Lang.get("changeIt"));
            dialogController.addAction(dialogController.getSecondaryButton(),e->{
                EventDAO eventDAO = new EventDAO();
                Event event1 = new Event(event.id(),event.name(),event.location(),event.date(),false,true);
                try{
                    eventDAO.openConnection();
                    eventDAO.update(event1);
                    eventDAO.closeConnection();
                }catch (SQLException ex){
                    throw new RuntimeException(ex);
                }
                event = event1;
                StageViewController.getInstance().setScene(FXMLPaths.EVENT_DETAILED.PATH, this,section,Save.NO);
                dialogController.close();
            });
            dialogController.addAction(dialogController.getPrimaryButton(),e->{
                StageViewController.getInstance().setScene(FXMLPaths.EVENT_EDIT.PATH, new EventsEditViewController(event),Section.EVENT,Save.NO);
                dialogController.close();
            });
            dialogController.showWarning(Lang.get("warning"));
        }

        checkDirectories();
        nameLabel.setText(event.name()+"-"+event.location()+"-"+event.date());

        MenuItem rename = new MenuItem(Lang.get("rename"));
        MenuItem delete = new MenuItem(Lang.get("delete"));

        rename.setGraphic(ImageManager.getImageView(ImageManager.getIcon("pen"),15));
        delete.setGraphic(ImageManager.getImageView(ImageManager.getIcon("delete"),15));

        rename.setOnAction(x->{
            MFXButton button = (MFXButton) ((((MenuItem)x.getSource()).getParentPopup()).getOwnerNode());
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(button.getText(), 0, button.getText().lastIndexOf("."));
                new TextInputController(Lang.get("enterNewName")+":",Lang.get("rename"),stringBuilder);

                String extension = (String.valueOf(stringBuilder).contains("."))?"":button.getText().substring(button.getText().lastIndexOf('.'));

                File oldFile = new File(currentPath+"/"+button.getText());
                File newFile = new File(currentPath+"/"+stringBuilder+extension);

                if(!oldFile.renameTo(newFile)){
                    DialogController dialogController = new DialogController(StageViewController.getStage());
                    dialogController.setContentText(Lang.get("dialogED3"));
                    dialogController.showError(Lang.get("error"));
                }else{
                    loadFiles(currentPath);
                }
        });

        delete.setOnAction(x->{
            MFXButton button = (MFXButton)((MenuItem) x.getSource()).getParentPopup().getOwnerNode();
            DialogController dialogController = new DialogController(StageViewController.getStage());
            dialogController.setContentText(Lang.get("dialogED2")+" ["+button.getText()+"]");
            dialogController.getSecondaryButton().setText(Lang.get("cancel"));
            dialogController.getPrimaryButton().setText(Lang.get("continue"));
            dialogController.addAction(dialogController.getSecondaryButton(),e->dialogController.close());
            dialogController.addAction(dialogController.getPrimaryButton(),e->{
                File file = new File(currentPath+"/"+button.getText());
                if(file.isDirectory()){
                    new FileManager().deleteDirectory(file.getAbsolutePath());
                }else{
                    new FileManager().deleteFile(file.getAbsolutePath());
                }
                loadFiles(currentPath);
                dialogController.close();
            });
            dialogController.showWarning(Lang.get("warning"));
        });

        contextMenu = new ContextMenu();

        contextMenu.getItems().addAll(rename,delete);



        loadStatsChart();
        if(event.finished()){
            ResultTableManager resultTableManager = new ResultTableManager(event);
            resultTableManager.loadTableView((MFXLegacyTableView<ResultTable>) tableView);
            tableView.getColumns().remove(2);
            tableView.getColumns().remove(2);
            tableView.getColumns().remove(2);
            resultTableManager.refresh();
        }else {
            ParticipantTableManager participantTableManager = new ParticipantTableManager(event);
            participantTableManager.loadTableView((MFXLegacyTableView<ParticipantTable>) tableView);
            tableView.getColumns().remove(2);
            tableView.getColumns().remove(2);
            participantTableManager.refresh();
        }

        ImageView editImageView = new ImageView(ImageManager.getIcon("edit"));
        editImageView.setFitHeight(25);
        editImageView.setFitWidth(25);
        editEventButton.setGraphic(editImageView);
        editEventButton.setText(Lang.get("editEvent"));

        ImageView participantImageView = new ImageView(ImageManager.getIcon("users"));
        participantImageView.setFitWidth(25);
        participantImageView.setFitHeight(25);
        participantButton.setGraphic(participantImageView);
        participantButton.setText(Lang.get("participants"));

        pieChart.setTitle(Lang.get("distance"));

        ImageView actionImageView = new ImageView();
        actionImageView.setFitWidth(25);
        actionImageView.setFitHeight(25);
        if(event.finished()) {
            editEventButton.setDisable(true);
            participantButton.setDisable(true);
            actionButton.setText(Lang.get("viewResults"));
            actionButton.setOnAction(x -> viewResults());
            actionImageView.setImage(ImageManager.getIcon("post"));
            actionButton.setGraphic(actionImageView);
        }else if(event.editable_results()){
            editEventButton.setOnAction(x -> editEvent());
            participantButton.setOnAction(x -> gotoParticipantsPage());
            actionButton.setText(Lang.get("editResults"));
            actionButton.setOnAction(x -> viewResults());
            actionImageView.setImage(ImageManager.getIcon("post-add"));
            actionButton.setGraphic(actionImageView);
        }else {
            editEventButton.setOnAction(x -> editEvent());
            participantButton.setOnAction(x -> gotoParticipantsPage());
            actionButton.setOnAction(x -> startEvent());
            actionButton.setText(Lang.get("startEvent"));
            actionImageView.setImage(ImageManager.getIcon("play"));
            actionButton.setGraphic(actionImageView);
            if (tableView.getItems().isEmpty()||!event.date().toLocalDate().isEqual(LocalDate.now())) {
                actionButton.setDisable(true);
            }

            TextResize.resize(editEventButton);
            TextResize.resize(participantButton);
            TextResize.resize(actionButton);
            TextResize.resize(nameLabel);

        }

        scrollPane.setOnDragOver(x->{
            if(x.getDragboard().hasFiles()){
                x.acceptTransferModes(TransferMode.COPY);
            }
            x.consume();
        });
        scrollPane.setOnDragDropped(x->{
            List<File> files = x.getDragboard().getFiles();
            boolean changed = false;
            for(File file:files){
                try {
                    if(file.isDirectory()){
                        Path source = Path.of(file.getAbsolutePath());
                        Path target = Path.of(currentPath+"/"+file.getName());
                        Files.walkFileTree(source, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE, new FileVisitor<>() {
                            @Override
                            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs){
                                Path targetDir = target.resolve(source.relativize(dir));
                                try {
                                    Files.copy(dir, targetDir);
                                } catch (IOException e) {
                                    return FileVisitResult.SKIP_SUBTREE; // skip processing
                                }
                                return FileVisitResult.CONTINUE;
                            }

                            @Override
                            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                Files.copy(file, target.resolve(source.relativize(file)));
                                return FileVisitResult.CONTINUE;
                            }

                            @Override
                            public FileVisitResult visitFileFailed(Path file, IOException exc){
                                return FileVisitResult.CONTINUE;
                            }

                            @Override
                            public FileVisitResult postVisitDirectory(Path dir, IOException exc){
                                return FileVisitResult.CONTINUE;
                            }
                        });
                        changed=true;
                    }else if(!Files.exists(Paths.get(currentPath+"/"+file.getName()))) {
                        Files.copy(Paths.get(file.getAbsolutePath()), Paths.get(currentPath + "/" + file.getName()), StandardCopyOption.COPY_ATTRIBUTES);
                        changed=true;
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            x.setDropCompleted(true);
            scrollPane.setId("");
            if(changed) {
                loadFiles(currentPath);
            }
            x.consume();
        });
        scrollPane.setOnDragDone(x->{
            if(x.getTransferMode() == TransferMode.COPY){
                loadFiles(currentPath);
            }
            x.consume();
        });
        scrollPane.setOnDragEntered(x->scrollPane.setId("dashedGreenBorder"));
        scrollPane.setOnDragExited(x->scrollPane.setId(""));

    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void checkDirectories(){
        new File(PATH).mkdirs();
        loadFiles(PATH);
    }

    private void loadFiles(String path){
        flowPane.getChildren().clear();
        currentPath = path;
        if(!path.equals(PATH)){
            MFXButton back = new MFXButton("<");
            back.setId("neutralRed");
            back.setPrefWidth(90);
            back.setPrefHeight(90);
            back.setAlignment(Pos.CENTER);
            back.setFont(Font.font("System",FontWeight.BOLD,25));
            back.setOnAction(x->loadFiles(new File(currentPath).getParent()));
            flowPane.getChildren().add(0,back);
        }

        MFXButton button = new MFXButton("+");
        button.setId("neutralGreen");
        button.setPrefWidth(90);
        button.setPrefHeight(90);
        button.setAlignment(Pos.CENTER);
        button.setFont(Font.font("System", FontWeight.BOLD,25));
        button.setOnAction(x->uploadFile());

        flowPane.getChildren().add(button);

        File[] files = new File(path).listFiles();

        assert files != null;
        for(File file:files){

            if(file.isDirectory()){
                MFXButton directoryButton = getDirectoryButton(path, file);
                flowPane.getChildren().add(flowPane.getChildren().size()-1,directoryButton);
                continue;
            }

            MFXButton fileButton = getFileButton(file);
            flowPane.getChildren().add(flowPane.getChildren().size()-1,fileButton);

        }

    }

    private MFXButton getDirectoryButton(String path, File file) {
        MFXButton directoryButton = new MFXButton(file.getName());
        directoryButton.setId("item");
        directoryButton.setPrefWidth(90);
        directoryButton.setPrefHeight(90);
        directoryButton.setAlignment(Pos.BOTTOM_CENTER);
        directoryButton.setContentDisplay(ContentDisplay.TOP);
        directoryButton.setGraphic(ImageManager.getImageView(ImageManager.getIcon("folder"),40));
        directoryButton.setOnAction(x->loadFiles(path +"/"+ file.getName()));
        directoryButton.setOnContextMenuRequested(x-> contextMenu.show(directoryButton,x.getScreenX(),x.getScreenY()));
        directoryButton.setOnDragDetected(x->dragNDropFromApp(directoryButton));
        directoryButton.setOnDragDone(x->loadFiles(currentPath));
        return directoryButton;
    }

    private MFXButton getFileButton(File file) {
        MFXButton fileButton = new MFXButton(file.getName());
        fileButton.setId("item");
        fileButton.setPrefHeight(90);
        fileButton.setPrefWidth(90);
        fileButton.setAlignment(Pos.BOTTOM_CENTER);
        fileButton.setContentDisplay(ContentDisplay.TOP);
        if(file.getName().endsWith(".png")||file.getName().endsWith(".jpeg")||file.getName().endsWith(".gif")||file.getName().endsWith(".bmp")){
            fileButton.setGraphic(ImageManager.getImageView(new Image("file:"+file.getAbsolutePath()),40));
        }else{
            fileButton.setGraphic(ImageManager.getImageView(ImageManager.getIcon("file"),40));
        }
        fileButton.setOnMouseClicked(x->{
            if(x.getButton()== MouseButton.PRIMARY&&x.getClickCount()>1) {
                new Thread(()->{
                    String os = System.getProperty("os.name");
                    String path = file.toURI().toString();
                        if(os.toLowerCase().contains("win")){
                            Main.getInstance().getHostServicesObject().showDocument(path);
                        }else if(os.toLowerCase().contains("mac")){
                            Main.getInstance().getHostServicesObject().showDocument(path);
                        }else if(os.toLowerCase().contains("nix")||os.toLowerCase().contains("nux")){
                            Main.getInstance().getHostServicesObject().showDocument(path);
                        }else{
                            Platform.runLater(()->{
                                DialogController dialogController = new DialogController(StageViewController.getStage());
                                dialogController.setContentText(Lang.get("fileLocation")+":\n" + file.getAbsolutePath());
                                dialogController.showInfo(Lang.get("unableToOpenFile"));
                            });
                        }
                }).start();
            }else{
                fileButton.requestFocus();
            }
        });
        fileButton.setOnContextMenuRequested(x->contextMenu.show(fileButton,x.getScreenX(),x.getScreenY()));
        fileButton.setOnDragDetected(x->dragNDropFromApp(fileButton));
        fileButton.setOnDragDone(x->loadFiles(currentPath));
        return fileButton;
    }

    private void dragNDropFromApp(MFXButton node){

        Dragboard db = node.startDragAndDrop(TransferMode.ANY);
        ClipboardContent content = new ClipboardContent();
        File file = new File(currentPath+"/"+node.getText());
        content.putFiles(List.of(file));
        db.setContent(content);

    }

    public void loadStatsChart(){

        HashMap<String,Integer> stats;
        try{
            ParticipantDAO participantDAO = new ParticipantDAO(event.id());
            participantDAO.openConnection();
            stats = participantDAO.getNumberOfParticipantsPerDistance();
            participantDAO.closeConnection();
        }catch (SQLException e){
            throw new RuntimeException(e);
        }

        if(stats.isEmpty()){
            pieChart.getData().add(new PieChart.Data(Lang.get("noData"),1));
        }else {
            for (String s : stats.keySet()) {
                pieChart.getData().add(new PieChart.Data(s, stats.get(s)));
            }
        }

    }

    @FXML
    private void uploadFile(){
        new FileTypeSelection(this);
    }

    @SuppressWarnings("unused")
    public void uploadImage(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(Lang.get("selectImage"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(Lang.get("all"),"*.jpg","*.JPG","*.jpeg","*.JPEG","*.png","*.PNG","*.mp4","*.MP4","*.mkv","*.MKV","*.ogv","*.OGV"),
                new FileChooser.ExtensionFilter(Lang.get("images"),"*.jpg","*.JPG","*.jpeg","*.JPEG","*.png","*PNG"),
                new FileChooser.ExtensionFilter(Lang.get("videos"),"*.mp4","*.MP4","*.mkv","*.MKV","*.ogv","*.OGV")
        );
        uploadFile(fileChooser);
    }

    @SuppressWarnings("unused")
    public void uploadDocument(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(Lang.get("selectDocument"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(Lang.get("documents"),"*.pdf","*.PDF","*.docx","*.DOCX","*.doc","*.DOC","*.txt","*.TXT")
        );
        uploadFile(fileChooser);
    }

    @SuppressWarnings({"unused", "ResultOfMethodCallIgnored"})
    public void makeNewFolder(){
        StringBuilder stringBuilder = new StringBuilder();
        new TextInputController(Lang.get("textInputED1")+":","",stringBuilder);
        new File(currentPath+"/"+stringBuilder).mkdirs();
        loadFiles(currentPath);
    }

    private void uploadFile(FileChooser fileChooser){
        List<File> files = fileChooser.showOpenMultipleDialog(StageViewController.getStage());
        try{
            for(File file:files){
                if(file.isDirectory()){
                    continue;
                }
                Files.copy(Paths.get(file.getAbsolutePath()),Paths.get(currentPath+"/"+file.getName()));
            }
        }catch (IOException e){
            throw new RuntimeException(e);
        }
        loadFiles(currentPath);
    }

    private void editEvent(){
        StageViewController.getInstance().setScene(FXMLPaths.EVENT_EDIT.PATH, new EventsEditViewController(event), section, Save.NO);
    }

    private void gotoParticipantsPage(){
        StageViewController.getInstance().setScene(FXMLPaths.TABLE_VIEW.PATH, new ParticipantsViewController(event), section, Save.NO);
    }

    private void startEvent() {
        boolean b=true;
        try {
            CategoryDAO categoryDAO = new CategoryDAO(event.id());
            categoryDAO.openConnection();
            if (categoryDAO.getAll().isEmpty()) {
                DialogController dialogController = new DialogController(StageViewController.getStage());
                dialogController.setContentText(Lang.get("dialogED1"));
                dialogController.showWarning(Lang.get("warning"));
                b = false;
            }
            categoryDAO.closeConnection();
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
        if(!b) {
            return;
        }
        StageViewController.getInstance().setScene(FXMLPaths.RACE_SETUP.PATH, new RaceSetupViewController(event), Section.EVENT, Save.NO);
    }

    private void viewResults(){
        StageViewController.getInstance().setScene(FXMLPaths.TABLE_VIEW.PATH, new ResultViewController(event,section),section,Save.NO);
    }

    @FXML
    @SuppressWarnings("unused")
    private void back(){
        if(section == Section.EVENT) {
            StageViewController.getInstance().setScene(FXMLPaths.TABLE_VIEW.PATH, new EventsMainViewController(), Section.EVENT, Save.NO);
        }else{
            StageViewController.getInstance().setScene(FXMLPaths.TABLE_VIEW.PATH, new HistoryViewController(), Section.HISTORY, Save.NO);
        }
    }

}
