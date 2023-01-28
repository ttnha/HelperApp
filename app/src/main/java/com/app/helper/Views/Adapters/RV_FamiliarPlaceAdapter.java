package com.app.helper.Views.Adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.helper.Location.FamiliarPlace.Model.FPDate;
import com.app.helper.Location.FamiliarPlace.Model.FamiliarPlace;
import com.app.helper.R;
import com.app.helper.Utils.ViewUtils;
import com.app.helper.Views.Followed.FamiliarPlaceActivity;
import com.app.helper.Views.Followed.FamiliarPlaceMapActivity;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RV_FamiliarPlaceAdapter extends RecyclerView.Adapter<RV_FamiliarPlaceAdapter.FamiliarPlaceHolder> {
    private final List<FamiliarPlace> familiarPlaceList;
    private final Activity activity;
    private final List<String> tmpList;

    private final Button btn_confirm;

    private final IListener iListener;

    public interface IListener {
        default void remove(String id, int position) {
        }
    }

    public RV_FamiliarPlaceAdapter(List<FamiliarPlace> familiarPlaceList, Activity activity, IListener iListener) {
        this.familiarPlaceList = familiarPlaceList;
        this.tmpList = new ArrayList<>();
        this.activity = activity;
        this.btn_confirm = activity.findViewById(R.id.btn_confirm);
        this.iListener = iListener;
    }

    public void changeAll() {
        this.tmpList.clear();
        this.familiarPlaceList.forEach(v -> tmpList.add(v.toString()));
    }

    public void onChange(int position, FamiliarPlace familiarPlace) {
        this.tmpList.set(position, familiarPlace.toString());
        this.familiarPlaceList.set(position, familiarPlace);
        this.btn_confirm.setVisibility(View.VISIBLE);
        this.btn_confirm.setEnabled(true);
        this.notifyItemChanged(position);
    }

    public void onInsert(FamiliarPlace familiarPlace) {
        this.tmpList.add(familiarPlace.toString());
        this.familiarPlaceList.add(familiarPlace);
        this.notifyItemInserted(familiarPlaceList.size() - 1);
    }

    public void onRemove(int position) {
        this.familiarPlaceList.remove(position);
        this.notifyDataSetChanged();
        Toast.makeText(activity, "Xóa thành công", Toast.LENGTH_SHORT).show();
    }

    @NonNull
    @Override
    public FamiliarPlaceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FamiliarPlaceHolder(LayoutInflater.from(activity).inflate(R.layout.rv_item_familiar_place, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull FamiliarPlaceHolder holder, int position) {
        FamiliarPlace familiarPlace = familiarPlaceList.get(position);
        if (familiarPlace != null) {
            boolean isStatus = familiarPlace.isStatus();
            holder.sw_notification.setChecked(isStatus);
            boolean isIs_interval = familiarPlace.isIs_interval();
            holder.sw_interval.setChecked(isIs_interval);
            holder.sw_interval.setEnabled(isStatus);
            holder.rl_main.setBackgroundColor(isStatus ? activity.getColor(R.color.green_light) : activity.getColor(R.color.red_light));
            holder.rl_main.setOnClickListener(v -> {
                Intent intent = new Intent(activity, FamiliarPlaceMapActivity.class);
                intent.putExtra(FamiliarPlaceActivity.KEY_DATA, familiarPlace);
                intent.putExtra(FamiliarPlaceActivity.KEY_INDEX, position);
                activity.startActivityForResult(intent, FamiliarPlaceActivity.KEY_CODE_EDIT);
            });

            FPDate fpDate = familiarPlace.getFp_date();
            holder.tv_place_name.setText(": " + familiarPlace.getPlace_name());
            holder.tv_place_date.setText(": + " + fpDate.getDate());
            holder.tv_place_date_time.setText("  + " + fpDate.getTime_start() + " -> " + fpDate.getTime_end());
            if (isIs_interval) {
                String dayOfWeek = getDayOfWeek(familiarPlace.getFp_date().convertDate());
                holder.tv_interval.setText(": " + dayOfWeek + " hàng tuần");
            } else {
                holder.tv_interval.setText(": Không");

            }
            holder.tv_radius.setText(": " + familiarPlace.getRadius() + " (m)");
            switchListeners(holder.sw_notification, holder.sw_interval, position);

            holder.ll_remove.setOnClickListener(v -> ViewUtils.showDialogConfirm("Cảnh báo!", "Bạn muốn xóa lịch này?", activity, is -> {
                if (is) {
                    iListener.remove(familiarPlace.getId(), position);
                }
            }));
        }
    }

    private String getDayOfWeek(int[] date) {
        String rs;
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, date[0]);
        calendar.set(Calendar.MONTH, date[1] - 1);
        calendar.set(Calendar.DAY_OF_MONTH, date[2]);
        switch (calendar.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY:
                rs = "Thứ 2";
                break;
            case Calendar.TUESDAY:
                rs = "Thứ 3";
                break;
            case Calendar.WEDNESDAY:
                rs = "Thứ 4";
                break;
            case Calendar.THURSDAY:
                rs = "Thứ 5";
                break;
            case Calendar.FRIDAY:
                rs = "Thứ 6";
                break;
            case Calendar.SATURDAY:
                rs = "Thứ 7";
                break;
            default:
                rs = "Chủ nhật";
                break;
        }
        return rs;
    }

    private void switchListeners(SwitchMaterial sw_notification, SwitchMaterial sw_interval, int position) {
        sw_notification.setOnClickListener(v -> {
            boolean isChecked = sw_notification.isChecked();
            FamiliarPlace familiarPlace = familiarPlaceList.get(position);
            familiarPlace.setStatus(isChecked);
            Log.e("STATUS", isChecked + "");
            onItemChangedInSwitch(position);
        });

        sw_interval.setOnClickListener(v -> {
            boolean isChecked = sw_interval.isChecked();
            FamiliarPlace familiarPlace = familiarPlaceList.get(position);
            familiarPlace.setIs_interval(isChecked);
            onItemChangedInSwitch(position);
        });
    }

    private void onItemChangedInSwitch(int position) {
        notifyItemChanged(position);
        if (!isTwoListEqual()) {
            btn_confirm.setVisibility(View.VISIBLE);
            btn_confirm.setEnabled(true);
        } else {
            btn_confirm.setVisibility(View.INVISIBLE);
            btn_confirm.setEnabled(false);
        }

    }

    private boolean isTwoListEqual() {
        if (familiarPlaceList.size() != tmpList.size()) return false;
        int size = familiarPlaceList.size();
        for (int i = 0; i < size; i++)
            if (!familiarPlaceList.get(i).toString().equals(tmpList.get(i))) return false;
        return true;
    }

    @Override
    public int getItemCount() {
        return familiarPlaceList != null ? familiarPlaceList.size() : 0;
    }


    protected static class FamiliarPlaceHolder extends RecyclerView.ViewHolder {
        private final TextView tv_place_name;
        private final TextView tv_place_date;
        private final TextView tv_place_date_time;
        private final TextView tv_interval;
        private final TextView tv_radius;
        private final RelativeLayout rl_main;
        private final SwitchMaterial sw_notification;
        private final SwitchMaterial sw_interval;
        private final LinearLayout ll_remove;

        public FamiliarPlaceHolder(@NonNull View itemView) {
            super(itemView);
            tv_place_name = itemView.findViewById(R.id.tv_place_name);
            tv_place_date = itemView.findViewById(R.id.tv_place_date);
            tv_place_date_time = itemView.findViewById(R.id.tv_place_date_time);
            tv_interval = itemView.findViewById(R.id.tv_interval);
            tv_radius = itemView.findViewById(R.id.tv_radius);

            rl_main = itemView.findViewById(R.id.rl_main);

            // Sub item
            sw_interval = itemView.findViewById(R.id.sw_interval);
            sw_notification = itemView.findViewById(R.id.sw_notification);
            ll_remove = itemView.findViewById(R.id.ll_remove);
        }
    }
}
