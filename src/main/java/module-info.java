module com.csit228.capstone {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires java.sql;
    requires java.desktop;
    requires mysql.connector.j;

    exports com.csit228.capstone.application;
    exports com.csit228.capstone.controller;
    exports com.csit228.capstone.model;
    exports com.csit228.capstone.utils;
    exports com.csit228.capstone.dao;

    opens com.csit228.capstone.application to javafx.graphics, javafx.fxml;
    opens com.csit228.capstone.controller to javafx.fxml;
    opens com.csit228.capstone.model to javafx.base, javafx.fxml;
}