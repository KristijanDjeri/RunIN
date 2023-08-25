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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesIO {
    private final Properties properties = new Properties();
    private String filePath;

    public PropertiesIO(String filePath){
        this.filePath = filePath;
        setFilePath(filePath);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void setFilePath(String filePath){
        this.filePath = filePath;
        File file = new File(filePath);
        try {
            if(!file.exists()){
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            properties.load(new FileInputStream(filePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void clear(){
        properties.clear();
    }

    public String read(String key){
        return properties.getProperty(key);
    }

    public void write(String key,String value){
        try {
            properties.setProperty(key,value);
            properties.store(new FileOutputStream(filePath),"DO NOT MODIFY THIS FILE!\nApp might not work properly.");
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

}
