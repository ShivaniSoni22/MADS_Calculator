package com.flytbase.madscalculator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.flytbase.madscalculator.databinding.ActivityMainBinding;
import com.flytbase.madscalculator.fragment.NavigationDrawerFragment;
import com.flytbase.madscalculator.model.CalculationHistory;
import com.flytbase.madscalculator.utility.SharedPrefsUtil;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private final char[] calculationOperations = {'*', '+', '/', '-'};
    private final int[] operatorsRank = {1, 2, 3, 4};  // 1 is highest and 4 is lowest
    private ActivityMainBinding activityMainBinding;
    private GoogleSignInClient mGoogleSignInClient;
    private boolean clearCalculation = false;
    private FirebaseAuth firebaseAuth;
    private String userId;
    private boolean isLoggedIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        firebaseAuth = FirebaseAuth.getInstance();
        ArrayList<CalculationHistory> historyList = new ArrayList<>();

        if (firebaseAuth.getCurrentUser() != null) {
            userId = firebaseAuth.getCurrentUser().getUid();
            activityMainBinding.toolbar.findViewById(R.id.tvLogout).setVisibility(View.VISIBLE);
            isLoggedIn = true;
        }
        setUpDrawer();
        activityMainBinding.toolbar.findViewById(R.id.tvLogout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
        activityMainBinding.button1.setOnClickListener(v -> setCalculationBox("1"));

        activityMainBinding.button2.setOnClickListener(v -> setCalculationBox("2"));

        activityMainBinding.button3.setOnClickListener(v -> setCalculationBox("3"));

        activityMainBinding.button4.setOnClickListener(v -> setCalculationBox("4"));

        activityMainBinding.button5.setOnClickListener(v -> setCalculationBox("5"));

        activityMainBinding.button6.setOnClickListener(v -> setCalculationBox("6"));

        activityMainBinding.button7.setOnClickListener(v -> setCalculationBox("7"));

        activityMainBinding.button8.setOnClickListener(v -> setCalculationBox("8"));

        activityMainBinding.button9.setOnClickListener(v -> setCalculationBox("9"));

        activityMainBinding.button0.setOnClickListener(v -> setCalculationBox("0"));

        activityMainBinding.buttonadd.setOnClickListener(v ->
                activityMainBinding.etDisplay.setText(String.format("%s + ", activityMainBinding.etDisplay.getText())));

        activityMainBinding.buttonsub.setOnClickListener(v ->
                activityMainBinding.etDisplay.setText(String.format("%s - ", activityMainBinding.etDisplay.getText())));

        activityMainBinding.buttonmul.setOnClickListener(v ->
                activityMainBinding.etDisplay.setText(String.format("%s * ", activityMainBinding.etDisplay.getText())));

        activityMainBinding.buttondiv.setOnClickListener(v ->
                activityMainBinding.etDisplay.setText(String.format("%s / ", activityMainBinding.etDisplay.getText())));

        activityMainBinding.buttoneql.setOnClickListener(v -> {
            clearCalculation = true;
            String expression = activityMainBinding.etDisplay.getText().toString();
            if (evaluate(expression) != -0) {
                activityMainBinding.etDisplay.setText(String.format("%s", evaluate(expression)));
                CalculationHistory calculationHistory = new CalculationHistory(
                        expression, activityMainBinding.etDisplay.getText().toString());
                if (userId != null) {
                    FirebaseFirestore.getInstance().collection("users")
                            .document(userId)
                            .collection("history")
                            .add(calculationHistory);
                }
                historyList.add(calculationHistory);
                saveInHistory(historyList, this);
            } else {
                activityMainBinding.etDisplay.setText("invalid");
            }
        });

        activityMainBinding.buttonDel.setOnClickListener(v -> {
            String s1 = activityMainBinding.etDisplay.getText().toString();
            if (s1.length() > 0)
                activityMainBinding.etDisplay.setText(s1.substring(0, s1.length() - 1));
        });

        activityMainBinding.btnClear.setOnClickListener(view -> activityMainBinding.etDisplay.setText(""));

    }

    public void setDisplayExpression(String expression) {
        activityMainBinding.etDisplay.setText(expression);
    }

    private void setUpDrawer() {
        NavigationDrawerFragment navDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_drawer_fragment);
        DrawerLayout drawerLayout = findViewById(R.id.main_drawer_layout);
        if (navDrawerFragment != null) {
            navDrawerFragment.setUpDrawer(R.id.nav_drawer_fragment, drawerLayout, findViewById(R.id.toolbar));
        }
    }

    private void signOut() {
        if (isLoggedIn) {
            firebaseAuth.signOut();
            mGoogleSignInClient.signOut()
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d(TAG, "onComplete: Signed out Successfully");
                            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            e.printStackTrace();
                        }
                    });
        }
    }

    private void saveInHistory(ArrayList<CalculationHistory> list, Context context) {
        SharedPrefsUtil.saveData(context, list);
    }

    private void setCalculationBox(String value) {
        if (clearCalculation) {
            activityMainBinding.etDisplay.setText("");
            this.clearCalculation = false;
        }
        activityMainBinding.etDisplay.setText(String.format("%s%s", activityMainBinding.etDisplay.getText(), value));
    }

    private float evaluate(String expression) {
        try {
            char[] tokens = expression.toCharArray();
            Stack<Float> numberValues = new Stack<>();
            Stack<Character> operationValues = new Stack<>();

            for (int i = 0; i < tokens.length; i++) {
                // is space is found skip to next index
                if (tokens[i] == ' ') {
                    continue;
                }

                if (!checkIsOperator(tokens[i])) {
                    StringBuilder s = new StringBuilder();
                    while (i < tokens.length && tokens[i] != ' ' && !checkIsOperator(tokens[i])) {
                        s.append(tokens[i++]);
                    }
                    numberValues.push(Float.parseFloat(s.toString()));
                } else if (checkIsOperator(tokens[i])) {
                    // if its operator list has one value then check the ranking
                    while (!operationValues.empty() && hasPrecedence(tokens[i], operationValues.peek())) {
                        // if the precedence value is greater than the next operator values is popped and
                        // calculation is performed.
                        numberValues.push(calculate(operationValues.pop(), numberValues.pop(), numberValues.pop()));
                    }
                    operationValues.push(tokens[i]);
                }
            }
            while (!operationValues.empty()) {
                numberValues.push(calculate(operationValues.pop(), numberValues.pop(), numberValues.pop()));
            }
            return numberValues.pop();
        } catch (Exception ee) {
            return -0;
        }
    }

    private boolean checkIsOperator(char operator) {
        for (char calculationOperation : calculationOperations) {
            if (operator == calculationOperation) {
                // if operator matches the list item return true.
                return true;
            }
        }
        return false;
    }

    private boolean hasPrecedence(char operator1, char operator2) {
        //check the ranking of the operators
        int op1p = getPrecedence(operator1);
        int op2p = getPrecedence(operator2);
        return op2p <= op1p;
    }

    public int getPrecedence(char c) {
        int i;
        for (i = 0; i < calculationOperations.length; i++) {
            if (c == calculationOperations[i])
                break;
        }
        return operatorsRank[i];
    }

    public float calculate(char op, float b, float a) {
        switch (op) {
            case '+':
                return a + b;
            case '-':
                return a - b;
            case '*':
                return a * b;
            case '/':
                return a / b;
        }
        return 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPrefsUtil.clearData(MainActivity.this);
    }

}