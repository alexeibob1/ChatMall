package com.networkchat.resources;

import com.networkchat.tooltips.UsernameTooltip;
import javafx.scene.image.Image;

public enum ImageResources {

    ERROR_ICON {
        private static final Image img = new Image(UsernameTooltip.class.getResourceAsStream("/com/networkchat/icons/error.png"));
        @Override
        public Image getImage() {
            return img;
        }
    };

    public abstract Image getImage();
}
