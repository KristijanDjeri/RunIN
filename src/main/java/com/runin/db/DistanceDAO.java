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

import com.runin.record.Distance;
import com.runin.shared.ConvertResultSet;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DistanceDAO extends DAO implements IDAO<Distance> {

    protected final String URL;

    public DistanceDAO(int eventID){
        URL = LOCATION+"/events/"+eventID+"/db";
        try{
            checkTableValidity();
        } catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getURL() {
        return URL;
    }

    @Override
    public List<Distance> getAll() throws SQLException {
        List<Distance> distanceList = new ArrayList<>();
        PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM Distances");
        ResultSet resultSet = preparedStatement.executeQuery();
        ConvertResultSet convertResultSet = new ConvertResultSet();
        while(resultSet.next()){
            distanceList.add(convertResultSet.toDistanceModel(resultSet));
        }
        close(preparedStatement,resultSet);
        return distanceList;
    }

    @Override
    public Distance get(int id) throws SQLException {
        PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM Distances WHERE id=?");
        preparedStatement.setInt(1,id);
        ResultSet resultSet = preparedStatement.executeQuery();
        Distance distance = null;
        if(resultSet.next()){
            distance = new ConvertResultSet().toDistanceModel(resultSet);
        }
        return distance;
    }

    @Override
    public void insert(Distance model) throws SQLException {
        PreparedStatement preparedStatement = getConnection().prepareStatement("INSERT INTO Distances(name,distance_in_km) VALUES (?,?)");
        preparedStatement.setString(1, model.name());
        preparedStatement.setDouble(2,model.distance_in_km());
        preparedStatement.executeUpdate();
        close(preparedStatement,null);
    }

    @Override
    public void update(Distance model) throws SQLException {
        PreparedStatement preparedStatement = getConnection().prepareStatement("UPDATE Distances SET name=?, distance_in_km=? WHERE id=?");
        preparedStatement.setString(1,model.name());
        preparedStatement.setDouble(2,model.distance_in_km());
        preparedStatement.setInt(3,model.id());
        preparedStatement.executeUpdate();
        close(preparedStatement,null);
    }

    @Override
    public void delete(int id) throws SQLException {
        PreparedStatement preparedStatement = getConnection().prepareStatement("DELETE FROM Distances WHERE id=?");
        preparedStatement.setInt(1,id);
        preparedStatement.executeUpdate();
        close(preparedStatement,null);
    }

    @Override
    public void checkTableValidity() throws SQLException {
        openConnection();
        (getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS Distances(id INT AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(20) UNIQUE,distance_in_km DOUBLE NOT NULL)")).executeUpdate();
        closeConnection();
    }

    public boolean checkIfNameExists(String name) throws SQLException{
        boolean exists = true;
        PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT EXISTS (SELECT * FROM Distances WHERE name=?)");
        preparedStatement.setString(1, name);
        ResultSet resultSet = preparedStatement.executeQuery();
        if(resultSet.next()){
            exists = resultSet.getBoolean(1);
        }
        close(preparedStatement,resultSet);
        return exists;
    }

    public Distance getDistanceViaName(String name) throws SQLException{
        Distance distance=null;
        PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM Distances WHERE name=?");
        preparedStatement.setString(1, name);
        ResultSet resultSet = preparedStatement.executeQuery();
        if(resultSet.next()){
            distance = new ConvertResultSet().toDistanceModel(resultSet);
        }
        close(preparedStatement,resultSet);
        return distance;
    }

}
