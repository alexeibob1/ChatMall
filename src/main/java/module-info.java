module com.networkchat {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.mail;
    requires java.sql;
    requires mysql.connector.j;


    opens com.networkchat to javafx.fxml;
    exports com.networkchat;
    exports com.networkchat.fxml;
    opens com.networkchat.fxml to javafx.fxml;
}