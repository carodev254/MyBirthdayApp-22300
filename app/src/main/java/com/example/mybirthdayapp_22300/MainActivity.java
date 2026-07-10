package com.example.mybirthdayapp_22300;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    private TextView tvExpression, tvResult;
    private final StringBuilder expression = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        tvExpression = findViewById(R.id.tvExpression);
        tvResult = findViewById(R.id.tvResult);

        setButtonListeners();
    }

    private void setButtonListeners() {
        View.OnClickListener listener = v -> {
            if (v instanceof Button) {
                Button b = (Button) v;
                String text = b.getText().toString();
                onButtonClick(text);
            }
        };

        android.widget.GridLayout gridLayout = findViewById(R.id.gridLayout);
        if (gridLayout != null) {
            int count = gridLayout.getChildCount();
            int i = 0;
            while (i < count) {
                View v = gridLayout.getChildAt(i);
                if (v instanceof Button) {
                    v.setOnClickListener(listener);
                }
                i++;
            }
        }
    }

    private void onButtonClick(String text) {
        String ac = getString(R.string.btn_ac);
        String c = getString(R.string.btn_c);
        String eq = getString(R.string.btn_equal);
        String pi = getString(R.string.btn_pi);
        String e = getString(R.string.btn_e);
        String mul = getString(R.string.btn_mul);
        String div = getString(R.string.btn_div);

        if (text.equals(ac)) {
            expression.setLength(0);
            tvResult.setText("");
        } else if (text.equals(c)) {
            if (expression.length() > 0) {
                expression.setLength(expression.length() - 1);
            }
        } else if (text.equals(eq)) {
            evaluateExpression();
            return;
        } else if (text.equals(pi)) {
            expression.append("π");
        } else if (text.equals(e)) {
            expression.append("e");
        } else if (text.equals(mul)) {
            expression.append("*");
        } else if (text.equals(div)) {
            expression.append("/");
        } else {
            switch (text) {
                case "sin":
                case "cos":
                case "tan":
                case "log":
                case "ln":
                case "sqrt":
                    expression.append(text).append("(");
                    break;
                default:
                    expression.append(text);
                    break;
            }
        }
        tvExpression.setText(expression.toString());
    }

    private void evaluateExpression() {
        try {
            String expr = expression.toString();
            double result = eval(expr);
            tvResult.setText(new DecimalFormat("0.##########").format(result));
        } catch (Exception e) {
            tvResult.setText(R.string.error);
        }
    }

    public static double eval(final String str) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char) ch);
                return x;
            }

            double parseExpression() {
                double x = parseTerm();
                for (; ; ) {
                    if (eat('+')) x += parseTerm();
                    else if (eat('-')) x -= parseTerm();
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (; ; ) {
                    if (eat('*')) x *= parseFactor();
                    else if (eat('/')) x /= parseFactor();
                    else if (eat('%')) x %= parseFactor();
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return +parseFactor();
                if (eat('-')) return -parseFactor();

                double x;
                int startPos = this.pos;
                if (eat('(')) {
                    x = parseExpression();
                    if (!eat(')')) throw new RuntimeException("Missing ')'");
                } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else if ((ch >= 'a' && ch <= 'z') || ch == 'π') {
                    if (ch == 'π') {
                        nextChar();
                        x = Math.PI;
                    } else {
                        while (ch >= 'a' && ch <= 'z') nextChar();
                        String func = str.substring(startPos, this.pos);
                        if (func.equals("e")) {
                            x = Math.E;
                        } else {
                            if (eat('(')) {
                                x = parseExpression();
                                if (!eat(')')) throw new RuntimeException("Missing ')'");
                            } else {
                                x = parseFactor();
                            }
                            switch (func) {
                                case "sqrt": x = Math.sqrt(x); break;
                                case "sin": x = Math.sin(Math.toRadians(x)); break;
                                case "cos": x = Math.cos(Math.toRadians(x)); break;
                                case "tan": x = Math.tan(Math.toRadians(x)); break;
                                case "log": x = Math.log10(x); break;
                                case "ln": x = Math.log(x); break;
                                default: throw new RuntimeException("Unknown function: " + func);
                            }
                        }
                    }
                } else {
                    throw new RuntimeException("Unexpected: " + (char) ch);
                }

                if (eat('^')) x = Math.pow(x, parseFactor());
                if (eat('!')) x = factorial((int) x);

                return x;
            }

            double factorial(int n) {
                if (n < 0) return 0;
                double res = 1;
                for (int i = 2; i <= n; i++) res *= i;
                return res;
            }
        }.parse();
    }
}
