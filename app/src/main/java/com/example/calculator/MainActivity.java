package com.example.calculator;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    TextView resultField; // текстовое поле результата / ввода
    String lastOperation = "="; // последняя операция ([+][-][*]...)
    Button lastActiveOperandButton; // ссылка на кнопку операции
    Button dotButton; // ссылка на кнопку десятичной точки
    Double resultNum = 0.0; // фактический ответ (результат)
    boolean isShownResult = false; // служебная переменная для выбора логики обработки тектового поля
    boolean isNewOperation = false; // служебная переменная для выбора логики обработки нажатия клавиш

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
            if (text.length() < 5) {
                findViewById(R.id.button_multiply).setEnabled(true);
                findViewById(R.id.button_plus).setEnabled(true);
            }
        }
    }

    public void onDot(View view) {
        dotButton.setEnabled(false);
        if (resultField.getText().toString().contains(",")) return;
        resultField.append(",");
        isShownResult = false;
    }


    // обработка нажатия на числовую кнопку
    public void onNumberClick(View view) {

        Button button = (Button) view;

        // если отображен результат, то перезаписываем
        if (isShownResult) {
            if (lastOperation.equals("="))
                onClear(null);
            isShownResult = false;
            resultField.setText(button.getText());
            dotButton.setEnabled(true);
            return;
        }

        String result = resultField.getText().toString();

        //ограничиваем возможность умножать/складывать длинные числа чтобы ответ поместился на экране
        if (result.length() > 3) {
            if (dotButton.isEnabled())
                findViewById(R.id.button_multiply).setEnabled(false);
            if (result.length() >= 8 || Objects.equals(lastOperation, "*"))
                powerButtons(false, true);
        }
        if (!Objects.equals(lastOperation, "+") && !Objects.equals(lastOperation, "*") && !(result.length() >= 8) && resultNum <= 99999)
            powerButtons(true, true);

        //если в поле [0] - заменяем его, если [0,] - дописываем
        if (!result.startsWith("0,")
                && result.startsWith("0"))
            resultField.setText(button.getText());
        else
            resultField.append(button.getText());

        dotButton.setEnabled(!result.contains(","));

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

        // если меняем операнд, не изменяя числа ( [+] -> [*] например), не применяем дейсвие
        if (isShownResult) {
            lastOperation = operation;
            return;
        }

        isShownResult = true;

        if ((Objects.equals(operation, "*") && resultNum >= 9999)
                || (Objects.equals(operation, "+") && resultNum >= 99999)) {
            powerButtons(false, true);
            findViewById(R.id.button_multiply).setEnabled(false);
            findViewById(R.id.button_plus).setEnabled(false);
        } else {
            powerButtons(true, true);
            findViewById(R.id.button_multiply).setEnabled(true);
            findViewById(R.id.button_plus).setEnabled(true);
        }


        String number = resultField.getText().toString();

        if (number.length() > 0) {
            number = number.replace(',', '.');
            try {
                performOperation(Double.valueOf(number));
            } catch (NumberFormatException ex) {
                resultField.setText("-0");
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
                    resultNum = number;
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


        if (resultNum.intValue() == resultNum) {
            resultField.setText(String.valueOf(resultNum.intValue()));
        } else {
            DecimalFormat formatter = new DecimalFormat("#.####");
            String result = formatter.format(resultNum);
            if (!resultNum.toString().replace(".", ",").equals(result))
                result = "≈" + result;
            resultField.setText(result);
        }

        findViewById(R.id.button_multiply).setEnabled(resultNum <= 99999);
        findViewById(R.id.button_plus).setEnabled(resultNum <= 99999);

        isShownResult = true;
    }

    // изменяет активность группы кнопок
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