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

import com.runin.record.Category;
import com.runin.shared.ConvertResultSet;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class CategoryDAO extends DAO implements IDAO<Category>{

    protected final String URL;

    public CategoryDAO(int eventID){
        URL = LOCATION + "/events/"+eventID+"/db";
        try {
            checkTableValidity();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getURL(){
        return URL;
    }

    @Override
    public List<Category> getAll() throws SQLException {
        List<Category> categoryList = new ArrayList<>();
        PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM Categories");
        ResultSet resultSet = preparedStatement.executeQuery();
        ConvertResultSet convertResultSet = new ConvertResultSet();
        while(resultSet.next()){
            categoryList.add(convertResultSet.toCategoryModel(resultSet));
        }
        close(preparedStatement,resultSet);
        return categoryList;
    }

    @Override
    public Category get(int id) throws SQLException {
        PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM Categories WHERE id=?");
        preparedStatement.setInt(1,id);
        ResultSet resultSet = preparedStatement.executeQuery();
        Category category=null;
        if(resultSet.next()) {
            category = new ConvertResultSet().toCategoryModel(resultSet);
        }
        close(preparedStatement,resultSet);
        return category;
    }

    @Override
    public void insert(Category model) throws SQLException {
        PreparedStatement preparedStatement = getConnection().prepareStatement("INSERT INTO Categories(name,above_age,below_age) VALUES (?,?,?)");
        preparedStatement.setString(1,model.name());
        preparedStatement.setInt(2,model.aboveAge());
        preparedStatement.setInt(3,model.belowAge());
        preparedStatement.executeUpdate();
        close(preparedStatement,null);
    }

    @Override
    public void update(Category model) throws SQLException {
        PreparedStatement preparedStatement = getConnection().prepareStatement("UPDATE Categories SET name=?,above_age=?,below_age=? WHERE id=?");
        preparedStatement.setString(1, model.name());
        preparedStatement.setInt(2,model.aboveAge());
        preparedStatement.setInt(3,model.belowAge());
        preparedStatement.setInt(4,model.id());
        preparedStatement.executeUpdate();
        close(preparedStatement,null);
    }

    @Override
    public void delete(int id) throws SQLException {
        PreparedStatement preparedStatement = getConnection().prepareStatement("DELETE FROM Categories WHERE id=?");
        preparedStatement.setInt(1,id);
        preparedStatement.executeUpdate();
        close(preparedStatement,null);
    }

    @Override
    public void checkTableValidity() throws SQLException {
        openConnection();
        (getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS Categories(id INT AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(30) UNIQUE, above_age INT NOT NULL, below_age INT NOT NULL)")).executeUpdate();
        closeConnection();
    }

    public void deleteAll() throws SQLException {
        PreparedStatement preparedStatement = getConnection().prepareStatement("TRUNCATE TABLE Categories");
        preparedStatement.executeUpdate();
        preparedStatement = getConnection().prepareStatement("ALTER TABLE Categories ALTER COLUMN id RESTART WITH 1");
        preparedStatement.executeUpdate();
        close(preparedStatement,null);
    }

}
