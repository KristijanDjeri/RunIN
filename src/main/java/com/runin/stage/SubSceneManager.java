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

package com.runin.stage;

import com.runin.event.EventsMainViewController;
import com.runin.history.HistoryViewController;
import com.runin.home.HomeViewController;
import com.runin.runner.RunnersMainViewController;
import com.runin.settings.SettingsViewController;
import com.runin.shared.FXMLPaths;
import com.runin.shared.Save;
import com.runin.shared.Section;
import javafx.fxml.FXMLLoader;
import javafx.scene.SubScene;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class SubSceneManager {

    private final ArrayList<SubScene> sectionList;
    private final HashMap<String,SubScene> savedSubScenes;

    SubSceneManager(){
        sectionList = new ArrayList<>();
        savedSubScenes = new HashMap<>();
        insertSections();
    }

    private void insertSections(){
        sectionList.add(null);
        sectionList.add(createSubScene(FXMLPaths.HOME.PATH, new HomeViewController()));
        sectionList.add(createSubScene(FXMLPaths.TABLE_VIEW.PATH, new RunnersMainViewController()));
        sectionList.add(createSubScene(FXMLPaths.TABLE_VIEW.PATH, new EventsMainViewController()));
        sectionList.add(createSubScene(FXMLPaths.TABLE_VIEW.PATH, new HistoryViewController()));
        sectionList.add(createSubScene(FXMLPaths.SETTINGS.PATH, new SettingsViewController()));
    }

    public SubScene loadSection(Section section){
        return sectionList.get(section.ID);
    }

    public SubScene processRequest(String fxmlLocation, Object controller, Section section, Save save){
        if(!savedSubScenes.containsKey(fxmlLocation)){
            if(save == Save.YES){
                saveSubScene(fxmlLocation,createSubScene(fxmlLocation,controller));
                sectionList.set(section.ID,loadSubScene(fxmlLocation));
            }else {
                SubScene subScene = createSubScene(fxmlLocation, controller);
                sectionList.set(section.ID,subScene);
                return subScene;
            }
        }
        return loadSubScene(fxmlLocation);
    }

    private SubScene createSubScene(String fxmlLocation, Object controller){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/runin/"+fxmlLocation));
            if(controller!=null){
                loader.setController(controller);
            }
            return new SubScene(loader.load(),0,0);
        }catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    private SubScene loadSubScene(String fxmlLocation){
        return savedSubScenes.get(fxmlLocation);
    }

    private void saveSubScene(String fxmlLocation, SubScene subScene){
        savedSubScenes.put(fxmlLocation,subScene);
    }

}
