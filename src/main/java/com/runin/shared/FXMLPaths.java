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

public enum FXMLPaths {

    HOME("home/home"),
    RUNNER_ADD("runner/runnerAdd"),
    RUNNER_PROFILE("runner/runnerProfile"),
    EVENT_ADD("event/eventAdd"),
    EVENT_DETAILED("event/eventDetailed"),
    EVENT_EDIT("event/eventEdit"),
    PARTICIPANT_ADD_RUNNERS("participant/participantAddRunners"),
    PARTICIPANT_EDIT("participant/participantEdit"),
    DISTANCE_EDIT("distance/distanceEdit"),
    CATEGORY_EDIT("category/categoryEdit"),
    RACE_MAIN("race/race"),
    RACE_SETUP("race/raceSetup"),
    TABLE_VIEW("stage/tableView"),
    FILE_TYPE_SELECTION("stage/fileTypeSelection"),
    TEXT_INPUT("stage/textInput"),
    RESULT_FILTER("result/resultFilter"),
    SETTINGS("settings/settings"),
    DATABASE_CHOOSER("stage/databaseChooser"),
    CREDITS("settings/credits")
    ;

    public final String PATH;

    FXMLPaths(String path){
        this.PATH = path+".fxml";
    }

}
