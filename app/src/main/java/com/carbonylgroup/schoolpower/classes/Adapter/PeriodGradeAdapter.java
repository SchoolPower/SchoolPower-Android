/**
 * Copyright (C) 2017 Gustav Wang
 */

package com.carbonylgroup.schoolpower.classes.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.carbonylgroup.schoolpower.R;
import com.carbonylgroup.schoolpower.R2;
import com.carbonylgroup.schoolpower.classes.ListItems.PeriodGradeItem;
import com.carbonylgroup.schoolpower.classes.Utils.Utils;

public class PeriodGradeAdapter extends RecyclerView.Adapter<PeriodGradeAdapter.PeriodGradeViewHolder> {

    private LayoutInflater inflater;
    private List<PeriodGradeItem> periodGradeItemList;
    private Context context;
    private Utils utils;

    public PeriodGradeAdapter(Context context, ArrayList<PeriodGradeItem> objects) {

        inflater = LayoutInflater.from(context);
        this.periodGradeItemList = objects;
        this.context = context;
        utils = new Utils(context);
    }

    @Override
    public PeriodGradeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.period_grade_list_item, parent, false);
        return new PeriodGradeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PeriodGradeViewHolder viewHolder, int position) {

        PeriodGradeItem item = getList().get(position);
        viewHolder.period_indicator.setText(item.getTermIndicator());
        viewHolder.period_latter_grade.setText(item.getTermLetterGrade());
        viewHolder.period_percentage_grade.setText(item.getTermPercentageGrade());
        viewHolder.period_grade_background.setBackgroundColor(utils.getColorByPeriodItem(context, item));
    }

    @Override
    public int getItemCount() {
        return getList() == null ? 0 : getList().size();
    }

    private List<PeriodGradeItem> getList() {
        return this.periodGradeItemList;
    }

    public static class PeriodGradeViewHolder extends RecyclerView.ViewHolder {

        @BindView(R2.id.period_indicator) TextView period_indicator;
        @BindView(R2.id.period_latter_grade) TextView period_latter_grade;
        @BindView(R2.id.period_percentage_grade) TextView period_percentage_grade;
        @BindView(R2.id.period_grade_background) LinearLayout period_grade_background;

        PeriodGradeViewHolder(View view) {

            super(view);
            ButterKnife.bind(this, view);
        }
    }
}