module com.csit228.capstone {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    exports com.csit228.capstone;
    exports com.csit228.capstone.controller;
    opens com.csit228.capstone.controller to javafx.fxml;
}