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

import com.runin.record.Event;
import com.runin.shared.ConvertResultSet;
import com.runin.shared.FileManager;
import com.runin.shared.ProjectPaths;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventDAO extends DAO implements IDAO<Event> {

    public EventDAO(){
        try{
            checkTableValidity();
        } catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getURL() {
        return LOCATION+"/events";
    }

    @Override
    public List<Event> getAll() throws SQLException {
        List<Event> eventList = new ArrayList<>();
        PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM Events");
        ResultSet resultSet = preparedStatement.executeQuery();
        ConvertResultSet convertResultSet = new ConvertResultSet();
        while(resultSet.next()){
            eventList.add(convertResultSet.toEventModel(resultSet));
        }
        close(preparedStatement,resultSet);
        return eventList;
    }

    @Override
    public Event get(int id) throws SQLException {
        PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM Events WHERE id=?");
        preparedStatement.setInt(1,id);
        ResultSet resultSet = preparedStatement.executeQuery();
        Event event = null;
        if(resultSet.next()){
            event = new ConvertResultSet().toEventModel(resultSet);
        }
        close(preparedStatement,resultSet);
        return event;
    }

    @Override
    public void insert(Event model) throws SQLException {
        PreparedStatement preparedStatement = getConnection().prepareStatement("INSERT INTO Events(id,name,location,date,finished,editable_results) VALUES (NEXT VALUE FOR events_sequence,?,?,?,?,?)");
        preparedStatement.setString(1,model.name());
        preparedStatement.setString(2,model.location());
        preparedStatement.setDate(3,model.date());
        preparedStatement.setBoolean(4, model.finished());
        preparedStatement.setBoolean(5,model.editable_results());
        preparedStatement.executeUpdate();
        close(preparedStatement,null);
    }

    @Override
    public void update(Event model) throws SQLException {
        PreparedStatement preparedStatement = getConnection().prepareStatement("UPDATE Events SET name=?, location=?, date=?, finished=?, editable_results=? WHERE id=?");
        preparedStatement.setString(1,model.name());
        preparedStatement.setString(2,model.location());
        preparedStatement.setDate(3,model.date());
        preparedStatement.setBoolean(4, model.finished());
        preparedStatement.setBoolean(5, model.editable_results());
        preparedStatement.setInt(6,model.id());
        preparedStatement.executeUpdate();
        close(preparedStatement, null);
    }

    @Override
    public void delete(int id) throws SQLException {
        PreparedStatement preparedStatement = getConnection().prepareStatement("DELETE FROM Events WHERE id=?");
        preparedStatement.setInt(1,id);
        preparedStatement.executeUpdate();
        close(preparedStatement,null);
        new FileManager().deleteDirectory(ProjectPaths.H2_DB_LIBRARY.getPath()+"/events/"+id);
    }

    @Override
    public void checkTableValidity() throws SQLException {
        openConnection();
        (getConnection().prepareStatement("CREATE SEQUENCE IF NOT EXISTS events_sequence START WITH 1 INCREMENT BY 1")).executeUpdate();
        (getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS Events(id INT AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(60) NOT NULL,location VARCHAR(60) NOT NULL, date DATE NOT NULL,finished BOOLEAN,editable_results BOOLEAN)")).executeUpdate();
        closeConnection();
    }

    public boolean checkIfEventExists(String name, String location, Date date) throws SQLException {
        boolean exists = false;
        PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT EXISTS (SELECT * FROM Events WHERE name=? AND location=? AND date=?)");
        preparedStatement.setString(1,name);
        preparedStatement.setString(2,location);
        preparedStatement.setDate(3,date);
        ResultSet resultSet = preparedStatement.executeQuery();
        if(resultSet.next()){
            exists = resultSet.getBoolean(1);
        }
        close(preparedStatement,resultSet);
        return exists;
    }

    public int getIDFromData(String name, String location, Date date) throws SQLException{
        int id = 0;
        PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT id FROM Events WHERE name=? AND location=? AND date=?");
        preparedStatement.setString(1,name);
        preparedStatement.setString(2,location);
        preparedStatement.setDate(3,date);
        ResultSet resultSet = preparedStatement.executeQuery();
        if(resultSet.next()){
            id = resultSet.getInt("id");
        }
        close(preparedStatement,resultSet);
        return id;
    }

    public Event getRecentEvent() throws SQLException {
        Event event;

        PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM Events WHERE finished = TRUE ORDER BY date DESC LIMIT 1");
        ResultSet result = preparedStatement.executeQuery();
        event = (result.next())?new ConvertResultSet().toEventModel(result):null;
        close(preparedStatement,result);

        return event;
    }

    public Event getUpcomingEvent() throws SQLException {
        Event event;

        PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM Events WHERE finished = FALSE AND date >= CURRENT_DATE() ORDER BY date ASC LIMIT 1");
        ResultSet result = preparedStatement.executeQuery();
        event = (result.next())?new ConvertResultSet().toEventModel(result):null;
        close(preparedStatement,result);

        return event;
    }

    public void exportDatabase(String path) throws SQLException {
        PreparedStatement preparedStatement = getConnection().prepareStatement("SCRIPT TO ?");
        preparedStatement.setString(1,path+"/events.sql");
        preparedStatement.execute();
        close(preparedStatement,null);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void importDatabase(String filePath) throws SQLException {
        File file = new File(ProjectPaths.H2_DB_LIBRARY.getPath()+"/tempEvents.mv.db");
        if(file.exists()){
            file.delete();
        }
        PreparedStatement temporaryStatement = DriverManager.getConnection("jdbc:h2:"+ProjectPaths.H2_DB_LIBRARY.getPath()+"/tempEvents").prepareStatement("RUNSCRIPT FROM ?");
        temporaryStatement.setString(1,filePath);
        temporaryStatement.execute();
        temporaryStatement.close();
        temporaryStatement = DriverManager.getConnection("jdbc:h2:"+ProjectPaths.H2_DB_LIBRARY.getPath()+"/tempEvents").prepareStatement("SELECT * FROM Events");
        ResultSet resultSet = temporaryStatement.executeQuery();
        while(resultSet.next()){
            try(PreparedStatement preparedStatement = getConnection().prepareStatement("INSERT INTO Events (id,name,location,date,finished,editable_results) VALUES (NEXT VALUE FOR events_sequence,?,?,?,?,?)")){
                preparedStatement.setString(1,resultSet.getString("name"));
                preparedStatement.setString(2,resultSet.getString("location"));
                preparedStatement.setDate(3,resultSet.getDate("date"));
                preparedStatement.setBoolean(4,resultSet.getBoolean("finished"));
                preparedStatement.setBoolean(5,resultSet.getBoolean("editable_results"));
                preparedStatement.executeUpdate();
            }catch (SQLException e){
                throw new RuntimeException(e);
            }
        }
        temporaryStatement.close();
        new File(ProjectPaths.H2_DB_LIBRARY.getPath()+"/tempEvents.mv.db").delete();
    }

    public int getNextIdValue() throws SQLException {
        int id = 1;
        PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT NEXT VALUE FOR events_sequence AS next_value");
        ResultSet resultSet = preparedStatement.executeQuery();
        if(resultSet.next()){
            id = resultSet.getInt("next_value");
        }
        preparedStatement = getConnection().prepareStatement("ALTER SEQUENCE events_sequence RESTART WITH NEXT VALUE FOR events_sequence - 1");
        preparedStatement.executeUpdate();
        close(preparedStatement,resultSet);
        return id;
    }

}
