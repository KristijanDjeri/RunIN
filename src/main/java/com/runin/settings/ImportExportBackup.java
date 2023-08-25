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

import com.runin.db.EventDAO;
import com.runin.db.RunnerDAO;
import com.runin.model_managers.EventTableManager;
import com.runin.model_managers.RunnerTableManager;
import com.runin.shared.*;
import com.runin.stage.StageViewController;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import java.io.*;
import java.nio.file.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ImportExportBackup {


    protected void importEverything(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(Lang.get("selectDocument"));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(Lang.get("zipFiles"),"*.zip","*.ZIP"));
        File file = fileChooser.showOpenDialog(StageViewController.getStage());

        Path path;
        try {
            path = Files.createTempDirectory("RunINImport");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        File outputDir = new File(path.toString());
        unzip(file,outputDir);
        String fileName = file.getName().substring(0,file.getName().lastIndexOf('.'));
        importRunnersProcess(outputDir,fileName);
        importEventsProcess(outputDir,fileName);
        new FileManager().deleteDirectory(outputDir.getAbsolutePath());
        RunnerTableManager.getInstance().refresh();
        Objects.requireNonNull(EventTableManager.getInstance(Section.EVENT)).refresh();
        Objects.requireNonNull(EventTableManager.getInstance(Section.HISTORY)).refresh();
    }

    protected void importRunners(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(Lang.get("selectDocument"));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(Lang.get("zipFiles"),"*.zip","*.ZIP"));
        File file = fileChooser.showOpenDialog(StageViewController.getStage());


        Path path;
        try {
            path = Files.createTempDirectory("RunINRunnersImport");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        File outputDir = new File(path.toString());
        unzip(file,outputDir);
        importRunnersProcess(outputDir,file.getName().substring(0,file.getName().lastIndexOf('.')));
        new FileManager().deleteDirectory(outputDir.getAbsolutePath());
        RunnerTableManager.getInstance().refresh();
    }

    protected void importEvents(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(Lang.get("selectDocument"));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(Lang.get("zipFiles"),"*.zip","*.ZIP"));
        File file = fileChooser.showOpenDialog(StageViewController.getStage());

        Path path;
        try {
            path = Files.createTempDirectory("RunINImport");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        File outputDir = new File(path.toString());
        unzip(file,outputDir);
        importEventsProcess(outputDir,file.getName().substring(0,file.getName().lastIndexOf('.')));
        new FileManager().deleteDirectory(outputDir.getAbsolutePath());
        Objects.requireNonNull(EventTableManager.getInstance(Section.EVENT)).refresh();
        Objects.requireNonNull(EventTableManager.getInstance(Section.HISTORY)).refresh();
    }

    private void importRunnersProcess(File outputDir, String name){
        int startID;
        RunnerDAO runnerDAO = new RunnerDAO();
        try{
            runnerDAO.openConnection();
            startID = runnerDAO.getNextIdValue();
            runnerDAO.closeConnection();
        } catch (SQLException e){
            throw new RuntimeException(e);
        }
        List<File> list = Arrays.stream(Objects.requireNonNull(outputDir.listFiles((d, dirName) -> dirName.startsWith(name)))).toList();
        if(list.isEmpty()) {
            return;
        }
        File unzipFile = list.get(0);
        rename(unzipFile.getAbsolutePath() + "/runners", startID, false);
        new FileManager().copyToDirectory(unzipFile.getAbsolutePath() + "/runners", ProjectPaths.H2_DB_LIBRARY.getPath() + "/runners","","");
        try {
            runnerDAO.openConnection();
            runnerDAO.importDatabase(unzipFile.getAbsolutePath() + "/runners.sql");
            runnerDAO.closeConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void importEventsProcess(File outputDir, String name){
        int startID;
        EventDAO eventDAO = new EventDAO();
        try{
            eventDAO.openConnection();
            startID = eventDAO.getNextIdValue();
            eventDAO.closeConnection();
        } catch (SQLException e){
            throw new RuntimeException(e);
        }
        List<File> list = Arrays.stream(Objects.requireNonNull(outputDir.listFiles((d, dirName) -> dirName.startsWith(name)))).toList();
        if(list.isEmpty()) {
            return;
        }
        File unzipFile = list.get(0);
        rename(unzipFile.getAbsolutePath() + "/events", startID, true);
        new FileManager().copyToDirectory(unzipFile.getAbsolutePath() + "/events", ProjectPaths.H2_DB_LIBRARY.getPath() + "/events","","");
        try {
            eventDAO.openConnection();
            eventDAO.importDatabase(unzipFile.getAbsolutePath() + "/events.sql");
            eventDAO.closeConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected void exportEverything(){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(Lang.get("selectDirectory"));
        File file = directoryChooser.showDialog(StageViewController.getStage());
        File eventsDir = exportEventsProcess(null,"RunINExport");
        exportRunnersProcess(eventsDir,"");
        if(file!=null) {
            zip(eventsDir, file, "RunINExport");
        }
    }

    protected void exportRunners(){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(Lang.get("selectDirectory"));
        File file = directoryChooser.showDialog(StageViewController.getStage());
        if(file!=null) {
            zip(exportRunnersProcess(null, "RunINRunnersExport"), file, "RunINRunnersExport");
        }
    }

    protected void exportEvents(){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(Lang.get("selectDirectory"));
        File file = directoryChooser.showDialog(StageViewController.getStage());
        if(file!=null) {
            zip(exportEventsProcess(null, "RunINEventsExport"), file, "RunINEventsExport");
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private File exportEventsProcess(File directory, String exportName){
        if(directory==null||!directory.isDirectory()){
            try {
                directory = new File(Files.createTempDirectory(exportName).toString());
                directory.mkdir();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        EventDAO eventDAO = new EventDAO();
        try {
            eventDAO.openConnection();
            eventDAO.exportDatabase(directory.getAbsolutePath());
            eventDAO.closeConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        new FileManager().copyToDirectory(ProjectPaths.H2_DB_LIBRARY.getPath()+"/events",directory.getAbsolutePath()+"/events","","");
        return directory;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private File exportRunnersProcess(File directory, String exportName){
        if(directory==null||!directory.isDirectory()){
            try {
                directory = new File(Files.createTempDirectory(exportName).toString());
                directory.mkdir();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        RunnerDAO runnerDAO = new RunnerDAO();
        try {
            runnerDAO.openConnection();
            runnerDAO.exportDatabase(directory.getAbsolutePath());
            runnerDAO.closeConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        new FileManager().copyToDirectory(ProjectPaths.H2_DB_LIBRARY.getPath()+"/runners",directory.getAbsolutePath()+"/runners","","");
        return directory;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void zip(File directory, File file, String exportName){
        File zipFile = new File(file.getAbsolutePath()+"/"+exportName+".zip");
        if(zipFile.exists()){
            zipFile.delete();
        }
        try(FileOutputStream fos = new FileOutputStream(zipFile);
            ZipOutputStream zos = new ZipOutputStream(fos)){
            zipDirectory(directory,directory.getName(),zos);
            new FileManager().deleteDirectory(directory.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void unzip(File sourceFile,File outputDirectory){
        try(FileInputStream fis = new FileInputStream(sourceFile);
            ZipInputStream zis = new ZipInputStream(fis)){
            ZipEntry entry;
            while((entry = zis.getNextEntry())!=null){
                File outputFile = new File(outputDirectory, entry.getName());
                if(entry.isDirectory()){
                    outputFile.mkdirs();
                }else{
                    File parentDir = outputFile.getParentFile();
                    if (!parentDir.exists()) {
                        parentDir.mkdirs();
                    }
                    try(FileOutputStream fos = new FileOutputStream(outputFile)){
                        byte[] buffer = new byte[1024];
                        int length;
                        while((length = zis.read(buffer))>0){
                            fos.write(buffer,0,length);
                        }
                    }
                }
                zis.closeEntry();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    protected void backup(){
        PropertiesIO propertiesIO = new PropertiesIO(ProjectPaths.CONFIG.getPath()+"/settings.properties");
        String backup = propertiesIO.read("backup");
        String path = (backup!=null&&!backup.isEmpty())?backup:ProjectPaths.RESOURCES.getPath()+"/backup";
        File dir = new File(path);
        dir.mkdirs();
        if(backup==null||backup.isEmpty()){
            propertiesIO.write("backup",dir.getAbsolutePath());
        }

        List<File> backups = List.of(Objects.requireNonNull(dir.listFiles((d, name) -> name.startsWith("RunINBackup_")&&name.endsWith(".zip"))));
        backups = backups.stream()
                .sorted(Comparator.comparing(f -> extractDateFromFilename(f.getName())))
                .collect(Collectors.toList());

        if(!backups.isEmpty()){
            Date lastBackupDate = extractDateFromFilename(backups.get(backups.size()-1).getName());
            Date currentDate = new Date();
            long differenceInDays = (currentDate.getTime()-lastBackupDate.getTime())/(1000*60*60*24);
            if(differenceInDays<2){
                return;
            }
        }

        if(backups.size()>=5){
            File oldestBackup = backups.get(0);
            oldestBackup.delete();
        }

        File eventsDir = exportEventsProcess(null,"RunINBackup_"+LocalDate.now());
        exportRunnersProcess(eventsDir,"");
        zip(eventsDir, dir, "RunINBackup_"+LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
    }

    private static Date extractDateFromFilename(String filename) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            return sdf.parse(filename.replace("RunINBackup_", "").replace(".zip", ""));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void zipDirectory(File directory, String dirName, ZipOutputStream zos){
        File[] files = directory.listFiles();
        if(files==null){
            return;
        }
        for(File file:files){
            if(file.isDirectory()){
                zipDirectory(file, dirName + File.separator + file.getName(),zos);
            }else{
                try(FileInputStream fis = new FileInputStream(file)){
                    zos.putNextEntry(new ZipEntry(dirName+File.separator+file.getName()));
                    byte[] bytes = new byte[1024];
                    int length;
                    while((length=fis.read(bytes))>=0){
                        zos.write(bytes,0,length);
                    }
                    zos.closeEntry();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void rename(String path,int start,boolean dir){
        File file = new File(path);
        if(file.exists()&&file.isDirectory()){
            List<File> childFiles = new ArrayList<>(Arrays.stream(Objects.requireNonNull(file.listFiles())).toList());
            childFiles.sort(Collections.reverseOrder());
            int i=childFiles.size()-1;
            for(File childFile:childFiles){
                if(childFile.isDirectory()&&dir){
                    childFile.renameTo(new File(file,String.valueOf(i+start)));
                }else if(!dir&&childFile.isFile()){
                    String extension = childFile.getName().substring(childFile.getName().lastIndexOf('.'));
                    childFile.renameTo(new File(file, (i+start)+extension));
                }else{
                    continue;
                }
                i--;
            }
        }
    }

}
