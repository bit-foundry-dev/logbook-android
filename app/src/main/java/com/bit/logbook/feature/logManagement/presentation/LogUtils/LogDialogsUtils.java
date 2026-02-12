package com.bit.logbook.feature.logManagement.presentation.LogUtils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.bit.logbook.R;
import com.bit.logbook.core.utils.DateUtils;
import com.bit.logbook.feature.logManagement.domain.entity.Log;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;

import java.time.format.DateTimeFormatter;

public class LogDialogsUtils {

    public static void showLogDetailsBottomSheet(Context context, Log log) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);

        View bottomSheetView = LayoutInflater.from(context)
                .inflate(R.layout.bottom_sheet_log_details, null);

        TextView titleView = bottomSheetView.findViewById(R.id.detail_title);
        TextView dateView = bottomSheetView.findViewById(R.id.detail_date);
        TextView timeView = bottomSheetView.findViewById(R.id.detail_time);
        Chip tagChip = bottomSheetView.findViewById(R.id.detail_tag);  // Changed to Chip
        TextView descriptionView = bottomSheetView.findViewById(R.id.detail_description);
        View divider = bottomSheetView.findViewById(R.id.divider);  // Added divider

        titleView.setText(log.getTitle());
        dateView.setText(DateUtils.formatFullDate(log.getStartDate().toLocalDate()));
        timeView.setText(log.getStartDate().format(DateTimeFormatter.ofPattern("HH:mm")));

        if (log.getTag() != null && !log.getTag().isEmpty()) {
            tagChip.setText(log.getTag());
            tagChip.setVisibility(View.VISIBLE);
        } else {
            tagChip.setVisibility(View.GONE);
        }

        if (log.getDescription() != null && !log.getDescription().isEmpty()) {
            descriptionView.setText(log.getDescription());
            descriptionView.setVisibility(View.VISIBLE);
            divider.setVisibility(View.VISIBLE);
        } else {
            descriptionView.setVisibility(View.GONE);
            divider.setVisibility(View.GONE);
        }

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }
}
