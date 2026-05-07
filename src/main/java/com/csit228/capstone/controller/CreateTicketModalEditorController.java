package com.csit228.capstone.controller;

import com.csit228.capstone.dao.DepartmentDAO;
import com.csit228.capstone.dao.TicketDAO;
import com.csit228.capstone.model.*;
import com.csit228.capstone.utils.AppSession;
import com.csit228.capstone.utils.FormValidator;
import javafx.fxml.FXML;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Objects;
import javafx.util.StringConverter;

public class CreateTicketModalEditorController {

    private boolean submitted;

    @FXML private Button buttonClose;
    @FXML private Button buttonCancel;
    @FXML private Button buttonCreateTicket;

    @FXML private ComboBox<TicketPriority> comboBoxPriority;
    @FXML private ComboBox<String> comboBoxHour;
    @FXML private ComboBox<String> comboBoxMinute;
    @FXML private ComboBox<String> comboBoxAmPm;

    @FXML private TextField textfieldDept;
    @FXML private TextField textfieldTicketTitle;
    @FXML private TextArea textAreaTicketDesc;

    @FXML private DatePicker datePickerDeadline;

    private final TicketDAO ticketDAO = TicketDAO.getTicketDAO();
    private final  DepartmentDAO departmentDAO = DepartmentDAO.getDepartmentDAO();
    private final User user = AppSession.currentUser;

    @FXML
    public void initialize() {
        setTextFields();
        populatePriorityComboBox();
        populateHourComboBox();
        populateMinuteComboBox();
        populateAmPmComboBox();
        configurePriorityDisplay();
        submitted = false;
    }

    private void setTextFields() {
        Department currDept = departmentDAO.getDepartmentByID(user.getDepartment_id());

        if (currDept == null) {
            return;
        }
        textfieldDept.setText(currDept.getName());
    }

    private void populatePriorityComboBox() {
        comboBoxPriority.getItems().setAll(Arrays.asList(TicketPriority.values()));
    }

    private void populateHourComboBox() {
        ObservableList<String> hours = FXCollections.observableArrayList();
        for (int i = 1; i <= 12; i++) {
            hours.add(String.format("%02d", i));
        }
        comboBoxHour.setItems(hours);
    }

    private void populateMinuteComboBox() {
        ObservableList<String> minutes = FXCollections.observableArrayList();
        for (int i = 0; i <= 60; i++) {
            minutes.add(String.format("%02d", i));
        }
        comboBoxMinute.setItems(minutes);
    }

    private void populateAmPmComboBox() {
        ObservableList<String> amPm = FXCollections.observableArrayList("AM", "PM");
        comboBoxAmPm.setItems(amPm);
    }

    private void configurePriorityDisplay() {
        StringConverter<TicketPriority> converter = new StringConverter<>() {
            @Override
            public String toString(TicketPriority priority) {
                if (priority == null) {
                    return "";
                }

                String name = priority.name().toLowerCase();
                return Character.toUpperCase(name.charAt(0)) + name.substring(1);
            }

            @Override
            public TicketPriority fromString(String string) {
                if (string == null || string.isBlank()) {
                    return null;
                }

                return TicketPriority.valueOf(string.trim().toUpperCase());
            }
        };

        comboBoxPriority.setConverter(converter);
        comboBoxPriority.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(TicketPriority item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : converter.toString(item));
            }
        });
        comboBoxPriority.setCellFactory(listView -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(TicketPriority item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : converter.toString(item));
            }
        });
    }

    private LocalDateTime getDeadlineDateTime() {
        LocalDate date = datePickerDeadline.getValue();
        String hourStr = comboBoxHour.getValue();
        String minuteStr = comboBoxMinute.getValue();
        String amPmStr = comboBoxAmPm.getValue();

        if (date == null || hourStr == null || minuteStr == null || amPmStr == null) {
            return null;
        }

        int hour = Integer.parseInt(hourStr);
        int minute = Integer.parseInt(minuteStr);

        if (amPmStr.equals("PM") && hour != 12) {
            hour += 12;
        } else if (amPmStr.equals("AM") && hour == 12) {
            hour = 0;
        }

        return LocalDateTime.of(date, LocalTime.of(hour, minute));
    }


    @FXML
    public void onClickedButtonClose() {
        closeModal(buttonClose);
    }

    @FXML
    public void onClickedCancel() {
        closeModal(buttonCancel);
    }

    @FXML
    public void onClickedCreateTicket() {
        String title = textfieldTicketTitle != null ? textfieldTicketTitle.getText() : "";
        String description = textAreaTicketDesc != null ? textAreaTicketDesc.getText() : "";
        Department department = departmentDAO.getDepartmentByID(user.getDepartment_id()) != null ?  departmentDAO.getDepartmentByID(user.getDepartment_id()) : null;
        TicketPriority priority = comboBoxPriority != null ? comboBoxPriority.getValue() : null;
        LocalDateTime deadline = getDeadlineDateTime() != null ? getDeadlineDateTime() : null;

        submitTicketForm(title, description, department, priority, deadline);

        boolean isValidSubmission = FormValidator.validateRequired(
                textfieldTicketTitle,
                textAreaTicketDesc,
                comboBoxPriority,
                datePickerDeadline,
                comboBoxHour,
                comboBoxMinute,
                comboBoxAmPm
        );

        if (!isValidSubmission) {
            System.out.println("Please fill all required fields!");
            return;
        }

        FormValidator.clearErrors(
                textfieldTicketTitle,
                textAreaTicketDesc,
                comboBoxPriority,
                datePickerDeadline,
                comboBoxHour,
                comboBoxMinute,
                comboBoxAmPm
        );

        System.out.println("Ticket successfully created!");
        closeModal(buttonCreateTicket);
    }

    public boolean isSubmitted() {
        return submitted;
    }

    private void submitTicketForm(String title, String description, Department department, TicketPriority priority, LocalDateTime deadline) {
        boolean hasValues = (title != null && !title.isBlank())
                && (description != null && !description.isBlank())
                && Objects.nonNull(department)
                && Objects.nonNull(priority)
                && deadline != null;

        if (hasValues) {
            LocalDateTime now = LocalDateTime.now();

            ticketDAO.createTicket(new Ticket(
                    0,
                    title,
                    description,
                    priority,
                    deadline,
                    TicketStatus.OPEN,
                    user.getUserId(),
                    null,
                    now,
                    now,
                    department.getId()
            ));

            submitted = true;
        }
    }

    private void closeModal(Button sourceButton) {
        if (sourceButton != null
                && sourceButton.getScene() != null
                && sourceButton.getScene().getWindow() instanceof Stage stage) {
            stage.close();
        }
    }

}
