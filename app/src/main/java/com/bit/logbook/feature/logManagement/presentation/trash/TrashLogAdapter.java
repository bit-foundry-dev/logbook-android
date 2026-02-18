package com.bit.logbook.feature.logManagement.presentation.trash;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bit.logbook.R;
import com.bit.logbook.core.utils.DateUtils;
import com.bit.logbook.feature.logManagement.domain.entity.Log;
import com.github.vipulasri.timelineview.TimelineView;
import com.google.android.material.chip.Chip;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TrashLogAdapter extends RecyclerView.Adapter<TrashLogAdapter.LogViewHolder> {

    private List<Log> logs = new ArrayList<>();
    private OnLogClickListener clickListener;
    private OnLogLongClickListener longClickListener;

    public List<Log> getLogs() {
        return logs;
    }

    // Click listener interfaces
    public interface OnLogClickListener {
        void onLogClick(Log log, int position);
    }

    public interface OnLogLongClickListener {
        void onLogLongClick(View view, Log log, int position);
    }

    // Setters for click listeners
    public void setOnLogClickListener(OnLogClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnLogLongClickListener(OnLogLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void setLogs(List<Log> newLogs) {
        if (newLogs == null) {
            newLogs = new ArrayList<>();
        }

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new LogDiffCallback(this.logs, newLogs));
        this.logs.clear();
        this.logs.addAll(newLogs);
        diffResult.dispatchUpdatesTo(this);
    }

    public void addLogs(List<Log> newLogs) {
        if (newLogs != null && !newLogs.isEmpty()) {
            int startPosition = this.logs.size();
            this.logs.addAll(newLogs);
            notifyItemRangeInserted(startPosition, newLogs.size());
        }
    }

    public void clearLogs() {
        int size = this.logs.size();
        this.logs.clear();
        notifyItemRangeRemoved(0, size);
    }

    public Log getLog(int position) {
        if (position >= 0 && position < logs.size()) {
            return logs.get(position);
        }
        return null;
    }

    public void removeLog(int position) {
        if (position >= 0 && position < logs.size()) {
            logs.remove(position);
            notifyItemRemoved(position);
        }
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trash, parent, false);
        return new LogViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        Log log = logs.get(position);
        holder.bind(log, position);
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    @Override
    public int getItemViewType(int position) {
        return TimelineView.getTimeLineViewType(position, getItemCount());
    }


    public class LogViewHolder extends RecyclerView.ViewHolder {
        private final TextView trashTime;
        private final TextView trashDate;
        private final TextView trashTitle;
        private final TextView trashDescription;
        private final Chip trashTagChip;

        public LogViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            trashTime = itemView.findViewById(R.id.trash_time);
            trashDate = itemView.findViewById(R.id.trash_date);
            trashTitle = itemView.findViewById(R.id.trash_title);
            trashDescription = itemView.findViewById(R.id.trash_description);
            trashTagChip = itemView.findViewById(R.id.trash_chip_tag);
        }

        public void bind(Log log, int position) {
            // Bind time
            if (log.getStartDate() != null) {
                trashTime.setText(DateUtils.formatTime(log.getStartDate()));
                trashDate.setText(DateUtils.formatFullDate(log.getStartDate().toLocalDate()));
            } else {
                trashTime.setText("--:--");
                trashDate.setText("__/__/____");
            }

            // Bind title
            String title = log.getTitle();
            if (title == null || title.trim().isEmpty()) {
                trashTitle.setText(R.string.untitled);
            } else {
                trashTitle.setText(title);
            }

            // Bind description
            String description = log.getDescription();
            if (description == null || description.trim().isEmpty()) {
                trashDescription.setVisibility(View.GONE);
            } else {
                trashDescription.setVisibility(View.VISIBLE);
                trashDescription.setText(description.replace("\n", " "));
            }

            // Bind tag (optional - if you add chip to layout)
            if (trashTagChip != null) {
                String tag = log.getTag();
                if (tag != null && !tag.trim().isEmpty()) {
                    trashTagChip.setVisibility(View.VISIBLE);
                    trashTagChip.setText(tag);
                } else {
                    trashTagChip.setVisibility(View.GONE);
                }
            }

            // Set click listener
            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onLogClick(log, position);
                }
            });

            // Set long click listener
            itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onLogLongClick(v, log, position);
                    return true;
                }
                return false;
            });
        }
    }


    private static class LogDiffCallback extends DiffUtil.Callback {
        private final List<Log> oldList;
        private final List<Log> newList;

        public LogDiffCallback(List<Log> oldList, List<Log> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            // Compare by ID
            Log oldLog = oldList.get(oldItemPosition);
            Log newLog = newList.get(newItemPosition);
            return oldLog.getId().equals(newLog.getId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            // Compare all fields
            Log oldLog = oldList.get(oldItemPosition);
            Log newLog = newList.get(newItemPosition);

            boolean titleSame = (oldLog.getTitle() == null && newLog.getTitle() == null) ||
                    (oldLog.getTitle() != null && oldLog.getTitle().equals(newLog.getTitle()));

            boolean descriptionSame = (oldLog.getDescription() == null && newLog.getDescription() == null) ||
                    (oldLog.getDescription() != null && oldLog.getDescription().equals(newLog.getDescription()));

            boolean tagSame = (oldLog.getTag() == null && newLog.getTag() == null) ||
                    (oldLog.getTag() != null && oldLog.getTag().equals(newLog.getTag()));

            boolean dateSame = (oldLog.getStartDate() == null && newLog.getStartDate() == null) ||
                    (oldLog.getStartDate() != null && oldLog.getStartDate().equals(newLog.getStartDate()));

            return titleSame && descriptionSame && tagSame && dateSame;
        }
    }
}