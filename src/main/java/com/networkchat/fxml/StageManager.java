package com.networkchat.fxml;

import com.networkchat.client.ClientSocket;
import com.networkchat.resources.FxmlView;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class StageManager {
    private final Stage primaryStage;
    private FXMLLoader fxmlLoader;

    private int pX;
    private int pY;

    public void setX(int x) {
        this.pX = x;
    }

    public void setY(int y) {
        this.pY = y;
    }

    public int getX() {
        return pX;
    }

    public int getY() {
        return pY;
    }

    public StageManager(Stage stage, FxmlView initForm) {
        this.primaryStage = stage;
        this.primaryStage.setScene(new Scene(initForm.getRoot()));
    }

    public void switchScene(FxmlView formType, ClientSocket socket) {
        this.fxmlLoader = formType.getFxmlLoader();
        show(formType.getRoot(), socket);
    }

    private void show(Parent root, ClientSocket socket) {
        Scene scene = this.primaryStage.getScene();
        scene.setRoot(root);
        scene.getWindow().sizeToScene();
        this.primaryStage.centerOnScreen();
        this.primaryStage.setScene(scene);
        initController(socket);
        this.primaryStage.show();
    }

    private void initController(ClientSocket socket) {
        Controllable controller = this.fxmlLoader.getController();
        controller.setStage(this.primaryStage);
        controller.setStageManager(this);
        controller.setSocket(socket);
        controller.init();
    }

    public void onFormDragEntered(MouseEvent event) {
        this.primaryStage.setX(event.getScreenX() + this.getX());
        this.primaryStage.setY(event.getScreenY() + this.getY());
    }

    public void onMousePressed(MouseEvent event) {
        this.setX((int)(this.primaryStage.getX() - event.getScreenX()));
        this.setY((int)(this.primaryStage.getY() - event.getScreenY()));
    }
}
