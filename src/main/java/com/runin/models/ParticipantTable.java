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

import com.runin.db.DistanceDAO;
import com.runin.db.ParticipantDAO;
import com.runin.db.ResultDAO;
import com.runin.model_managers.ParticipantTableManager;
import com.runin.record.Participant;
import com.runin.shared.DialogController;
import com.runin.shared.ImageManager;
import com.runin.shared.Lang;
import com.runin.stage.StageViewController;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.beans.property.*;
import java.sql.SQLException;

@SuppressWarnings("unused")
public class ParticipantTable {

    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty firstName = new SimpleStringProperty("");
    private final StringProperty lastName = new SimpleStringProperty("");
    private final StringProperty name = new SimpleStringProperty("");
    private final IntegerProperty age = new SimpleIntegerProperty();
    private final DoubleProperty distance = new SimpleDoubleProperty();
    private final IntegerProperty runnerId = new SimpleIntegerProperty();
    private final ObjectProperty<MFXButton> removeButton = new SimpleObjectProperty<>();

    private final int eventId;
    private final Participant participant;

    public ParticipantTable(int eventId,Participant participant){
        this.eventId = eventId;
        this.participant = participant;
        setId(participant.id());
        setFirstName(participant.first_name());
        setLastName(participant.last_name());
        setName(participant.first_name()+" "+participant.last_name());
        setAge(participant.age());
        setRunnerId(participant.runner_id());
        try{
            DistanceDAO distanceDAO = new DistanceDAO(eventId);
            distanceDAO.openConnection();
            setDistance(distanceDAO.get(participant.distance_id()).distance_in_km());
            distanceDAO.closeConnection();
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
        setButton();
    }

    public Participant getParticipant(){
        return participant;
    }

    private void setButton(){
        MFXButton button = new MFXButton("",ImageManager.getImageView(ImageManager.getIcon("delete"),30));
        button.setStyle("-fx-background-color:transparent;");
        button.setOnAction(e->{
            DialogController dialogController = new DialogController(StageViewController.getStage());
            dialogController.setContentText(Lang.get("dialogPT1")+" ["+getFirstName()+" "+getLastName()+"]");
            dialogController.getSecondaryButton().setText(Lang.get("cancel"));
            dialogController.getPrimaryButton().setText(Lang.get("continue"));

            dialogController.showWarning(Lang.get("warning"));

            dialogController.addAction(dialogController.getSecondaryButton(),evt->dialogController.close());
            dialogController.addAction(dialogController.getPrimaryButton(),evt->{
                try{
                    ParticipantDAO participantDAO = new ParticipantDAO(eventId);
                    participantDAO.openConnection();
                    participantDAO.delete(getId());
                    participantDAO.closeConnection();

                    ResultDAO resultDAO = new ResultDAO(eventId);
                    resultDAO.openConnection();
                    if(resultDAO.participantExists(getParticipant().id())){
                        resultDAO.delete(resultDAO.getIDFromParticipant(getParticipant().id()));
                    }
                    resultDAO.closeConnection();
                }catch (SQLException e1){
                    throw new RuntimeException(e1);
                }
                ParticipantTableManager.getInstance().remove(this);
                dialogController.close();
            });

        });
        button.buttonTypeProperty().set(io.github.palexdev.materialfx.enums.ButtonType.RAISED);
        setRemoveButton(button);
        getRemoveButton().setVisible(false);
    }

    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public String getFirstName() {
        return firstName.get();
    }

    public void setFirstName(String firstName) {
        this.firstName.set(firstName);
    }

    public String getLastName() {
        return lastName.get();
    }

    public void setLastName(String lastName) {
        this.lastName.set(lastName);
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public int getAge() {
        return age.get();
    }

    public void setAge(int age) {
        this.age.set(age);
    }

    public double getDistance() {
        return distance.get();
    }

    public void setDistance(double distance) {
        this.distance.set(distance);
    }

    public void setRunnerId(int runnerId) {
        this.runnerId.set(runnerId);
    }

    public MFXButton getRemoveButton() {
        return removeButton.get();
    }

    public void setRemoveButton(MFXButton removeButton) {
        this.removeButton.set(removeButton);
    }
}
