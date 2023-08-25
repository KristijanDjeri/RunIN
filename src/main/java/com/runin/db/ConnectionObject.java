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

package com.runin.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class ConnectionObject {

    private Connection connection;

    public Connection openConnection(String url) throws SQLException {
        if(connection!=null&&!connection.isClosed()){
            closeConnection();
        }
        connection = DriverManager.getConnection(url);
        return connection;
    }

    public void closeConnection() throws SQLException {
        if(connection!=null){
            connection.close();
        }
    }

    public Connection getConnection(){
        return connection;
    }

    public abstract Connection openConnection() throws SQLException;
}
