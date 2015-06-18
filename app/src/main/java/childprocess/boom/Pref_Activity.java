package childprocess.boom;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;

/**
 * Created by jhony on 6/17/2015.
 */
public class Pref_Activity extends Activity {
    Spinner spinner;
    String theme_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE); //hide title bar
        setContentView(R.layout.activity_pref);
        SharedPreferences prefs = getSharedPreferences("theme_id", MODE_PRIVATE);
        theme_id = prefs.getString("id", null);
        spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.Skin_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new itemAdapter());
spinner.setSelection(Integer.parseInt(theme_id));
        Button myButton = (Button) findViewById(R.id.theme);
        myButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

                Spinner sp = (Spinner) findViewById(R.id.spinner);
                String spinnerString = null;
                spinnerString = sp.getSelectedItem().toString();
                int nPos = sp.getSelectedItemPosition();
                SharedPreferences prefs = getSharedPreferences("theme_id", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("id", String.valueOf(nPos));

                //Code for extracting password value and saving it in the SharedPreference
                editor.commit();

            }
        });
    }

    //get the selected dropdown list value

    public void addListenerOnButton() {

//         btnSubmit = (Button) findViewById(R.id.btnSubmit);
//
//        btnSubmit.setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//
//                Toast.makeText(SpinnerExample.this,
//                        "On Button Click : " +
//                                "\n" + String.valueOf(spinner1.getSelectedItem()),
//                        Toast.LENGTH_LONG).show();
//            }
//
//        });

    }
}

