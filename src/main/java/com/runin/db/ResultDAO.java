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

import com.runin.record.Result;
import com.runin.shared.ConvertResultSet;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class ResultDAO extends DAO implements IDAO<Result>{

    private final String URL;

    public ResultDAO(int eventID) {
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
    public List<Result> getAll() throws SQLException {
        List<Result> resultList = new ArrayList<>();
        PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM Results");
        ResultSet resultSet = preparedStatement.executeQuery();
        ConvertResultSet convertResultSet = new ConvertResultSet();
        while(resultSet.next()){
            resultList.add(convertResultSet.toResultModel(resultSet));
        }
        close(preparedStatement,resultSet);
        return resultList;
    }

    @Override
    public Result get(int id) throws SQLException {
        PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM Results WHERE id=?");
        preparedStatement.setInt(1,id);
        ResultSet resultSet = preparedStatement.executeQuery();
        Result result = null;
        if(resultSet.next()){
            result = new ConvertResultSet().toResultModel(resultSet);
        }
        close(preparedStatement, resultSet);
        return result;
    }

    @Override
    public void insert(Result model) throws SQLException {
        PreparedStatement preparedStatement = getConnection().prepareStatement("INSERT INTO Results(participant_id,gender,distance_id,category_id,finish_time) VALUES (?,?,?,?,?)");
        preparedStatement.setInt(1,model.participant_id());
        preparedStatement.setBoolean(2, model.gender());
        preparedStatement.setShort(3,model.distance_id());
        preparedStatement.setShort(4, model.category_id());
        preparedStatement.setTime(5, Time.valueOf(model.finish_time()));
        preparedStatement.executeUpdate();
        close(preparedStatement,null);
    }

    @Override
    public void update(Result model) throws SQLException {
        PreparedStatement preparedStatement = getConnection().prepareStatement("UPDATE Results SET participant_id=?, gender=?, distance_id=?, category_id=?, finish_time=? WHERE id=?");
        preparedStatement.setInt(1,model.participant_id());
        preparedStatement.setBoolean(2, model.gender());
        preparedStatement.setShort(3,model.distance_id());
        preparedStatement.setShort(4,model.category_id());
        preparedStatement.setTime(5,Time.valueOf(model.finish_time()));
        preparedStatement.setInt(6,model.id());
        preparedStatement.executeUpdate();
        close(preparedStatement, null);
    }

    @Override
    public void delete(int id) throws SQLException {
        PreparedStatement preparedStatement = getConnection().prepareStatement("DELETE FROM Results WHERE id=?");
        preparedStatement.setInt(1,id);
        preparedStatement.executeUpdate();
        close(preparedStatement,null);
    }

    @Override
    public void checkTableValidity() throws SQLException {
        openConnection();
        (getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS Results(id INT AUTO_INCREMENT PRIMARY KEY, " +
                "participant_id INT, gender BOOLEAN, distance_id SMALLINT NOT NULL, category_id SMALLINT NOT NULL, finish_time TIME NOT NULL)")).executeUpdate();
        closeConnection();
    }

    public void clear() throws SQLException {
        PreparedStatement preparedStatement = getConnection().prepareStatement("DROP TABLE Results");
        preparedStatement.executeUpdate();
        close(preparedStatement,null);
    }

    public boolean participantExists(int number) throws SQLException {
        boolean b;
        PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM Results WHERE participant_id=?");
        preparedStatement.setInt(1,number);
        ResultSet resultSet = preparedStatement.executeQuery();
        b = resultSet.next();
        close(preparedStatement,resultSet);
        return b;
    }

    public int getIDFromParticipant(int number) throws SQLException {
        int id=0;
        PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT id FROM Results WHERE participant_id=?");
        preparedStatement.setInt(1,number);
        ResultSet resultSet = preparedStatement.executeQuery();
        if(resultSet.next()){
            id = resultSet.getInt("id");
        }
        close(preparedStatement,resultSet);
        return id;
    }

}
