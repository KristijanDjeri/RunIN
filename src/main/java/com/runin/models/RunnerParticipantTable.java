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

import io.github.palexdev.materialfx.controls.MFXCheckbox;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

@SuppressWarnings("unused")
public class RunnerParticipantTable {

    private final ObjectProperty<MFXCheckbox> checkBox = new SimpleObjectProperty<>();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty distance = new SimpleStringProperty();

    public RunnerParticipantTable(String name) {
        setName(name);
        setCheckBox(new MFXCheckbox());
        setDistance("");
    }

    public MFXCheckbox getCheckBox() {
        return checkBox.get();
    }

    public void setCheckBox(MFXCheckbox checkBox) {
        this.checkBox.set(checkBox);
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getDistance() {
        return distance.get();
    }

    public void setDistance(String distance) {
        this.distance.set(distance);
    }

    public ObjectProperty<MFXCheckbox> checkBoxProperty() {
        return checkBox;
    }

    public StringProperty nameProperty() {
        return name;
    }

    public StringProperty distanceProperty() {
        return distance;
    }
}
