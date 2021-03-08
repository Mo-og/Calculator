package com.example.calculator;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    TextView resultField; // текстовое поле результата / ввода
    String lastOperation = "="; // последняя операция ([+][-][*]...)
    Button lastActiveOperandButton; // ссылка на кнопку операции
    Button dotButton; // ссылка на кнопку десятичной точки
    Double resultNum = 0.0; // фактический ответ (результат)
    boolean isShownResult = false; // служебная переменная для выбора логики обработки тектового поля

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resultField = (TextView) findViewById(R.id.result);
        resultField.setText("0");
        dotButton = (Button) findViewById(R.id.button_dot);
    }

    public void onClear(View view) {
        // помечаем кнопку неактивной цветом
        if (lastActiveOperandButton != null)
            lastActiveOperandButton.setBackgroundColor(getResources().getColor(R.color.button_operand, getTheme()));
        resultNum = 0.0;
        resultField.setText("0");
        lastOperation = "=";
        isShownResult = false;
        dotButton.setEnabled(true);
        powerButtons(true, false);
    }

    public void onBackspace(View view) {
        String text = resultField.getText().toString();
        if (text.charAt(text.length() - 1) == ',')
            dotButton.setEnabled(true);

        if (text.length() <= 1)
            resultField.setText("0");
        else
            resultField.setText(text.substring(0, text.length() - 1));
        if (text.length() < 9) {
            powerButtons(true, true);
            if (text.length() <= 4){
                findViewById(R.id.button_multiply).setEnabled(true);
                findViewById(R.id.button_plus).setEnabled(true);
        }}
    }

    public void onDot(View view) {
        dotButton.setEnabled(false);
        resultField.append(",");
    }


    // обработка нажатия на числовую кнопку
    public void onNumberClick(View view) {
        String result = resultField.getText().toString();

        if (isShownResult) {
            isShownResult = false;
            resultField.setText("");
            if (Objects.equals(lastOperation, "=")) resultNum = 0.0;
        }
        if (lastOperation != null && lastActiveOperandButton != null)
            if (!lastOperation.contentEquals(lastActiveOperandButton.getText())) {
                resultField.setText("");
                dotButton.setEnabled(true);
            }

        if (result.length() >= 3) {
            findViewById(R.id.button_multiply).setEnabled(false);
            if (result.length() >= 8)
                powerButtons(false, true);
        }
        if (!Objects.equals(lastOperation, "+") && !Objects.equals(lastOperation, "*") && !(result.length() >= 8))
            powerButtons(true, true);
        if (Objects.equals(lastOperation, "*") && resultField.getText().toString().length() > 3)
            powerButtons(false, true);
        Button button = (Button) view;
        if (!result.startsWith("0,")
                && result.startsWith("0"))
            resultField.setText(button.getText());
        else
            resultField.append(button.getText());

    }

    // обработка нажатия на кнопку операции ([+][-][*]...)
    public void onOperationClick(View view) {

        //отмена выделения у старой кнопки, выделение цветом активной кнопки
        if (lastActiveOperandButton != null) {
            lastActiveOperandButton.setEnabled(true);
            lastActiveOperandButton.setBackgroundColor(getResources().getColor(R.color.button_operand, getTheme()));
        }
        Button button = (Button) view;
        button.setBackgroundColor(getResources().getColor(R.color.button_pressed, getTheme()));
        lastActiveOperandButton = button;
        //

        String operation = button.getText().toString();

        if ((Objects.equals(operation, "*") && resultNum.toString().length() > 4)
                || (Objects.equals(operation, "+") && resultNum.toString().length() > 5)) {
            powerButtons(false, true);
            findViewById(R.id.button_multiply).setEnabled(false);
            findViewById(R.id.button_plus).setEnabled(false);
        } else {
            powerButtons(true, true);

        }
        // если меняем операнд, не изменяя числа ( [+] -> [*] например), не применяем дейсвие
        if (isShownResult && getNumFromField() != 0.0) {
            lastOperation = operation;
            return;
        }
        isShownResult = true;

        String number = resultField.getText().toString();

        if (number.length() > 0) {
            number = number.replace(',', '.');
            try {
                performOperation(Double.valueOf(number));
            } catch (NumberFormatException ex) {
                resultField.setText("");
            }
            lastOperation = operation;
        }
    }

    // возвращает отображаемый результат числом
    private Double getNumFromField() {
        try {
            return Double.valueOf(resultField.getText().toString().replace(',', '.'));
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }

    private void performOperation(Double number) {

        // при вводе самой первой операции
        if (((resultNum == null || resultNum == 0.0) && lastOperation.equals("="))) {
            resultNum = number;
        } else {
            switch (lastOperation) {
                case "=":
                    break;
                case "/":
                    if (number == 0) {
                        resultField.setText(getString(R.string.divisionByZero, resultNum.toString()).replace('.', ','));
                        powerButtons(false, false);
                        return;
                    } else {
                        resultNum /= number;
                    }
                    break;
                case "*":
                    resultNum *= number;
                    break;
                case "+":
                    resultNum += number;
                    break;
                case "-":
                    resultNum -= number;
                    break;
            }
        }

        String result = resultNum.toString().replace('.', ',');
        if (resultNum.intValue() == resultNum) {
            resultField.setText(String.valueOf(resultNum.intValue()));
        }
        dotButton.setEnabled(!result.contains(","));
        isShownResult = true;
    }

    private void powerButtons(boolean buttonsEnabled, boolean numericOnly) {
        findViewById(R.id.button0).setEnabled(buttonsEnabled);
        findViewById(R.id.button1).setEnabled(buttonsEnabled);
        findViewById(R.id.button2).setEnabled(buttonsEnabled);
        findViewById(R.id.button3).setEnabled(buttonsEnabled);
        findViewById(R.id.button4).setEnabled(buttonsEnabled);
        findViewById(R.id.button5).setEnabled(buttonsEnabled);
        findViewById(R.id.button6).setEnabled(buttonsEnabled);
        findViewById(R.id.button7).setEnabled(buttonsEnabled);
        findViewById(R.id.button8).setEnabled(buttonsEnabled);
        findViewById(R.id.button9).setEnabled(buttonsEnabled);
        if (!numericOnly) {
            findViewById(R.id.button_plus).setEnabled(buttonsEnabled);
            findViewById(R.id.button_minus).setEnabled(buttonsEnabled);
            findViewById(R.id.button_divide).setEnabled(buttonsEnabled);
            findViewById(R.id.button_multiply).setEnabled(buttonsEnabled);
            findViewById(R.id.button_equals).setEnabled(buttonsEnabled);
            findViewById(R.id.button_dot).setEnabled(buttonsEnabled);
            findViewById(R.id.button_backspace).setEnabled(buttonsEnabled);
        }
    }
}