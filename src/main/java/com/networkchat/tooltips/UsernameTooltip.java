package com.networkchat.tooltips;

import com.networkchat.resources.ImageResources;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

public class UsernameTooltip {
    private static final String info = """
            User with specified name already exists.
            Please, enter another one.
            """;

    public static Tooltip getTooltip() {
        Tooltip tooltip = new Tooltip();
        tooltip.setText(info);
        ImageView imageView = new ImageView(ImageResources.ERROR_ICON.getImage());
        imageView.setFitWidth(25);
        imageView.setFitHeight(25);
        tooltip.setGraphic(imageView);
        tooltip.setShowDelay(Duration.seconds(0.2));
        return tooltip;
    }
}
