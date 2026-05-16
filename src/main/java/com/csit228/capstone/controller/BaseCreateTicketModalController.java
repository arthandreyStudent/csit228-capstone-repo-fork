package com.csit228.capstone.controller;

import com.csit228.capstone.dao.TicketDAO;
import com.csit228.capstone.enums.TicketPriority;
import com.csit228.capstone.enums.TicketStatus;
import com.csit228.capstone.model.*;
import com.csit228.capstone.utils.AppSession;
import com.csit228.capstone.utils.FormValidator;
import com.csit228.capstone.utils.NotificationManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;

public abstract class BaseCreateTicketModalController {
  
  @FXML
  protected Button buttonClose;
  
  @FXML
  protected Button buttonCancel;
  
  @FXML
  protected Button buttonCreateTicket;
  
  @FXML
  protected TextField textfieldTicketTitle;
  
  @FXML
  protected TextArea textAreaTicketDesc;
  
  @FXML
  protected ComboBox<TicketPriority> comboBoxPriority;
  
  @FXML
  protected ComboBox<String> comboBoxHour;
  
  @FXML
  protected ComboBox<String> comboBoxMinute;
  
  @FXML
  protected ComboBox<String> comboBoxAmPm;
  
  @FXML
  protected DatePicker datePickerDeadline;
  
  protected final TicketDAO ticketDAO = TicketDAO.getTicketDAO();
  protected final User currentUser = AppSession.currentUser;
  
  protected boolean submitted = false;
  
  protected Control[] extraFieldsToValidate() {
    return new Control[0];
  }
  
  protected abstract Department resolveDepartment();
  
  protected abstract Integer resolveDepartmentId(Department department);
  
  protected void setupCommonFields() {
    populatePriorityComboBox();
    populateHourComboBox();
    populateMinuteComboBox();
    populateAmPmComboBox();
    configurePriorityDisplay();
  }
  
  @FXML
  public void onClickedCreateTicket() {
    Control[] baseFields =
      {textfieldTicketTitle, textAreaTicketDesc, comboBoxPriority, datePickerDeadline, comboBoxHour, comboBoxMinute,
       comboBoxAmPm,};
    Control[] allFields = concat(baseFields, extraFieldsToValidate());
    
    if (!FormValidator.validateRequired(allFields)) {
      System.out.println("Please fill all required fields!");
      return;
    }
    
    FormValidator.clearErrors(allFields);
    
    String title = textfieldTicketTitle.getText();
    String description = textAreaTicketDesc.getText();
    Department department = resolveDepartment();
    TicketPriority priority = comboBoxPriority.getValue();
    LocalDateTime deadline = getDeadlineDateTime();
    
    if (title.isBlank() || description.isBlank() || department == null || priority == null || deadline == null)
      return;
    
    LocalDateTime now = LocalDateTime.now();
    Ticket ticket;
    ticketDAO.createTicket(
      ticket = new Ticket(0, title, description, priority, deadline, TicketStatus.OPEN, currentUser.getUserId(), null, now, now,
                 resolveDepartmentId(department)));
    submitted = true;

    System.out.println("Ticket successfully created!");
    NotificationManager.notifyCreation(ticket, currentUser.getFullName());
    closeModal(buttonCreateTicket);
  }
  
  public boolean isSubmitted() {
    return submitted;
  }
  
  @FXML
  public void onClickedButtonClose() {
    closeModal(buttonClose);
  }
  
  @FXML
  public void onClickedCancel() {
    closeModal(buttonCancel);
  }
  
  protected void closeModal(Button sourceButton) {
    if (sourceButton != null && sourceButton.getScene() != null &&
        sourceButton.getScene().getWindow() instanceof Stage stage) {
      stage.close();
    }
  }
  
  protected LocalDateTime getDeadlineDateTime() {
    LocalDate date = datePickerDeadline.getValue();
    String hourStr = comboBoxHour.getValue();
    String minuteStr = comboBoxMinute.getValue();
    String amPmStr = comboBoxAmPm.getValue();
    
    if (date == null || hourStr == null || minuteStr == null || amPmStr == null)
      return null;
    
    int hour = Integer.parseInt(hourStr);
    int minute = Integer.parseInt(minuteStr);
    
    if (amPmStr.equals("PM") && hour != 12)
      hour += 12;
    else if (amPmStr.equals("AM") && hour == 12)
      hour = 0;
    
    return LocalDateTime.of(date, LocalTime.of(hour, minute));
  }
  
  private void populatePriorityComboBox() {
    comboBoxPriority.getItems().setAll(Arrays.asList(TicketPriority.values()));
  }
  
  private void populateHourComboBox() {
    ObservableList<String> hours = FXCollections.observableArrayList();
    for (int i = 1; i <= 12; i++)
      hours.add(String.format("%02d", i));
    comboBoxHour.setItems(hours);
  }
  
  private void populateMinuteComboBox() {
    ObservableList<String> minutes = FXCollections.observableArrayList();
    for (int i = 0; i < 60; i++)
      minutes.add(String.format("%02d", i));
    comboBoxMinute.setItems(minutes);
  }
  
  private void populateAmPmComboBox() {
    comboBoxAmPm.setItems(FXCollections.observableArrayList("AM", "PM"));
  }
  
  private void configurePriorityDisplay() {
    StringConverter<TicketPriority> converter = new StringConverter<>() {
      @Override
      public String toString(TicketPriority priority) {
        if (priority == null)
          return "";
        String name = priority.name().toLowerCase();
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
      }
      
      @Override
      public TicketPriority fromString(String string) {
        if (string == null || string.isBlank())
          return null;
        return TicketPriority.valueOf(string.trim().toUpperCase());
      }
    };
    
    comboBoxPriority.setConverter(converter);
    comboBoxPriority.setButtonCell(priorityCell(converter));
    comboBoxPriority.setCellFactory(lv -> priorityCell(converter));
  }
  
  private ListCell<TicketPriority> priorityCell(StringConverter<TicketPriority> converter) {
    return new ListCell<>() {
      @Override
      protected void updateItem(TicketPriority item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty || item == null ? null : converter.toString(item));
      }
    };
  }
  
  private Control[] concat(Control[] a, Control[] b) {
    Control[] result = new Control[a.length + b.length];
    System.arraycopy(a, 0, result, 0, a.length);
    System.arraycopy(b, 0, result, a.length, b.length);
    return result;
  }
}
