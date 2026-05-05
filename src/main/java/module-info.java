module com.csit228.capstone {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires java.desktop;
    requires mysql.connector.j;


    exports com.csit228.capstone.controller;
    opens com.csit228.capstone.application to javafx.graphics, javafx.fxml;
}