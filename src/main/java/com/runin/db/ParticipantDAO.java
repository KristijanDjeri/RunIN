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

import com.runin.record.Participant;
import com.runin.shared.ConvertResultSet;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ParticipantDAO extends DAO implements IDAO<Participant> {

    private final String URL;
    private final int eventID;

    public ParticipantDAO(int eventID) {
        this.eventID = eventID;
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
    public List<Participant> getAll() throws SQLException {
        List<Participant> participantList = new ArrayList<>();
        PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM Participants");
        ResultSet resultSet = preparedStatement.executeQuery();
        ConvertResultSet convertResultSet = new ConvertResultSet();
        while(resultSet.next()){
            participantList.add(convertResultSet.toParticipantModel(resultSet));
        }
        close(preparedStatement,resultSet);
        return participantList;
    }

    @Override
    public Participant get(int id) throws SQLException {
        PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM Participants WHERE id=?");
        preparedStatement.setInt(1,id);
        ResultSet resultSet = preparedStatement.executeQuery();
        Participant participant = null;
        if(resultSet.next()){
            participant = new ConvertResultSet().toParticipantModel(resultSet);
        }
        close(preparedStatement,resultSet);
        return participant;
    }

    @Override
    public void insert(Participant model) throws SQLException {
        PreparedStatement preparedStatement = getConnection().prepareStatement("INSERT INTO Participants(id,first_name,last_name,age,gender,distance_id,category_id,runner_id) VALUES (NEXT VALUE FOR participants_sequence,?,?,?,?,?,?,?)");
        preparedStatement.setString(1, model.first_name());
        preparedStatement.setString(2, model.last_name());
        preparedStatement.setInt(3,model.age());
        preparedStatement.setBoolean(4,model.gender());
        preparedStatement.setInt(5,model.distance_id());
        preparedStatement.setInt(6,model.category_id());
        preparedStatement.setInt(7,model.runner_id());
        preparedStatement.executeUpdate();
        close(preparedStatement, null);
    }

    @Override
    public void update(Participant model) throws SQLException {
        PreparedStatement preparedStatement = getConnection().prepareStatement("UPDATE Participants SET first_name=?, last_name=?, age=?, gender=?, distance_id=?, category_id=?, runner_id=? WHERE id=?");
        preparedStatement.setString(1, model.first_name());
        preparedStatement.setString(2, model.last_name());
        preparedStatement.setInt(3,model.age());
        preparedStatement.setBoolean(4,model.gender());
        preparedStatement.setInt(5,model.distance_id());
        preparedStatement.setInt(6,model.category_id());
        preparedStatement.setInt(7,model.runner_id());
        preparedStatement.setInt(8,model.id());
        preparedStatement.executeUpdate();
        close(preparedStatement, null);
    }

    @Override
    public void delete(int id) throws SQLException {
        PreparedStatement preparedStatement = getConnection().prepareStatement("DELETE FROM Participants WHERE id=?");
        preparedStatement.setInt(1,id);
        preparedStatement.executeUpdate();
        close(preparedStatement, null);
    }

    @Override
    public void checkTableValidity() throws SQLException {
        openConnection();
        (getConnection().prepareStatement("CREATE SEQUENCE IF NOT EXISTS participants_sequence START WITH 1 INCREMENT BY 1")).executeUpdate();
        (getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS Participants(id INT PRIMARY KEY," +
                "first_name VARCHAR(20), last_name VARCHAR(20), age SMALLINT, gender BOOLEAN, distance_id SMALLINT, category_id SMALLINT, runner_id BIGINT)")).executeUpdate();
        closeConnection();
    }

    public int getNextIdValue() throws SQLException {
        int id = 1;
        PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT NEXT VALUE FOR participants_sequence AS next_value");
        ResultSet resultSet = preparedStatement.executeQuery();
        if(resultSet.next()){
            id = resultSet.getInt("next_value");
        }
        preparedStatement = getConnection().prepareStatement("ALTER SEQUENCE participants_sequence RESTART WITH NEXT VALUE FOR participants_sequence - 1");
        preparedStatement.executeUpdate();
        close(preparedStatement,resultSet);
        return id;
    }

    public boolean participantExists(int id) throws SQLException {
        boolean exists = false;
        PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM Participants WHERE id=?");
        preparedStatement.setInt(1,id);
        ResultSet resultSet = preparedStatement.executeQuery();
        if(resultSet.next()){
            exists = true;
        }
        close(preparedStatement,resultSet);
        return exists;
    }

    public HashMap<String,Integer> getNumberOfParticipantsPerDistance() throws SQLException {
        HashMap<String,Integer> stats = new HashMap<>();
        new DistanceDAO(eventID);
        PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT COUNT(Participants.id) AS total, Distances.name AS name FROM Participants RIGHT JOIN Distances ON Participants.distance_id=Distances.id GROUP BY Distances.id");
        ResultSet resultSet = preparedStatement.executeQuery();
        while(resultSet.next()){
            stats.put(resultSet.getString("name"),resultSet.getInt("total"));
        }
        close(preparedStatement,resultSet);

        return stats;
    }

    public boolean runnerExists(int id) throws SQLException{
        boolean exists;
        PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM Participants WHERE runner_id=?");
        preparedStatement.setInt(1,id);
        ResultSet resultSet = preparedStatement.executeQuery();
        exists = resultSet.next();
        close(preparedStatement,resultSet);
        return exists;
    }

}
