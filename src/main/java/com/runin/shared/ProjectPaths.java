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

 import com.runin.db.DAO;

 import java.io.File;
 import java.net.URISyntaxException;
 public enum ProjectPaths {

    JAR_DIRECTORY(new Object() {
        String evaluate() {
            try {
                return new File(DAO.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
    }.evaluate()),
     ASSETS(JAR_DIRECTORY.getPath()+"/resources/assets"),
     H2_DB_LIBRARY(System.getProperty("user.home")+"/.RunIN/h2-db-library"),
     CONFIG(System.getProperty("user.home")+"/.RunIN/resources/config"),
     RESOURCES(System.getProperty("user.home")+"/.RunIN")
    ;

    private final String path;

    ProjectPaths(String path){
        this.path = path;
    }

    public String getPath(){
        return path;
    }
}
