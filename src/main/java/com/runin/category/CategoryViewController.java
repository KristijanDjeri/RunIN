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

package com.runin.category;

import com.runin.db.CategoryDAO;
import com.runin.event.EventsEditViewController;
import com.runin.model_managers.CategoryTableManager;
import com.runin.record.Category;
import com.runin.record.Event;
import com.runin.shared.DialogController;
import com.runin.shared.FXMLPaths;
import com.runin.shared.Lang;
import com.runin.shared.TextResize;
import com.runin.stage.PopupStageViewController;
import com.runin.stage.StageViewController;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.enums.FloatMode;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class CategoryViewController implements Initializable {

    @FXML
    private MFXButton actionButton,cancelButton;
    @FXML
    private Label addCategoryLabel;
    @FXML
    private VBox vbox;

    private final List<HBox> categories = new ArrayList<>();
    private List<String> names;
    private final CategoryDAO categoryDAO;
    private final PopupStageViewController popupStageViewController;
    private final boolean addView;
    private short minAge=0;
    private final EventsEditViewController eventsEditViewController;

    public CategoryViewController(Event event, boolean edit, EventsEditViewController eventsEditViewController){
        categoryDAO = new CategoryDAO(event.id());
        addView = !edit;
        this.eventsEditViewController = eventsEditViewController;
        popupStageViewController = new PopupStageViewController();
        popupStageViewController.loadScene(FXMLPaths.CATEGORY_EDIT.PATH, this);
        popupStageViewController.setTitle(Lang.get("category"));
        popupStageViewController.show();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        names = new ArrayList<>();

        actionButton.setText((addView)?Lang.get("add"):Lang.get("update"));
        cancelButton.setText(Lang.get("cancel"));
        addCategoryLabel.setText((addView)?Lang.get("addCategory"):Lang.get("updateCategory"));

        TextResize.resize(addCategoryLabel);
        TextResize.resize(actionButton);
        TextResize.resize(cancelButton);

        actionButton.setOnAction(x->insert());

        if(!addView){
            try {
                categoryDAO.openConnection();
                for (Category category : categoryDAO.getAll()) {
                    addHBox();
                    ((MFXTextField)categories.get(categories.size()-1).getChildren().get(0)).setText(category.name());
                    ((MFXTextField)categories.get(categories.size()-1).getChildren().get(1)).setText(String.valueOf(category.aboveAge()));
                    ((MFXTextField)categories.get(categories.size()-1).getChildren().get(2)).setText(String.valueOf(category.belowAge()));
                }
            }catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }else{
            addHBox();
        }
    }

    @FXML
    private void addHBox(){
        if(validateInput()){
            return;
        }

        if(vbox.getChildren().size()>1){
            ((StackPane)vbox.getChildren().get(vbox.getChildren().size()-2)).getChildren().get(0).setDisable(true);
            ((StackPane)vbox.getChildren().get(vbox.getChildren().size()-2)).getChildren().get(1).setVisible(true);
        }

        HBox hbox = new HBox();
        MFXTextField nameField = new MFXTextField();
        nameField.setPromptText(Lang.get("name"));
        nameField.setFont(Font.font(15));
        nameField.setFloatMode(FloatMode.DISABLED);
        nameField.setFont(Font.font("System", FontWeight.BOLD,16));

        MFXTextField ageAbove = new MFXTextField();
        ageAbove.setPromptText(Lang.get("aboveAge"));
        ageAbove.setText(String.valueOf(minAge));
        ageAbove.setEditable(false);
        ageAbove.setFloatMode(FloatMode.DISABLED);
        ageAbove.setFont(Font.font("System", FontWeight.BOLD,16));
        ageAbove.setDisable(true);

        MFXTextField ageBelow = new MFXTextField();
        ageBelow.setPromptText(Lang.get("belowAge"));
        ageBelow.setFloatMode(FloatMode.DISABLED);
        ageBelow.setFont(Font.font("System", FontWeight.BOLD,16));
        ageBelow.setTextFormatter(new TextFormatter<>(change->{
            String newText = change.getControlNewText();
            if(newText.isEmpty()){
                return change;
            }
            if(!newText.matches("\\d*")){
                return null;
            }
            return change;
        }));

        nameField.setOnAction(x->ageBelow.requestFocus());
        ageBelow.setOnAction(x->addHBox());

        hbox.setAlignment(Pos.CENTER);
        hbox.getChildren().addAll(nameField,ageAbove,ageBelow);
        hbox.setSpacing(13);

        MFXButton button = new MFXButton("-");
        button.setFont(Font.font("System",FontWeight.BOLD,25));
        button.setAlignment(Pos.CENTER);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setId("neutralRed");
        button.setOnAction(x->{
            ((StackPane)vbox.getChildren().get(vbox.getChildren().size()-3)).getChildren().get(1).setVisible(false);
            ((StackPane)vbox.getChildren().get(vbox.getChildren().size()-3)).getChildren().get(0).setDisable(false);
            if(vbox.getChildren().size()>3){
                ((StackPane)vbox.getChildren().get(vbox.getChildren().size()-4)).getChildren().get(1).setVisible(true);
            }
            categories.remove(categories.size()-1);
            minAge = Short.parseShort(((MFXTextField)hbox.getChildren().get(1)).getText());
            names.remove(((MFXTextField)hbox.getChildren().get(0)).getText());
            vbox.getChildren().remove(vbox.getChildren().size()-2);
        });
        button.setVisible(false);

        categories.add(hbox);
        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(hbox,button);
        stackPane.setMaxWidth(Double.MAX_VALUE);
        stackPane.setAlignment(Pos.CENTER);
        vbox.getChildren().add(vbox.getChildren().size()-1,stackPane);

        if(vbox.getChildren().size()>3){
            ((StackPane)vbox.getChildren().get(vbox.getChildren().size()-4)).getChildren().get(1).setVisible(false);
        }

    }

    private boolean validateInput(){
        if(!categories.isEmpty()) {
            String belowAgeText = ((MFXTextField)categories.get(categories.size()-1).getChildren().get(2)).getText();
            String name = ((MFXTextField)categories.get(categories.size()-1).getChildren().get(0)).getText();
            DialogController dialogController = new DialogController(StageViewController.getStage());
            dialogController.getPrimaryButton().setText(Lang.get("ok"));
            dialogController.addAction(dialogController.getPrimaryButton(),e->dialogController.close());
            if(belowAgeText.isEmpty()||Integer.parseInt(belowAgeText)>120||Integer.parseInt(belowAgeText)<=minAge){
                dialogController.setContentText(Lang.get("dialogC3")+" [ >"+minAge+" ]\n"+Lang.get("dialogC4")+" [ <121 ]");
                dialogController.showWarning(Lang.get("invalidInput"));
                return true;
            }else if(name.isEmpty()){
                dialogController.setContentText(Lang.get("dialogC2"));
                dialogController.showWarning(Lang.get("invalidInput"));
                return true;
            }else if(names.contains(name)){
                dialogController.setContentText(Lang.get("dialogC1"));
                dialogController.showWarning(Lang.get("invalidInput"));
                return true;
            }else{
                minAge=Short.parseShort(belowAgeText);
                names.add(name);
            }
        }
        return false;
    }

    private void finalValidation(){
        if(minAge<120){
            HBox hbox = new HBox();
            MFXTextField nameField = new MFXTextField();
            nameField.setText("Uncategorized");
            MFXTextField above = new MFXTextField();
            above.setText(String.valueOf(minAge));
            MFXTextField below = new MFXTextField();
            below.setText("120");
            hbox.getChildren().addAll(nameField,above,below);
            categories.add(hbox);
        }
    }

    private void insert(){
        try {
            if(validateInput()){
                return;
            }
            finalValidation();
            categoryDAO.openConnection();
            categoryDAO.deleteAll();
            for (HBox category : categories) {
                String name = ((MFXTextField) category.getChildren().get(0)).getText();
                short above = Short.parseShort(((MFXTextField) category.getChildren().get(1)).getText());
                short below = Short.parseShort(((MFXTextField) category.getChildren().get(2)).getText());
                categoryDAO.insert(new Category((short) 0, name, above, below));
            }
            categoryDAO.closeConnection();
            CategoryTableManager.getInstance().refresh();
            close();
            eventsEditViewController.categoryAction();
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void close(){
        popupStageViewController.close();
    }

}
