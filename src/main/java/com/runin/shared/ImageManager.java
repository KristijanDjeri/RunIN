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

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ImageManager {

    public static Image getIcon(String icon){
        return new Image("file:"+ProjectPaths.ASSETS.getPath()+"/icons/"+icon+".png");
    }

    public static ImageView getImageView(Image image,int r){
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(r);
        imageView.setFitWidth(r);
        return imageView;
    }

    public static Image getIllustration(String illustration){
        return new Image("file:"+ProjectPaths.ASSETS.getPath()+"/illustrations/"+illustration);
    }

    public static Image getImageFromSystem(String path){
        return new Image("file:"+path);
    }

}
