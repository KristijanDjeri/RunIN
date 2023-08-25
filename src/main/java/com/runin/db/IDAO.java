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

import java.sql.SQLException;
import java.util.List;

public interface IDAO <T>{

    List<T> getAll() throws SQLException;
    T get(int id) throws SQLException;
    void insert(T model) throws SQLException;
    void update(T model) throws SQLException;
    void delete(int id) throws SQLException;
    void checkTableValidity() throws SQLException;

}
