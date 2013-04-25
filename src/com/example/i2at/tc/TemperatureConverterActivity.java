
package com.example.i2at.tc;

import java.util.List;

import android.content.ComponentName;
import android.content.pm.InstrumentationInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.android.debug.hv.LocalViewServerActivity;

/**
 * <em>TemperatureConverterActivity</em> is a demonstration Activity used to
 * show some testing features.
 * 
 * @author diego
 */
public class TemperatureConverterActivity extends LocalViewServerActivity {
    public static final String FAHRENHEIT_KEY = "com.example.i2at.tc.Fahrenheit";

    public static final String CELSIUS_KEY = "com.example.i2at.tc.Celsius";

    @SuppressWarnings("unused")
    private static final String TAG = "TemperatureConverterActivity";

    private static final int MENU_ITEM_RUN_TESTS = 1;

    private static final boolean DEBUG = false;

    public abstract class TemperatureChangeWatcher implements TextWatcher {
        private EditNumber mSource;
        private EditNumber mDest;

        /**
         * @param source
         * @param dest
         */
        public TemperatureChangeWatcher(EditNumber source, EditNumber dest) {
            this.mSource = source;
            this.mDest = dest;
        }

        /*
         * (non-Javadoc)
         * @see android.text.TextWatcher#afterTextChanged(android.text.Editable)
         */
        @Override
        public void afterTextChanged(Editable arg0) {
            // TODO Auto-generated method stub

        }

        /*
         * (non-Javadoc)
         * @see android.text.TextWatcher#onTextChanged(java.lang.CharSequence,
         * int, int, int)
         */
        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                int count) {
            if (!mDest.hasWindowFocus() || mDest.hasFocus() || s == null) {
                return;
            }
            final String str = s.toString();
            if ("".equals(str)) {
                mDest.setText("");
                return;
            }
            try {
                android.util.Log.v("TemperatureChangeWatcher", "converting temp=" + str + "{"
                        + Double.parseDouble(str) + "}");
                final double result = convert(Double.parseDouble(str));
                android.util.Log.v("TemperatureChangeWatcher", "result=" + result);
                // final String resutlStr = String.format("%.2f", result);
                mDest.setNumber(result);
            } catch (NumberFormatException e) {
                // WARNING:
                // this is thrown while a number is entered
                // for example just a '-'
            } catch (Exception e) {
                mSource.setError("ERROR: " + e.getLocalizedMessage());
            }
        }

        protected abstract double convert(double temp);

        /*
         * (non-Javadoc)
         * @see android.text.TextWatcher#onTextChanged(java.lang.CharSequence,
         * int, int, int)
         */
        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                int arg3) {
            // TODO Auto-generated method stub

        }

    }

    private EditNumber mCelsius;
    private EditNumber mFahrenheit;

    private TextView mDebug;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mCelsius = (EditNumber) findViewById(R.id.celsius);
        mFahrenheit = (EditNumber) findViewById(R.id.fahrenheit);

        mCelsius.addTextChangedListener(new TemperatureChangeWatcher(mCelsius, mFahrenheit) {

            @Override
            protected double convert(double temp) {
                int[] location = new int[2];
                mCelsius.getLocationOnScreen(location);
                mDebug.append("celsius: loc on screen (" + location[0] + "," + location[1] + ")\n");
                mDebug.append("         pos (" + mCelsius.getLeft() + "," + mCelsius.getTop()
                        + ")\n");
                mFahrenheit.getLocationOnScreen(location);
                mDebug.append("fahrenheit: loc on screen (" + location[0] + "," + location[1]
                        + ")\n");
                mDebug.append("         pos (" + mFahrenheit.getLeft() + "," + mFahrenheit.getTop()
                        + ")\n");
                return TemperatureConverter.celsiusToFahrenheit(temp);
            }

        });

        mFahrenheit.addTextChangedListener(new TemperatureChangeWatcher(mFahrenheit, mCelsius) {

            @Override
            protected double convert(double temp) {
                return TemperatureConverter.fahrenheitToCelsius(temp);
            }

        });

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(CELSIUS_KEY)) {
                mCelsius.setNumber(savedInstanceState.getDouble(CELSIUS_KEY));
            }
            else if (savedInstanceState.containsKey(FAHRENHEIT_KEY)) {
                mFahrenheit.setNumber(savedInstanceState.getDouble(FAHRENHEIT_KEY));
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onAttachedToWindow()
     */
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (DEBUG) {
            mDebug = (TextView) findViewById(R.id.debug);
            int[] location = new int[2];
            mCelsius.getLocationOnScreen(location);
            mDebug.append("DEBUG:\n");
            mDebug.append("celsius: loc on screen (" + location[0] + "," + location[1] + ")\n");
            mDebug.append("         pos (" + mDebug.getLeft() + "," + mDebug.getTop() + ")\n");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, MENU_ITEM_RUN_TESTS, Menu.NONE, "Run tests").setIcon(
                android.R.drawable.ic_menu_manage);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ITEM_RUN_TESTS:
                runTests();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void runTests() {
        final String packageName = getPackageName();
        final List<InstrumentationInfo> list = getPackageManager().queryInstrumentation(
                packageName, 0);
        if (list.isEmpty()) {
            Toast.makeText(this, "Cannot find instrumentation for " + packageName,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        InstrumentationInfo instrumentationInfo = null;
        for (InstrumentationInfo ii : list) {
            if ((packageName + ".test").equals(ii.packageName)) {
                instrumentationInfo = ii;
                break;
            }
        }
        final ComponentName componentName = new ComponentName(instrumentationInfo.packageName,
                instrumentationInfo.name);
        if (!startInstrumentation(componentName, null, null)) {
            Toast.makeText(this, "Cannot run instrumentation for " + packageName,
                    Toast.LENGTH_SHORT).show();
        }
    }

    public double getCelsius() {
        return mCelsius.getNumber();
    }

    public double getFahrenheit() {
        return mFahrenheit.getNumber();
    }

}
