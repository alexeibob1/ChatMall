package com.networkchat.tooltips;

import com.networkchat.resources.ImageResources;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

public class EmailTooltip {
    private static final String info = """
            This E-mail is already in use or invalid.
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
