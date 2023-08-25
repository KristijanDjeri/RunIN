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

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;

 public class FileManager {

    public void deleteDirectory(String path){
        try {
            Files.walkFileTree(Paths.get(path), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public File[] findFilesByNameAndExtension(String dir,String name, String[] extensions){
        File directory = new File(dir);
        return directory.listFiles((dir1, name1)->{
            for(String ext:extensions){
                if(name1.toLowerCase().startsWith(name+".")&&name1.toLowerCase().endsWith("."+ext)){
                    return true;
                }
            }
            return false;
        });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void deleteFile(String path){
        File file = new File(path);
        if(file.exists()){
            file.delete();
        }
    }

     public void copyToDirectory(String source, String target, String prefix, String suffix){
         Path srcDir = Paths.get(source);
         Path destDir = Paths.get(target);

         try {
             Files.walkFileTree(srcDir, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE,
                     new FileVisitor<>() {
                         @Override
                         public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                             Path targetDir = destDir.resolve(srcDir.relativize(dir));
                             File file = new File(targetDir.toString());
                             if (!file.exists()&&file.getName().startsWith(prefix)&&file.getName().endsWith(suffix)) {
                                 Files.createDirectory(targetDir);
                             }
                             return FileVisitResult.CONTINUE;
                         }

                         @Override
                         public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                             Path targetFile = destDir.resolve(srcDir.relativize(file));
                             File file1 = new File(file.toString());
                             if(file1.getName().startsWith(prefix)&&file1.getName().endsWith(suffix)) {
                                 Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                             }
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
         } catch (IOException ex) {
             throw new RuntimeException(ex);
         }
     }

}
