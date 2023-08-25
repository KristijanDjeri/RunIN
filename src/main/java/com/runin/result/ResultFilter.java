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

package com.runin.result;

public class ResultFilter {

    private short categoryID = 0;
    private short distanceID = 0;
    private short gender = 0;

    public ResultFilter(short categoryID,short distanceID, short gender){
        setCategoryID(categoryID);
        setDistanceID(distanceID);
        setGender(gender);
    }

    public short getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(short categoryID) {
        this.categoryID = categoryID;
    }

    public short getDistanceID() {
        return distanceID;
    }

    public void setDistanceID(short distanceID) {
        this.distanceID = distanceID;
    }

    public short getGender() {
        return gender;
    }

    public void setGender(short gender) {
        this.gender = gender;
    }
}
