package com.networkchat.resources;

import com.networkchat.fxml.StageManager;
import com.networkchat.login.LoginController;
import com.networkchat.registration.ConfirmationController;
import com.networkchat.registration.RegistrationController;
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
        public Parent getRoot() {
            return this.root;
        }
        @Override
        public String getFxmlFile() {
            return "/com/networkchat/fxml/login-form.fxml";
        }

        @Override
        public Class<?> getControllerClass() {
            return LoginController.class;
        }

        @Override
        public FXMLLoader getFxmlLoader() {
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
        public String getFxmlFile() {
            return "/com/networkchat/fxml/registration-form.fxml";
        }

        @Override
        public Class<?> getControllerClass() {
            return RegistrationController.class;
        }

        @Override
        public FXMLLoader getFxmlLoader() {
            return fxmlLoader;
        }

        @Override
        public Parent getRoot() {
            return this.root;
        }
    },

    CONFIRMATION {
        private static final FXMLLoader fxmlLoader = new FXMLLoader(StageManager.class.getResource("/com/networkchat/fxml/confirmation-form.fxml"));
        private static final Parent root;

        static {
            try {
                root = fxmlLoader.load();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        public String getFxmlFile() {
            return "/com/networkchat/fxml/confirmation-form.fxml";
        }

        @Override
        public Class<?> getControllerClass() {
            return ConfirmationController.class;
        }

        @Override
        public FXMLLoader getFxmlLoader() {
            return fxmlLoader;
        }

        @Override
        public Parent getRoot() {
            return this.root;
        }
    },

    CHATROOM {
        private static final FXMLLoader fxmlLoader = new FXMLLoader(StageManager.class.getResource("/com/networkchat/fxml/chatroom-form.fxml"));
        private static final Parent root;

        static {
            try {
                root = fxmlLoader.load();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        public String getFxmlFile() {
            return "/com/networkchat/fxml/chatroom-form.fxml";
        }

        @Override
        public Class<?> getControllerClass() {
            return ConfirmationController.class;
        }

        @Override
        public FXMLLoader getFxmlLoader() {
            return fxmlLoader;
        }

        @Override
        public Parent getRoot() {
            return this.root;
        }
    };

    public abstract String getFxmlFile();

    public abstract Class<?> getControllerClass();

    public abstract FXMLLoader getFxmlLoader();

    public abstract Parent getRoot();
}
