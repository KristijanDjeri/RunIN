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

import com.runin.shared.ProjectPaths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class DAO extends ConnectionObject {

    protected final String LOCATION;

    {
        LOCATION = "jdbc:h2:"+ ProjectPaths.H2_DB_LIBRARY.getPath();
    }

    public abstract String getURL();

    @Override
    public Connection openConnection() throws SQLException {
        return openConnection(getURL());
    }

    public void close(PreparedStatement preparedStatement, ResultSet resultSet){
        try {
            if(resultSet!=null){
                resultSet.close();
            }
            if(preparedStatement != null){
                preparedStatement.close();
            }
        }catch(SQLException e){
            throw new RuntimeException(e);
        }
    }

}
