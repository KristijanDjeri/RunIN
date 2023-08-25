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

import com.runin.record.Runner;
import com.runin.shared.ConvertResultSet;
import com.runin.shared.ProjectPaths;
import java.io.File;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RunnerDAO extends DAO implements IDAO<Runner> {

    public RunnerDAO(){
        try{
            checkTableValidity();
        } catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getURL() {
        return LOCATION+"/runners";
    }

    @Override
    public List<Runner> getAll() throws SQLException {
        List<Runner> runnerList = new ArrayList<>();
        PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM Runners");
        ResultSet resultSet = preparedStatement.executeQuery();
        ConvertResultSet convertResultSet = new ConvertResultSet();
        while(resultSet.next()){
            runnerList.add(convertResultSet.toRunnerModel(resultSet));
        }
        close(preparedStatement,resultSet);
        return runnerList;
    }

    @Override
    public Runner get(int id) throws SQLException {
        PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM Runners WHERE id=?");
        preparedStatement.setInt(1,id);
        ResultSet resultSet = preparedStatement.executeQuery();
        Runner runner = null;
        if(resultSet.next()){
            runner = new ConvertResultSet().toRunnerModel(resultSet);
        }
        close(preparedStatement, resultSet);
        return runner;
    }

    @Override
    public void insert(Runner model) throws SQLException {
        PreparedStatement preparedStatement = getConnection().prepareStatement("INSERT INTO Runners(id,first_name,last_name,date_of_birth,gender,email,phone_number) VALUES (NEXT VALUE FOR runners_sequence,?,?,?,?,?,?)");
        preparedStatement.setString(1,model.first_name());
        preparedStatement.setString(2, model.last_name());
        preparedStatement.setDate(3,model.date_of_birth());
        preparedStatement.setBoolean(4,model.gender());
        preparedStatement.setString(5,model.email());
        preparedStatement.setString(6,model.phone_number());
        preparedStatement.executeUpdate();
        close(preparedStatement, null);
    }

    @Override
    public void update(Runner model) throws SQLException {
        PreparedStatement preparedStatement = getConnection().prepareStatement("UPDATE Runners SET first_name=?, last_name=?, date_of_birth=?, gender=?, email=?, phone_number=? WHERE id=?");
        preparedStatement.setString(1,model.first_name());
        preparedStatement.setString(2, model.last_name());
        preparedStatement.setDate(3,model.date_of_birth());
        preparedStatement.setBoolean(4,model.gender());
        preparedStatement.setString(5,model.email());
        preparedStatement.setString(6,model.phone_number());
        preparedStatement.setInt(7,model.id());
        preparedStatement.executeUpdate();
        close(preparedStatement, null);
    }

    @Override
    public void delete(int id) throws SQLException {
        PreparedStatement preparedStatement = getConnection().prepareStatement("DELETE FROM Runners WHERE id=?");
        preparedStatement.setInt(1,id);
        preparedStatement.executeUpdate();
    }

    @Override
    public void checkTableValidity() throws SQLException {
        openConnection();
        (getConnection().prepareStatement("CREATE SEQUENCE IF NOT EXISTS runners_sequence START WITH 1 INCREMENT BY 1")).executeUpdate();
        (getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS Runners(id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "first_name VARCHAR(30) NOT NULL,last_name VARCHAR(30) NOT NULL, date_of_birth DATE,gender BOOLEAN NOT NULL,email VARCHAR(40),phone_number VARCHAR(20))")).executeUpdate();
        closeConnection();
    }

    public boolean checkIfNameExists(String firstName, String lastName) throws SQLException {
        boolean exists = false;
        PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT EXISTS (SELECT * FROM Runners WHERE first_name=? AND last_name=?)");
        preparedStatement.setString(1, firstName);
        preparedStatement.setString(2, lastName);
        ResultSet resultSet = preparedStatement.executeQuery();
        if(resultSet.next()){
            exists = resultSet.getBoolean(1);
        }
        close(preparedStatement, resultSet);
        return exists;
    }

    public void exportDatabase(String path) throws SQLException {
        PreparedStatement preparedStatement = getConnection().prepareStatement("SCRIPT TO ?");
        preparedStatement.setString(1,path+"/runners.sql");
        preparedStatement.execute();
        close(preparedStatement,null);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void importDatabase(String filePath) throws SQLException {
        File file = new File(ProjectPaths.H2_DB_LIBRARY.getPath()+"/tempRunners.mv.db");
        if(file.exists()){
            file.delete();
        }
        PreparedStatement temporaryStatement = DriverManager.getConnection("jdbc:h2:"+ProjectPaths.H2_DB_LIBRARY.getPath()+"/tempRunners").prepareStatement("RUNSCRIPT FROM ?");
        temporaryStatement.setString(1,filePath);
        temporaryStatement.execute();
        temporaryStatement.close();
        temporaryStatement = DriverManager.getConnection("jdbc:h2:"+ProjectPaths.H2_DB_LIBRARY.getPath()+"/tempRunners").prepareStatement("SELECT * FROM Runners");
        ResultSet resultSet = temporaryStatement.executeQuery();
        while(resultSet.next()){
            try(PreparedStatement preparedStatement = getConnection().prepareStatement("INSERT INTO Runners (id,first_name,last_name,date_of_birth,gender,email,phone_number) VALUES (NEXT VALUE FOR runners_sequence,?,?,?,?,?,?)")){
                preparedStatement.setString(1,resultSet.getString("first_name"));
                preparedStatement.setString(2,resultSet.getString("last_name"));
                preparedStatement.setDate(3,resultSet.getDate("date_of_birth"));
                preparedStatement.setBoolean(4,resultSet.getBoolean("gender"));
                preparedStatement.setString(5,resultSet.getString("email"));
                preparedStatement.setString(6,resultSet.getString("phone_number"));
                preparedStatement.executeUpdate();
            }catch (SQLException e){
                throw new RuntimeException(e);
            }
        }
        temporaryStatement.close();
        new File(ProjectPaths.H2_DB_LIBRARY.getPath()+"/tempRunners.mv.db").delete();
    }

    public int getNextIdValue() throws SQLException {
        int id = 1;
        PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT NEXT VALUE FOR runners_sequence AS next_value");
        ResultSet resultSet = preparedStatement.executeQuery();
        if(resultSet.next()){
            id = resultSet.getInt("next_value");
        }
        preparedStatement = getConnection().prepareStatement("ALTER SEQUENCE runners_sequence RESTART WITH NEXT VALUE FOR runners_sequence - 1");
        preparedStatement.executeUpdate();
        close(preparedStatement,resultSet);
        return id;
    }

}
