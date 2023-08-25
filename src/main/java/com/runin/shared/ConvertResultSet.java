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

import com.runin.record.*;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ConvertResultSet {

    public Category toCategoryModel(ResultSet resultSet) throws SQLException {
        return new Category(resultSet.getShort("id"),
                resultSet.getString("name"),
                resultSet.getShort("above_age"),
                resultSet.getShort("below_age"));
    }

    public Distance toDistanceModel(ResultSet resultSet) throws SQLException {
        return new Distance(resultSet.getShort("id"),
                resultSet.getString("name"),
                resultSet.getDouble("distance_in_km"));
    }

    public Participant toParticipantModel(ResultSet resultSet) throws SQLException {
        return new Participant(resultSet.getInt("id"),
                resultSet.getString("first_name"),
                resultSet.getString("last_name"),
                resultSet.getInt("age"),
                resultSet.getBoolean("gender"),
                resultSet.getShort("distance_id"),
                resultSet.getShort("category_id"),
                resultSet.getInt("runner_id"));
    }

    public Result toResultModel(ResultSet resultSet) throws SQLException {
        return new Result(resultSet.getInt("id"),
                resultSet.getInt("participant_id"),
                resultSet.getBoolean("gender"),
                resultSet.getShort("distance_id"),
                resultSet.getShort("category_id"),
                resultSet.getString("finish_time"));
    }

    public Event toEventModel(ResultSet resultSet) throws SQLException {
        return new Event(resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("location"),
                resultSet.getDate("date"),
                resultSet.getBoolean("finished"),
                resultSet.getBoolean("editable_results"));
    }

    public Runner toRunnerModel(ResultSet resultSet) throws  SQLException{
        return new Runner(resultSet.getInt("id"),
                resultSet.getString("first_name"),
                resultSet.getString("last_name"),
                resultSet.getDate("date_of_birth"),
                resultSet.getBoolean("gender"),
                resultSet.getString("email"),
                resultSet.getString("phone_number"));
    }

}
