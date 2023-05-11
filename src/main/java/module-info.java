module com.networkchat {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.mail;
    requires java.sql;
    requires mysql.connector.j;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;


    opens com.networkchat to javafx.fxml;
    exports com.networkchat;
    exports com.networkchat.fxml;
    opens com.networkchat.fxml to javafx.fxml;
    exports com.networkchat.registration;
    opens com.networkchat.registration to javafx.fxml;
    exports com.networkchat.login;
    opens com.networkchat.login to javafx.fxml;
    exports com.networkchat.resources;
    opens com.networkchat.resources to javafx.fxml;
    exports com.networkchat.packets.client to com.fasterxml.jackson.databind;
}