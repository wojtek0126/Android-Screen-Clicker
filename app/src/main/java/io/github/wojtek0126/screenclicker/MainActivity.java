package io.github.wojtek0126.screenclicker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

/** Settings screen: choose the screen profile, the tap point and the tap rate. */
public class MainActivity extends Activity {

    private TextView statusText;
    private Spinner profileSpinner;
    private EditText xInput;
    private EditText yInput;
    private EditText rateInput;
    private ClickerSettings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.status_text);
        profileSpinner = findViewById(R.id.profile_spinner);
        xInput = findViewById(R.id.x_input);
        yInput = findViewById(R.id.y_input);
        rateInput = findViewById(R.id.rate_input);

        ArrayAdapter<ScreenProfile> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, ScreenProfile.PROFILES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        profileSpinner.setAdapter(adapter);
        profileSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onProfileChanged(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        findViewById(R.id.accessibility_button).setOnClickListener(v ->
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        findViewById(R.id.show_button).setOnClickListener(v -> showFloatingControls());
        findViewById(R.id.hide_button).setOnClickListener(v -> {
            ClickerService service = ClickerService.getInstance();
            if (service != null) {
                service.hideOverlay();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // The point may have been moved with the floating target in the meantime.
        settings = ClickerSettings.load(this);
        profileSpinner.setSelection(settings.profileIndex, false);
        fillInputs();
        statusText.setText(ClickerService.getInstance() != null
                ? R.string.service_enabled : R.string.service_disabled);
    }

    /** Keeps the same physical point when switching to another screen profile. */
    private void onProfileChanged(int position) {
        if (settings == null || position == settings.profileIndex) {
            return;
        }
        readInputs();
        float[] device = settings.devicePoint(this);
        settings.profileIndex = position;
        settings.setDevicePoint(this, device[0], device[1]);
        fillInputs();
    }

    private void fillInputs() {
        xInput.setText(format(settings.x));
        yInput.setText(format(settings.y));
        rateInput.setText(format(settings.tapsPerSecond));
    }

    /** Copies valid input values into {@link #settings}; returns false on invalid input. */
    private boolean readInputs() {
        try {
            float x = (float) parse(xInput);
            float y = (float) parse(yInput);
            double rate = parse(rateInput);
            if (x < 0 || y < 0 || Float.isNaN(x) || Float.isNaN(y)
                    || Float.isInfinite(x) || Float.isInfinite(y)
                    || Double.isNaN(rate) || rate <= 0) {
                return false;
            }
            settings.x = x;
            settings.y = y;
            settings.tapsPerSecond = TapRate.clamp(rate);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void showFloatingControls() {
        if (!readInputs()) {
            Toast.makeText(this, R.string.invalid_input, Toast.LENGTH_SHORT).show();
            return;
        }
        settings.profileIndex = profileSpinner.getSelectedItemPosition();
        settings.save(this);
        fillInputs();

        ClickerService service = ClickerService.getInstance();
        if (service == null) {
            Toast.makeText(this, R.string.enable_service_first, Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            return;
        }
        service.showOverlay();
        Toast.makeText(this, R.string.overlay_hint, Toast.LENGTH_LONG).show();
        moveTaskToBack(true);
    }

    /** Accepts both '.' and ',' as decimal separator (keyboards follow the device locale). */
    private static double parse(EditText input) {
        return Double.parseDouble(input.getText().toString().trim().replace(',', '.'));
    }

    private static String format(double value) {
        if (value == Math.rint(value)) {
            return String.format(Locale.US, "%.0f", value);
        }
        return String.format(Locale.US, "%.2f", value);
    }
}
