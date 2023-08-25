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

package com.runin.models;

import com.runin.db.CategoryDAO;
import com.runin.db.DistanceDAO;
import com.runin.db.ParticipantDAO;
import com.runin.record.Category;
import com.runin.record.Event;
import com.runin.record.Participant;
import com.runin.record.Result;
import com.runin.shared.Lang;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.sql.SQLException;

@SuppressWarnings("unused")
public class ResultTable {

    private final int id;
    private final StringProperty participantName = new SimpleStringProperty();
    private final IntegerProperty participantId = new SimpleIntegerProperty();
    private final StringProperty gender = new SimpleStringProperty();
    private final StringProperty distance = new SimpleStringProperty();
    private final StringProperty category = new SimpleStringProperty();
    private final StringProperty finishTime = new SimpleStringProperty();

    private final Result result;

    public ResultTable(Event event, Result result){
        this.result = result;

        //
        try {
            ParticipantDAO participantDAO = new ParticipantDAO(event.id());
            participantDAO.openConnection();
            Participant participant = participantDAO.get(result.participant_id());
            setParticipantName(participant.first_name()+" "+participant.last_name());
            setParticipantId(result.participant_id());
            setGender(participant.gender()? Lang.get("male"):Lang.get("female"));
            participantDAO.closeConnection();

            DistanceDAO distanceDAO = new DistanceDAO(event.id());
            distanceDAO.openConnection();
            setDistance(distanceDAO.get(result.distance_id()).name());
            distanceDAO.closeConnection();

            CategoryDAO categoryDAO = new CategoryDAO(event.id());
            categoryDAO.openConnection();
            Category category1 = categoryDAO.get(result.category_id());
            String categoryName = (category1==null)?"":category1.name();
            setCategory(categoryName);
            categoryDAO.closeConnection();

            setFinishTime((result.finish_time().equals("00:00:00")&&!event.editable_results())?"DNF": result.finish_time());
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
        //

        id = result.id();
    }

    public Result getResult(){
        return result;
    }

    public int getId() {
        return id;
    }

    public String getParticipantName() {
        return participantName.get();
    }

    public void setParticipantName(String participantName) {
        this.participantName.set(participantName);
    }

    public int getParticipantId() {
        return participantId.get();
    }

    public void setParticipantId(int participantId) {
        this.participantId.set(participantId);
    }

    public String getGender() {
        return gender.get();
    }

    public void setGender(String gender) {
        this.gender.set(gender);
    }

    public String getDistance() {
        return distance.get();
    }

    public void setDistance(String distance) {
        this.distance.set(distance);
    }

    public String getCategory() {
        return category.get();
    }

    public void setCategory(String category) {
        this.category.set(category);
    }

    public String getFinishTime() {
        return finishTime.get();
    }

    public void setFinishTime(String finishTime) {
        this.finishTime.set(finishTime);
    }

    public StringProperty participantNameProperty() {
        return participantName;
    }

    public IntegerProperty participantIdProperty() {
        return participantId;
    }

    public StringProperty genderProperty() {
        return gender;
    }

    public StringProperty distanceProperty() {
        return distance;
    }

    public StringProperty categoryProperty() {
        return category;
    }

    public StringProperty finishTimeProperty() {
        return finishTime;
    }
}
