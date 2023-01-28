package com.app.helper.Views.Followed;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.helper.Followed.Model.Followed;
import com.app.helper.Location.FamiliarPlace.DAO.FamiliarPlaceDAO;
import com.app.helper.Location.FamiliarPlace.Model.FamiliarPlace;
import com.app.helper.R;
import com.app.helper.Utils.ViewUtils;
import com.app.helper.Views.Adapters.RV_FamiliarPlaceAdapter;

import java.util.ArrayList;
import java.util.List;

public class FamiliarPlaceActivity extends AppCompatActivity {
    public static final String KEY_DATA = "KEY_DATA";
    public static final String KEY_INDEX = "KEY_INDEX";

    public static final int KEY_CODE_EDIT = 77;
    public static final int KEY_CODE_INSERT = 69;

    private final Activity activity = this;
    private TextView tv_empty_list;
    private TextView tv_title_detail;
    private RecyclerView rv_fp;
    private Button btn_add, btn_confirm;
    private LinearLayout ln_pbar;

    private RV_FamiliarPlaceAdapter rv_familiarPlaceAdapter;

    private Followed followed_current;
    private FamiliarPlaceDAO familiarPlaceDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_familiar_place);
        followed_current = (Followed) getIntent().getSerializableExtra(FollowActivity.KEY_FOLLOWED);
        if (followed_current == null) finish();

        getWidgets();
        setListeners();

        familiarPlaceDAO = new FamiliarPlaceDAO(this, followed_current);
        initFamiliarPlaceList();

    }

    private void getWidgets() {
        TextView tv_name = findViewById(R.id.tv_name);
        tv_name.setText(followed_current.getName());
        TextView tv_phone = findViewById(R.id.tv_phone);
        tv_phone.setText(followed_current.getUid());
        tv_empty_list = findViewById(R.id.tv_empty_list);
        tv_title_detail = findViewById(R.id.tv_title_detail);

        rv_fp = findViewById(R.id.rv_fp);
        ln_pbar = findViewById(R.id.ln_pbar);

        btn_add = findViewById(R.id.btn_add);
        btn_confirm = findViewById(R.id.btn_confirm);
    }


    private void setListeners() {
        btn_add.setOnClickListener(v -> activity.startActivityForResult(new Intent(activity, FamiliarPlaceMapActivity.class), KEY_CODE_INSERT));

        btn_confirm.setOnClickListener(v -> {
            familiarPlaceDAO.updateValues(mFamiliarPlaces);
            Toast.makeText(activity, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
            rv_familiarPlaceAdapter.changeAll();
            v.setVisibility(View.INVISIBLE);
            v.setEnabled(false);
        });
    }

    private List<FamiliarPlace> mFamiliarPlaces;

    private boolean isShow;

    private void initFamiliarPlaceList() {
        ViewUtils.progressBarProcess(true, ln_pbar, activity);

        mFamiliarPlaces = new ArrayList<>();
        rv_fp.setLayoutManager(new LinearLayoutManager(activity, RecyclerView.VERTICAL, false));
        rv_familiarPlaceAdapter = new RV_FamiliarPlaceAdapter(mFamiliarPlaces, activity, new RV_FamiliarPlaceAdapter.IListener() {
            @Override
            public void remove(String id, int position) {
                familiarPlaceDAO.removeValue(id, new FamiliarPlaceDAO.IControlData() {
                    @Override
                    public void isOK(boolean isOK) {
                        if (isOK) {
                            rv_familiarPlaceAdapter.onRemove(position);
                            if (mFamiliarPlaces.isEmpty()) {
                                tv_title_detail.setVisibility(View.GONE);
                                tv_empty_list.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });
            }
        });
        rv_fp.setAdapter(rv_familiarPlaceAdapter);
        familiarPlaceDAO.getFamiliarPlace(new FamiliarPlaceDAO.IControlData() {
            @Override
            public void familiarPlace(FamiliarPlace familiarPlace) {
                if (!isShow) {
                    isShow = true;
                    ViewUtils.progressBarProcess(false, ln_pbar, activity);
                }
                if (familiarPlace != null) {
                    rv_familiarPlaceAdapter.onInsert(familiarPlace);
                    tv_empty_list.setVisibility(View.GONE);
                    tv_title_detail.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void back(View view) {
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == KEY_CODE_EDIT) {
            Log.e("onActivityResult", "KEY_CODE_EDIT");
            if (data != null) {
                if (resultCode == Activity.RESULT_OK) {
                    FamiliarPlace familiarPlace = (FamiliarPlace) data.getSerializableExtra(KEY_DATA);
                    int position = data.getIntExtra(KEY_INDEX, -1);
                    if (position != -1) {
                        rv_familiarPlaceAdapter.onChange(position, familiarPlace);
                    }
                }
            }
        } else if (requestCode == KEY_CODE_INSERT) {
            Log.e("onActivityResult", "KEY_CODE_INSERT");
            if (data != null) {
                if (resultCode == Activity.RESULT_OK) {
                    FamiliarPlace familiarPlace = (FamiliarPlace) data.getSerializableExtra(KEY_DATA);
                    if (familiarPlace != null) {
                        familiarPlaceDAO.addValue(familiarPlace, new FamiliarPlaceDAO.IControlData() {
                            @Override
                            public void isOK(boolean isOK) {
                                Toast.makeText(activity, "Thêm thành công", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        familiarPlaceDAO.removeListener();
    }
}