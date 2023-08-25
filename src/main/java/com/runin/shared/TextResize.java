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

import javafx.scene.Node;

public class TextResize {

    public static void resize(Node node){
        String currentStyle = node.getStyle();
        double maxTextWidth = node.getLayoutBounds().getWidth();
        double maxTextHeight = node.getLayoutBounds().getHeight();
        node.setStyle(currentStyle+"-fx-font-size:"+Math.min(maxTextWidth,maxTextHeight)+"px;");
    }

    @SuppressWarnings("unused")
    public static void applyAutoResize(Node node){
        node.layoutBoundsProperty().addListener((observable,oldBound,newBounds)->{
            String currentStyle = node.getStyle();
            double maxTextWidth = newBounds.getWidth();
            double maxTextHeight = newBounds.getHeight();
            node.setStyle(currentStyle+"-fx-font-size:"+Math.min(maxTextWidth,maxTextHeight)+"px;");
        });
    }

}
