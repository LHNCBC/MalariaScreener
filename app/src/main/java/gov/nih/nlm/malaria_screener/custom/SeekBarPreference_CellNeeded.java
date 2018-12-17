package gov.nih.nlm.malaria_screener.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import gov.nih.nlm.malaria_screener.R;


public class SeekBarPreference_CellNeeded extends Preference implements OnSeekBarChangeListener
{
	private static final String ANDROIDNS = "http://schemas.android.com/apk/res/android";
	private static final String ROBOBUNNYNS = "http://robobunny.com";
	private int DEFAULT_VALUE = 1000;

	private int maxValue = 3000;
	private int minValue = 1;
	private int interval = 25;
	private int currentValue;
	private String unitsLeft = "";
	private String unitsRight = "";

	private SeekBar seekBar;
	private TextView statusText;
	private TextView unitsRightView;
	private TextView unitsLeftView;

	public SeekBarPreference_CellNeeded(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		setValuesFromXml(attrs);
	}

	public SeekBarPreference_CellNeeded(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		setValuesFromXml(attrs);
	}

	private void setValuesFromXml(AttributeSet attrs)
	{
		maxValue = attrs.getAttributeIntValue(ANDROIDNS, "max", 3000);
		minValue = attrs.getAttributeIntValue(ROBOBUNNYNS, "min", 1);
		DEFAULT_VALUE = attrs.getAttributeIntValue(ROBOBUNNYNS, "defaultValue", 1000);

		unitsLeft = getAttributeStringValue(attrs, ROBOBUNNYNS, "unitsLeft", "");
		String units = getAttributeStringValue(attrs, ROBOBUNNYNS, "units", "");
		unitsRight = getAttributeStringValue(attrs, ROBOBUNNYNS, "unitsRight", units);

		try
		{
			String intervalStr = attrs.getAttributeValue(ROBOBUNNYNS, "interval");
			if (intervalStr != null) interval = Integer.parseInt(intervalStr);
		}
		catch (NumberFormatException ignored) {}
	}

	@Override
	protected View onCreateView(ViewGroup parent)
	{
		View ret = super.onCreateView(parent);

		View summary = ret.findViewById(android.R.id.summary);
		if (summary != null)
		{
			ViewParent summaryParent = summary.getParent();
			if (summaryParent instanceof ViewGroup)
			{
				final LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				ViewGroup summaryParent2 = (ViewGroup) summaryParent;
				layoutInflater.inflate(R.layout.seek_bar_preference, summaryParent2);

				seekBar = (SeekBar) summaryParent2.findViewById(R.id.seekBar);
				seekBar.setMax(maxValue - minValue);
				seekBar.setOnSeekBarChangeListener(this);

				statusText = (TextView) summaryParent2.findViewById(R.id.seekBarPrefValue);

				unitsRightView = (TextView) summaryParent2.findViewById(R.id.seekBarPrefUnitsRight);
				unitsLeftView = (TextView) summaryParent2.findViewById(R.id.seekBarPrefUnitsLeft);
			}
		}

		return ret;
	}

	@Override
	public void onBindView(View view)
	{
		super.onBindView(view);
		updateView();
	}

	protected void updateView()
	{
		if (statusText != null)
		{
			statusText.setText(String.valueOf(currentValue));
			statusText.setMinimumWidth(30);
		}

		if (seekBar != null) seekBar.setProgress(currentValue - minValue);

		if (unitsRightView != null) unitsRightView.setText(unitsRight);
		if (unitsLeftView != null) unitsLeftView.setText(unitsLeft);
	}

	//region OnSeekBarChangeListener interface
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
	{
		int newValue = progress + minValue;

		if (newValue > maxValue) newValue = maxValue;
		else if (newValue < minValue) newValue = minValue;
		else if (interval != 1 && newValue % interval != 0) newValue = Math.round(((float) newValue) / interval) * interval;

		// change rejected, revert to the previous value
		if (!callChangeListener(newValue))
		{
			seekBar.setProgress(currentValue - minValue);
			return;
		}

		// change accepted, store it
		currentValue = newValue;
		if (statusText != null) statusText.setText(String.valueOf(newValue));
		persistInt(newValue);
	}

	public void onStartTrackingTouch(SeekBar seekBar)
	{
	}

	public void onStopTrackingTouch(SeekBar seekBar)
	{
		notifyChanged();
	}
	//endregion

	@Override
	protected Object onGetDefaultValue(TypedArray ta, int index)
	{
		int defaultValue = ta.getInt(index, DEFAULT_VALUE);
		return defaultValue;
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue)
	{
		if (restoreValue)
		{
			currentValue = getPersistedInt(currentValue);
		}
		else
		{
			int temp = 0;
			if (defaultValue instanceof Integer) temp = (Integer) defaultValue;

			persistInt(temp);
			currentValue = temp;
		}
	}

	private static String getAttributeStringValue(AttributeSet attrs, String namespace, String name, String defaultValue)
	{
		String value = attrs.getAttributeValue(namespace, name);
		if (value == null) value = defaultValue;

		return value;
	}
}
