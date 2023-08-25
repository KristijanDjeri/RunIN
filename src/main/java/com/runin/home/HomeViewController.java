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

package com.runin.home;

import com.runin.db.EventDAO;
import com.runin.event.EventsDetailedViewController;
import com.runin.record.Event;
import com.runin.shared.*;
import com.runin.stage.StageViewController;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ThreadLocalRandom;

public class HomeViewController implements Initializable {

    @FXML
    private ImageView headerImageView;
    @FXML
    private Label headerLabel,recentEventLabel,upcomingEventLabel,recentEventName,upcomingEventName;
    @FXML
    private TextFlow quoteTextFlow;
    @FXML
    private VBox recentVBox, upcomingVBox;
    @FXML
    private HBox eventHBox;
    @FXML
    private MFXButton recentEventButton,upcomingEventButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        headerImageView.setImage(ImageManager.getIllustration("Jogging-amico.png"));
        Font font = Font.loadFont(getClass().getResourceAsStream("/fonts/AbrilFatface-Regular.ttf"), 80);
        headerLabel.setFont(font);

        Locale locale = Lang.getLocale();
        String quote = null;
        String path = ProjectPaths.CONFIG.getPath()+"/locales/quotes/RunINQuotes_"+locale.getLanguage()+"_"+locale.getCountry()+".txt";
        try {
            if(new File(path).exists()){
                List<String> quotes = Files.readAllLines(Paths.get(path));
                quote = quotes.get(ThreadLocalRandom.current().nextInt(quotes.size()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if(quote!=null&&!quote.isEmpty()) {
            Text text = new Text();
            text.setTextAlignment(TextAlignment.CENTER);
            text.setStyle("-fx-font-size:40;");
            text.setText(quote);
            quoteTextFlow.getChildren().add(text);
        }

        Event recentEvent,upcomingEvent;

        EventDAO eventDAO = new EventDAO();
        try{
            eventDAO.openConnection();
            recentEvent = eventDAO.getRecentEvent();
            upcomingEvent = eventDAO.getUpcomingEvent();
            eventDAO.closeConnection();
        }catch (SQLException e){
            throw new RuntimeException(e);
        }


        if(recentEvent==null&&upcomingEvent==null){
            eventHBox.getChildren().clear();
        }else {
            if (recentEvent != null) {
                recentEventLabel.setText(Lang.get("recentEvent"));
                recentEventButton.setText(Lang.get("viewEvent"));
                recentEventName.setText(recentEvent.name() + "-" + recentEvent.location() + "-" + recentEvent.date());

                recentEventButton.setOnAction(x -> StageViewController.getInstance().setScene(FXMLPaths.EVENT_DETAILED.PATH, new EventsDetailedViewController(recentEvent, Section.HISTORY), Section.HISTORY, Save.NO));

            } else {
                eventHBox.getChildren().remove(recentVBox);
            }
            if (upcomingEvent != null) {
                upcomingEventLabel.setText(Lang.get("upcomingEvent"));
                upcomingEventButton.setText(Lang.get("viewEvent"));
                upcomingEventName.setText(upcomingEvent.name() + "-" + upcomingEvent.location() + "-" + upcomingEvent.date());

                upcomingEventButton.setOnAction(x -> StageViewController.getInstance().setScene(FXMLPaths.EVENT_DETAILED.PATH, new EventsDetailedViewController(upcomingEvent, Section.EVENT), Section.EVENT, Save.NO));

            } else {
                eventHBox.getChildren().remove(upcomingVBox);
            }
        }
    }
}
