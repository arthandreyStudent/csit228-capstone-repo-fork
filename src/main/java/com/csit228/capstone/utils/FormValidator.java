package com.csit228.capstone.utils;

import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.Control;
import javafx.scene.control.TextInputControl;

public class FormValidator {

  // The name of the CSS class we will apply to invalid fields
  private static final String ERROR_STYLE_CLASS = "error-field";

  // A key to track if we've already attached a listener to a field
  private static final String LISTENER_ADDED_KEY = "validation_listener_added";

  /**
   * Checks if all provided JavaFX Controls have values.
   * Applies a red border to missing fields and removes it from filled ones.
   * Also attaches responsive listeners to clear the error state as the user types.
   *
   * @param fields A comma-separated list (VarArgs) of JavaFX Controls
   * @return true if ALL fields are filled, false if AT LEAST ONE is empty
   */
  public static boolean validateRequired(Control... fields) {
    boolean isAllValid = true;

    for (Control field : fields) {
      boolean isFieldValid = true;
      
      if (field instanceof TextInputControl) {
        TextInputControl textInput = (TextInputControl) field;
        if (
          textInput.getText() == null || textInput.getText().trim().isEmpty()
        ) {
          isFieldValid = false;
        }
      }

      else if (field instanceof ComboBoxBase) {
        ComboBoxBase<?> comboBoxBase = (ComboBoxBase<?>) field;
        if (comboBoxBase.getValue() == null) {
          isFieldValid = false;
        } else if (comboBoxBase.getValue() instanceof String) {
          String val = (String) comboBoxBase.getValue();
          if (val.trim().isEmpty()) {
            isFieldValid = false;
          }
        }
      }

      else if (field instanceof ChoiceBox) {
        ChoiceBox<?> choiceBox = (ChoiceBox<?>) field;
        if (choiceBox.getValue() == null) {
          isFieldValid = false;
        } else if (choiceBox.getValue() instanceof String) {
          String val = (String) choiceBox.getValue();
          if (val.trim().isEmpty()) {
            isFieldValid = false;
          }
        }
      }
      
      if (isFieldValid) {
        field.getStyleClass().remove(ERROR_STYLE_CLASS);
      } else {
        if (!field.getStyleClass().contains(ERROR_STYLE_CLASS)) {
          field.getStyleClass().add(ERROR_STYLE_CLASS);
        }
        isAllValid = false;
        
        attachResponsiveListener(field);
      }
    }

    return isAllValid;
  }

  /**
   * Attaches a one-time listener to the field to remove the error class upon interaction.
   */
  private static void attachResponsiveListener(Control field) {
    if (Boolean.TRUE.equals(field.getProperties().get(LISTENER_ADDED_KEY))) {
      return;
    }

    if (field instanceof TextInputControl) {
      ((TextInputControl) field).textProperty().addListener(
        (observable, oldValue, newValue) -> {
          field.getStyleClass().remove(ERROR_STYLE_CLASS);
        }
      );
    } else if (field instanceof ComboBoxBase) {
      ((ComboBoxBase<?>) field).valueProperty().addListener(
        (observable, oldValue, newValue) -> {
          field.getStyleClass().remove(ERROR_STYLE_CLASS);
        }
      );
    } else if (field instanceof ChoiceBox) {
      ((ChoiceBox<?>) field).valueProperty().addListener(
        (observable, oldValue, newValue) -> {
          field.getStyleClass().remove(ERROR_STYLE_CLASS);
        }
      );
    }
    
    field.getProperties().put(LISTENER_ADDED_KEY, true);
  }

  /**
   * Clears the error borders from all fields (useful for form reset buttons)
   */
  public static void clearErrors(Control... fields) {
    for (Control field : fields) {
      field.getStyleClass().remove(ERROR_STYLE_CLASS);
    }
  }
}
