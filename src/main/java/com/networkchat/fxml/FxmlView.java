package com.networkchat.fxml;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;

public enum FxmlView {
    LOGIN {
        private static final FXMLLoader fxmlLoader = new FXMLLoader(StageManager.class.getResource("/com/networkchat/fxml/login-form.fxml"));
        private static final Parent root;

        static {
            try {
                root = fxmlLoader.load();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        Parent getRoot() {
            return this.root;
        }
        @Override
        String getFxmlFile() {
            return "/com/networkchat/fxml/login-form.fxml";
        }

        @Override
        Class<?> getControllerClass() {
            return LoginController.class;
        }

        @Override
        FXMLLoader getFxmlLoader() {
            return fxmlLoader;
        }
    },
    REGISTRATION {
        private static final FXMLLoader fxmlLoader = new FXMLLoader(StageManager.class.getResource("/com/networkchat/fxml/registration-form.fxml"));
        private static final Parent root;

        static {
            try {
                root = fxmlLoader.load();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        String getFxmlFile() {
            return "/com/networkchat/fxml/registration-form.fxml";
        }

        @Override
        Class<?> getControllerClass() {
            return RegistrationController.class;
        }

        @Override
        FXMLLoader getFxmlLoader() {
            return fxmlLoader;
        }

        @Override
        Parent getRoot() {
            return this.root;
        }
    };

    abstract String getFxmlFile();

    abstract Class<?> getControllerClass();

    abstract FXMLLoader getFxmlLoader();

    abstract Parent getRoot();
}
