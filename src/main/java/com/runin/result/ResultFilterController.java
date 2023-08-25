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

import com.runin.db.CategoryDAO;
import com.runin.db.DistanceDAO;
import com.runin.record.Category;
import com.runin.record.Distance;
import com.runin.record.Event;
import com.runin.shared.FXMLPaths;
import com.runin.shared.Lang;
import com.runin.shared.TextResize;
import com.runin.stage.PopupStageViewController;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class ResultFilterController implements Initializable {

    @FXML
    private MFXComboBox<String> categoryComboBox,distanceComboBox,genderComboBox;
    @FXML
    private Label filterLabel;
    @FXML
    private MFXButton closeButton,applyButton;

    private HashMap<String,Short> categories,distances,genders;
    private final PopupStageViewController popupStageViewController;
    private ResultFilter resultFilter;
    private final Event event;

    public ResultFilterController(Event event){
        this.event = event;
        popupStageViewController = new PopupStageViewController();
        popupStageViewController.setTitle("Filter");
        popupStageViewController.loadScene(FXMLPaths.RESULT_FILTER.PATH, this);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        categories = new HashMap<>();
        distances = new HashMap<>();
        genders = new HashMap<>();

        categories.put(Lang.get("all"),(short)0);
        distances.put(Lang.get("all"),(short)0);

        categoryComboBox.getItems().add(Lang.get("all"));
        distanceComboBox.getItems().add(Lang.get("all"));

        try{
            CategoryDAO categoryDAO = new CategoryDAO(event.id());
            categoryDAO.openConnection();
            for(Category category: categoryDAO.getAll()){
                categories.put(category.name(), category.id());
                categoryComboBox.getItems().add(category.name());
            }
            categoryDAO.closeConnection();
        }catch (SQLException e){
            throw new RuntimeException(e);
        }

        try{
            DistanceDAO distanceDAO = new DistanceDAO(event.id());
            distanceDAO.openConnection();
            for(Distance distance:distanceDAO.getAll()){
                distances.put(distance.name(), distance.id());
                distanceComboBox.getItems().add(distance.name());
            }
            distanceDAO.closeConnection();
        }catch (SQLException e){
            throw new RuntimeException(e);
        }

        genders.put(Lang.get("both"),(short)0);
        genders.put(Lang.get("male"),(short)1);
        genders.put(Lang.get("female"),(short)2);
        genderComboBox.getItems().addAll(genders.keySet());

        loadLang();

    }

    public ResultFilter showFilter(ResultFilter resultFilter){
        this.resultFilter = resultFilter;

        if(categories.containsValue(resultFilter.getCategoryID())){
            categoryComboBox.getSelectionModel().selectItem(getKey(categories,resultFilter.getCategoryID()));
        }else{
            categoryComboBox.getSelectionModel().clearSelection();
        }

        if(distances.containsValue(resultFilter.getDistanceID())){
            distanceComboBox.getSelectionModel().selectItem(getKey(distances,resultFilter.getDistanceID()));
        }else{
            distanceComboBox.getSelectionModel().clearSelection();
        }

        if(genders.containsValue(resultFilter.getGender())){
            genderComboBox.getSelectionModel().selectItem(getKey(genders, resultFilter.getGender()));
        }else{
            genderComboBox.getSelectionModel().clearSelection();
        }

        popupStageViewController.getStage().showAndWait();
        return this.resultFilter;
    }

    public static <K, V> K getKey(HashMap<K, V> map, V value) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return null;
    }

    @FXML
    private void apply(){

        if(categoryComboBox.getSelectionModel().getSelectedItem()!=null){
            resultFilter.setCategoryID(categories.get(categoryComboBox.getSelectionModel().getSelectedItem()));
        }else{
            resultFilter.setCategoryID((short)0);
        }

        if(distanceComboBox.getSelectionModel().getSelectedItem()!=null){
            resultFilter.setDistanceID(distances.get(distanceComboBox.getSelectionModel().getSelectedItem()));
        }else{
            resultFilter.setDistanceID((short)0);
        }

        if(genderComboBox.getSelectionModel().getSelectedItem()!=null){
            resultFilter.setGender(genders.get(genderComboBox.getSelectionModel().getSelectedItem()));
        }else{
            resultFilter.setGender((short)0);
        }

        close();
    }

    @FXML
    private void close(){
        popupStageViewController.close();
    }

    private void loadLang(){
        filterLabel.setText(Lang.get("filter"));
        categoryComboBox.setFloatingText(Lang.get("category"));
        distanceComboBox.setFloatingText(Lang.get("distance"));
        genderComboBox.setFloatingText(Lang.get("gender"));
        closeButton.setText(Lang.get("close"));
        applyButton.setText(Lang.get("apply"));

        TextResize.resize(filterLabel);
        TextResize.resize(applyButton);
        TextResize.resize(closeButton);

    }

}
