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

package com.runin.race;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.util.Duration;
import java.time.LocalTime;

public class Stopwatch {

    private long startTime;
    private final Timeline timeline;

    protected Label timeLabel;
    public String time;

    public Stopwatch(LocalTime duration, Label stopwatchLabel, RaceViewController raceViewController){
        timeLabel = stopwatchLabel;
        timeline = new Timeline(new KeyFrame(Duration.seconds(1),event->{
            long elapsedTime = System.currentTimeMillis()-startTime;
            updateTimeLabel(elapsedTime);
        }));
        if(duration==null) {
            timeline.setCycleCount(Animation.INDEFINITE);
        }else{
            timeline.setCycleCount(duration.toSecondOfDay());
        }
        timeline.setOnFinished(e-> raceViewController.endEvent());
    }

    public boolean isRunning(){
        return timeline.getStatus() == Animation.Status.RUNNING;
    }

    private void updateTimeLabel(long elapsedTime){
        Platform.runLater(()->{
            int seconds = (int)(elapsedTime/1000)%60;
            int minutes = (int)(elapsedTime/(1000*60))%60;
            int hours = (int)(elapsedTime/(1000*60*60))%24;
            String time = String.format("%02d:%02d:%02d",hours,minutes,seconds);
            this.time = time;
            timeLabel.setText(time);
        });
    }

    public void start(){
        if(!isRunning()) {
            startTime = System.currentTimeMillis();
            timeline.play();
        }
    }

    public void stop(){
        timeline.stop();
    }

}
