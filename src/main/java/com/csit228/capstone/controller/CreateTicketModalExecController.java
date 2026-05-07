package com.csit228.capstone.controller;

import com.csit228.capstone.dao.DepartmentDAO;
import com.csit228.capstone.dao.TicketDAO;
import com.csit228.capstone.model.Department;
import com.csit228.capstone.model.Ticket;
import com.csit228.capstone.model.TicketPriority;
import com.csit228.capstone.model.TicketStatus;
import com.csit228.capstone.model.User;
import com.csit228.capstone.utils.AppSession;
import com.csit228.capstone.utils.FormValidator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Objects;

public class CreateTicketModalExecController {

    private static final int VOLUNTEER_DEPARTMENT_ID = 0;
    private static final String VOLUNTEER_DEPARTMENT_NAME = "Volunteer";

    private boolean submitted;

    @FXML private Button buttonClose;
    @FXML private Button buttonCancel;
    @FXML private Button buttonCreateTicket;

    @FXML private ComboBox<Department> comboBoxDepartment;
    @FXML private ComboBox<TicketPriority> comboBoxPriority;
    @FXML private ComboBox<String> comboBoxHour;
    @FXML private ComboBox<String> comboBoxMinute;
    @FXML private ComboBox<String> comboBoxAmPm;

    @FXML private TextField textfieldTicketTitle;
    @FXML private TextArea textAreaTicketDesc;
    @FXML private DatePicker datePickerDeadline;

    private final TicketDAO ticketDAO = TicketDAO.getTicketDAO();
    private final User currentUser = AppSession.currentUser;

    @FXML
    public void initialize() {
        populateDepartmentComboBox();
        populatePriorityComboBox();
        populateHourComboBox();
        populateMinuteComboBox();
        populateAmPmComboBox();
        configureDepartmentDisplay();
        configurePriorityDisplay();
        submitted = false;
    }

    private void populateDepartmentComboBox() {
        DepartmentDAO departmentDAO = DepartmentDAO.getDepartmentDAO();

        ObservableList<Department> departments =
                FXCollections.observableArrayList(departmentDAO.getDepartments());

        Department volunteerDepartment = new Department(
                VOLUNTEER_DEPARTMENT_ID,
                VOLUNTEER_DEPARTMENT_NAME,
                "Tickets available for any member to volunteer"
        );

        departments.add(0, volunteerDepartment);

        comboBoxDepartment.setItems(departments);
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

        for (int i = 0; i < 60; i++) {
            minutes.add(String.format("%02d", i));
        }

        comboBoxMinute.setItems(minutes);
    }

    private void populateAmPmComboBox() {
        ObservableList<String> amPm = FXCollections.observableArrayList("AM", "PM");
        comboBoxAmPm.setItems(amPm);
    }

    private void configureDepartmentDisplay() {
        StringConverter<Department> converter = new StringConverter<>() {
            @Override
            public String toString(Department department) {
                if (department == null) {
                    return "";
                }

                return department.getName();
            }

            @Override
            public Department fromString(String string) {
                return null;
            }
        };

        comboBoxDepartment.setConverter(converter);

        comboBoxDepartment.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Department item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        comboBoxDepartment.setCellFactory(listView -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Department item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
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
        boolean isValidSubmission = FormValidator.validateRequired(
                textfieldTicketTitle,
                textAreaTicketDesc,
                comboBoxDepartment,
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
                comboBoxDepartment,
                comboBoxPriority,
                datePickerDeadline,
                comboBoxHour,
                comboBoxMinute,
                comboBoxAmPm
        );

        String title = textfieldTicketTitle.getText();
        String description = textAreaTicketDesc.getText();
        Department department = comboBoxDepartment.getValue();
        TicketPriority priority = comboBoxPriority.getValue();
        LocalDateTime deadline = getDeadlineDateTime();

        submitTicketForm(title, description, department, priority, deadline);

        if (submitted) {
            System.out.println("Ticket successfully created!");
            closeModal(buttonCreateTicket);
        }
    }

    public boolean isSubmitted() {
        return submitted;
    }

    private void submitTicketForm(
            String title,
            String description,
            Department department,
            TicketPriority priority,
            LocalDateTime deadline
    ) {
        boolean hasValues = (title != null && !title.isBlank())
                && (description != null && !description.isBlank())
                && Objects.nonNull(department)
                && Objects.nonNull(priority)
                && deadline != null
                && currentUser != null;

        if (!hasValues) {
            submitted = false;
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        Integer departmentId = isVolunteerDepartment(department)
                ? null
                : department.getId();

        ticketDAO.createTicket(new Ticket(
                0,
                title,
                description,
                priority,
                deadline,
                TicketStatus.OPEN,
                currentUser.getUserId(),
                null,
                now,
                now,
                departmentId
        ));

        submitted = true;
    }

    private boolean isVolunteerDepartment(Department department) {
        if (department == null) {
            return false;
        }

        return department.getId() == VOLUNTEER_DEPARTMENT_ID
                || department.getName().equalsIgnoreCase(VOLUNTEER_DEPARTMENT_NAME);
    }

    private void closeModal(Button sourceButton) {
        if (sourceButton != null
                && sourceButton.getScene() != null
                && sourceButton.getScene().getWindow() instanceof Stage stage) {
            stage.close();
        }
    }
}
