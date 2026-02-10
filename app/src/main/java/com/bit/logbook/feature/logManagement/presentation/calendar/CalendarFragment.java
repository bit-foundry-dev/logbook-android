package com.bit.logbook.feature.logManagement.presentation.calendar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bit.logbook.R;
import com.bit.logbook.core.utils.Constants;
import com.bit.logbook.core.utils.DateUtils;
import com.bit.logbook.feature.logManagement.presentation.today.TodayFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.time.LocalDate;
import java.util.Calendar;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CalendarFragment extends Fragment {

    private CalendarView calendarView;
    private TextView yearTextView, selectedDateText;

    private final Calendar currentCalendar = Calendar.getInstance();
    private final Calendar absoluteMinCalendar = Calendar.getInstance();
    private final Calendar absoluteMaxCalendar = Calendar.getInstance();
    private final Calendar todayCalendar = Calendar.getInstance();

    private int displayedYear;
    private final int currentYear = todayCalendar.get(Calendar.YEAR);

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupAbsoluteDateBoundaries();
        setupCalendarView();
        setupYearPicker();

        // Load initial fragment with today's date
        loadFragmentForDate(currentCalendar);
    }

    private void initializeViews(@NonNull View view) {
        calendarView = view.findViewById(R.id.calendarView);
        yearTextView = view.findViewById(R.id.year_text_view);
        selectedDateText = view.findViewById(R.id.selected_date_text);
    }

    private void setupAbsoluteDateBoundaries() {
        // Set absolute max date to today (block all future dates)
        absoluteMaxCalendar.setTimeInMillis(System.currentTimeMillis());

        // Set absolute min date (e.g., 10 years back)
        absoluteMinCalendar.setTimeInMillis(System.currentTimeMillis());
        absoluteMinCalendar.add(Calendar.YEAR, -Constants.YEARS_BACK_LIMIT);
        absoluteMinCalendar.set(Calendar.MONTH, Calendar.JANUARY);
        absoluteMinCalendar.set(Calendar.DAY_OF_MONTH, 1);
        absoluteMinCalendar.set(Calendar.HOUR_OF_DAY, 0);
        absoluteMinCalendar.set(Calendar.MINUTE, 0);
        absoluteMinCalendar.set(Calendar.SECOND, 0);
        absoluteMinCalendar.set(Calendar.MILLISECOND, 0);

        // Initialize current calendar to today
        currentCalendar.setTimeInMillis(System.currentTimeMillis());
        displayedYear = currentCalendar.get(Calendar.YEAR);
    }

    private void setupCalendarView() {
        // Set initial date
        calendarView.setDate(currentCalendar.getTimeInMillis(), false, false);

        // Apply year-specific boundaries
        updateYearBoundaries(displayedYear);

        // Date selection listener
        calendarView.setOnDateChangeListener(this::onDateSelected);
    }

    private void setupYearPicker() {
        // Year text view click - open NumberPicker dialog
        yearTextView.setOnClickListener(v -> showYearPickerDialog());
    }

    private void updateYearBoundaries(int targetYear) {
        Calendar yearStart = Calendar.getInstance();
        Calendar yearEnd = Calendar.getInstance();

        int minYear = absoluteMinCalendar.get(Calendar.YEAR);

        if (targetYear == currentYear) {
            // Current year: Show from January 1 to today
            yearStart.set(targetYear, Calendar.JANUARY, 1, 0, 0, 0);
            yearStart.set(Calendar.MILLISECOND, 0);
            yearEnd.setTimeInMillis(absoluteMaxCalendar.getTimeInMillis());

        } else if (targetYear == minYear) {
            // Minimum year: Show from absolute min date to December 31
            yearStart.setTimeInMillis(absoluteMinCalendar.getTimeInMillis());
            yearEnd.set(targetYear, Calendar.DECEMBER, 31, 23, 59, 59);
            yearEnd.set(Calendar.MILLISECOND, 999);

        } else {
            // Any other past year: Show full year (January 1 to December 31)
            yearStart.set(targetYear, Calendar.JANUARY, 1, 0, 0, 0);
            yearStart.set(Calendar.MILLISECOND, 0);
            yearEnd.set(targetYear, Calendar.DECEMBER, 31, 23, 59, 59);
            yearEnd.set(Calendar.MILLISECOND, 999);
        }

        // Apply the calculated boundaries to CalendarView
        calendarView.setMinDate(yearStart.getTimeInMillis());
        calendarView.setMaxDate(yearEnd.getTimeInMillis());

        // Update year display
        updateYearDisplay();
    }

    private void showYearPickerDialog() {
        int minYear = absoluteMinCalendar.get(Calendar.YEAR);
        int maxYear = absoluteMaxCalendar.get(Calendar.YEAR);

        // Inflate custom dialog layout
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_year_picker, null);

        NumberPicker yearPicker = dialogView.findViewById(R.id.year_picker);

        // Configure NumberPicker
        yearPicker.setMinValue(minYear);
        yearPicker.setMaxValue(maxYear);
        yearPicker.setValue(displayedYear);
        yearPicker.setWrapSelectorWheel(false);

        // Create and show dialog
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.select_year)
                .setView(dialogView)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    int selectedYear = yearPicker.getValue();
                    if (selectedYear != displayedYear) {
                        navigateToYear(selectedYear);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void onDateSelected(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
        currentCalendar.set(year, month, dayOfMonth);

        // If user somehow selected a different year (by swiping), update displayed year
        if (displayedYear != year) {
            displayedYear = year;
            updateYearDisplay();
        }

        // Load fragment for the selected date
        loadFragmentForDate(currentCalendar);
    }

    private void navigateToYear(int targetYear) {
        displayedYear = targetYear;

        if (targetYear == currentYear) {
            // If navigating to current year, show today's date
            currentCalendar.setTimeInMillis(todayCalendar.getTimeInMillis());
        } else {
            // If navigating to past year, show January 1st
            currentCalendar.set(Calendar.YEAR, targetYear);
            currentCalendar.set(Calendar.MONTH, Calendar.JANUARY);
            currentCalendar.set(Calendar.DAY_OF_MONTH, 1);
        }

        // First, set the date without animation
        calendarView.setDate(currentCalendar.getTimeInMillis(), false, false);

        // Then update year-specific boundaries
        updateYearBoundaries(targetYear);

        // Force calendar to refresh by setting date again with animation
        calendarView.post(() -> calendarView.setDate(currentCalendar.getTimeInMillis(), true, true));

        // Load fragment for the new date
        loadFragmentForDate(currentCalendar);
    }

    private void updateYearDisplay() {
        yearTextView.setText(String.valueOf(displayedYear));
    }

    private void loadFragmentForDate(Calendar selectedDate) {
        selectedDateText.setText(DateUtils.formatFullDate(selectedDate));
        TodayFragment fragment = new TodayFragment(LocalDate.of(
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH) + 1,
                selectedDate.get(Calendar.DAY_OF_MONTH)
        ));

        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}