package it.nicolabrogelli.manosoft.princessdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class StratActivity extends AppCompatActivity {

    de.hdodenhof.circleimageview.CircleImageView btnUiPrincess, btnTerminalPrincess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_strat);

        btnUiPrincess = (de.hdodenhof.circleimageview.CircleImageView) findViewById(R.id.btnUiPrincess);
        btnTerminalPrincess = (de.hdodenhof.circleimageview.CircleImageView) findViewById(R.id.btnTerminalPrincess);

        btnUiPrincess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnUiPrincess.setImageResource(R.color.princess_theme_dark_blue_colorPrimary);
                Intent i = new Intent(StratActivity.this, UIPrincesaActivity.class);
                startActivity(i);
            }
        });

        btnTerminalPrincess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnTerminalPrincess.setImageResource(R.color.princess_theme_dark_blue_colorPrimary);
                Intent i = new Intent(StratActivity.this, TerminalActivity.class);
                startActivity(i);

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        btnUiPrincess.setImageResource(R.drawable.ui);
        btnTerminalPrincess.setImageResource(R.drawable.terminal);
    }
}
