package com.bit.logbook.feature.logManagement.presentation;

import android.content.Context;
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

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder> {

    private final Context context;
    private List<Log> logs = new ArrayList<>();
    private OnLogClickListener clickListener;
    private OnLogLongClickListener longClickListener;

    public LogAdapter(Context context) {
        this.context = context;
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
                .inflate(R.layout.item_timeline, parent, false);
        return new LogViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        Log log = logs.get(position);
        Log nextLog = null;
        boolean isLastItem = (position == logs.size() - 1);

        // For any position except the last, the next log is at position + 1
        if (!isLastItem) {
            nextLog = logs.get(position + 1);
        }

        holder.bind(log, nextLog, isLastItem);
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
        private final TextView timeText;
        private final TextView titleText;
        private final TextView descriptionText;
        private final TextView timeDifferenceText;
        private final Chip tagChip;

        public LogViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            timeText = itemView.findViewById(R.id.text_timeline_time);
            titleText = itemView.findViewById(R.id.text_timeline_title);
            descriptionText = itemView.findViewById(R.id.text_timeline_description);
            timeDifferenceText = itemView.findViewById(R.id.text_time_difference);
            TimelineView timelineView = itemView.findViewById(R.id.timeline);
            tagChip = itemView.findViewById(R.id.chip_tag);

            timelineView.initLine(viewType);
        }

        public void bind(Log log, Log nextLog, boolean isLastItem) {
            // Bind time
            if (log.getStartDate() != null) {
                timeText.setText(DateUtils.formatTime(log.getStartDate()));
            } else {
                timeText.setText("--:--");
            }

            // Bind title
            String title = log.getTitle();
            if (title == null || title.trim().isEmpty()) {
                titleText.setText(R.string.untitled);
            } else {
                titleText.setText(title);
            }

            // Bind description
            String description = log.getDescription();
            if (description == null || description.trim().isEmpty()) {
                descriptionText.setVisibility(View.GONE);
            } else {
                descriptionText.setVisibility(View.VISIBLE);
                descriptionText.setText(description.replace("\n", " "));
            }

            // Bind time difference
            bindTimeDifference(log, nextLog, isLastItem);

            // Bind tag
            if (tagChip != null) {
                String tag = log.getTag();
                if (tag != null && !tag.trim().isEmpty()) {
                    tagChip.setVisibility(View.VISIBLE);
                    tagChip.setText(tag);
                } else {
                    tagChip.setVisibility(View.GONE);
                }
            }

            // Set click listeners
            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onLogClick(log, getAdapterPosition());
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onLogLongClick(v, log, getAdapterPosition());
                    return true;
                }
                return false;
            });
        }

        /**
         * Binds the time difference to the next log.
         * Shows the gap between this log and the next log (chronologically older).
         * Hides for the last item (no next log to compare to).
         */
        private void bindTimeDifference(Log log, Log nextLog, boolean isLastItem) {
            if (log.getStartDate() == null) {
                timeDifferenceText.setVisibility(View.GONE);
                return;
            }

            // Last item has no next log, so hide duration
            if (isLastItem) {
                timeDifferenceText.setVisibility(View.GONE);
                return;
            }

            // Show time difference to the next log
            if (nextLog != null && nextLog.getStartDate() != null) {
                try {
                    // Calculate duration between current log and next log
                    Duration duration = Duration.between(nextLog.getStartDate(), log.getStartDate()).abs();
                    String formattedDuration = formatDuration(duration);

                    if (formattedDuration != null && !formattedDuration.isEmpty()) {
                        timeDifferenceText.setText(formattedDuration);
                        timeDifferenceText.setVisibility(View.VISIBLE);
                    } else {
                        timeDifferenceText.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    timeDifferenceText.setVisibility(View.GONE);
                }
            } else {
                timeDifferenceText.setVisibility(View.GONE);
            }
        }

        /**
         * Formats a duration into a human-readable string.
         * Handles durations from seconds to days.
         *
         * @param duration The duration to format
         * @return Formatted string like "2d 5h", "3h 45m 30s", "< 1s", or null if invalid
         */
        private String formatDuration(Duration duration) {
            if (duration == null) {
                return null;
            }

            long totalSeconds = duration.getSeconds();

            // Less than 1 second
            if (totalSeconds < 1) {
                return "< 1s";
            }

            // Calculate components
            long days = duration.toDays();
            long hours = duration.toHours() % 24;
            long minutes = duration.toMinutes() % 60;
            long seconds = duration.getSeconds() % 60;

            StringBuilder result = new StringBuilder();

            // Add days if present
            if (days > 0) {
                result.append(days).append("d");
            }

            // Add hours if present
            if (hours > 0) {
                if (result.length() > 0) {
                    result.append(" ");
                }
                result.append(hours).append(context.getString(R.string.hour_symbole));
            }

            // Add minutes if less than a day
            if (minutes > 0 && days == 0) {
                if (result.length() > 0) {
                    result.append(" ");
                }
                result.append(minutes).append(context.getString(R.string.minute_symbole));
            }

            // Add seconds only if less than an hour (to avoid clutter)
            if (seconds > 0 && hours == 0 && days == 0) {
                if (result.length() > 0) {
                    result.append(" ");
                }
                result.append(seconds).append(context.getString(R.string.second_symbole));
            }

            return result.length() > 0 ? result.toString() : null;
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
            Log oldLog = oldList.get(oldItemPosition);
            Log newLog = newList.get(newItemPosition);
            return oldLog.getId().equals(newLog.getId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
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