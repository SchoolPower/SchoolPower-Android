/**
 * Copyright (C) 2017 Gustav Wang
 */

package com.carbonylgroup.schoolpower.classes.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.carbonylgroup.schoolpower.R;
import com.carbonylgroup.schoolpower.R2;
import com.carbonylgroup.schoolpower.classes.ListItems.AssignmentItem;
import com.carbonylgroup.schoolpower.classes.ListItems.MainListItem;
import com.carbonylgroup.schoolpower.classes.ListItems.PeriodGradeItem;
import com.carbonylgroup.schoolpower.classes.Utils.Utils;

public class CourseDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int HEADER_VIEW = 1;
    private static final int FOOTER_VIEW = 2;
    private int presentingTermPos = 0;
    private Utils utils;
    private Context context;
    private MainListItem item;
    private ArrayList<String> termsList;
    private List<AssignmentItem> assignmentItemList;

    public CourseDetailAdapter(Context context, MainListItem mainListItem) {

        this.item = mainListItem;
        this.context = context;
        utils = new Utils(context);
        initTermList();

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;

        if (viewType == HEADER_VIEW) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.course_detail_header, parent, false);
            return new HeaderViewHolder(view);
        }

        if (viewType == FOOTER_VIEW) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.course_detail_footer, parent, false);
            return new FooterViewHolder(view);
        }

        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.course_detail_assignment_item, parent, false);
        return new NormalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        try {
            if (holder instanceof NormalViewHolder) {

                NormalViewHolder normalViewHolder = (NormalViewHolder) holder;

                Collections.sort(assignmentItemList, new Comparator<AssignmentItem>() {
                    public int compare(AssignmentItem o1, AssignmentItem o2) {
                        return o2.getAssignmentDate().compareTo(o1.getAssignmentDate());
                    }
                });

                AssignmentItem assignmentItem = assignmentItemList.get(position - 1);
                normalViewHolder.detail_assignment_name_tv.setText(assignmentItem.getAssignmentTitle());
                normalViewHolder.detail_assignment_date_tv.setText(assignmentItem.getAssignmentDate());
                normalViewHolder.detail_assignment_percentage_tv.setText(assignmentItem.getAssignmentPercentage());
                normalViewHolder.detail_assignment_dividing_score_tv.setText(assignmentItem.getAssignmentDividedScore());
                Log.d("[][][", String.valueOf(assignmentItem.isNew()));
                if(assignmentItem.isNew()) normalViewHolder.detail_assignment_grade_background.setBackgroundColor(context.getResources().getColor(R.color.accent));
                else normalViewHolder.detail_assignment_grade_background.setBackgroundColor(utils.getColorByLetterGrade(context, assignmentItem.getAssignmentGrade()));

            } else if (holder instanceof HeaderViewHolder) {

                final HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
                final ArrayAdapter<String> termAdapter = new ArrayAdapter<>(context, R.layout.term_selection_spinner, termsList);

                headerViewHolder.detail_letter_grade_tv.setText(item.getLetterGrade());
                headerViewHolder.detail_percentage_grade_tv.setText(item.getPercentageGrade());
                headerViewHolder.detail_header_teacher_name_tv.setText(item.getTeacherName());
                headerViewHolder.detail_header_block_tv.setText(context.getString(R.string.block) + " " + item.getBlockLetter());
                headerViewHolder.detail_header_room_tv.setText(context.getString(R.string.room) + " " + item.getRoomNumber());
                headerViewHolder.detail_header_term_tv.setText(context.getString(R.string.term) + " " + item.getTermNumber());
                headerViewHolder.detail_header_grade_background.setBackgroundColor(utils.getColorByLetterGrade(context, item.getLetterGrade()));
                headerViewHolder.detail_term_select_spinner.setAdapter(termAdapter);
                headerViewHolder.detail_term_select_spinner.setSelection(presentingTermPos);
                headerViewHolder.detail_term_select_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                        String selectItem = termsList.get(position);
                        if (position != presentingTermPos) {
                            presentingTermPos = position;
                            if (selectItem.equals(context.getString(R.string.all_terms)))
                                setAllTerms(termsList);
                            else setTerm(selectItem, false);
                            refreshAdapter();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {

        if (getList() == null) return 0;
        if (getList().size() == 0) return 2;
        return getList().size() + 2;
    }

    @Override
    public int getItemViewType(int position) {

        if (position == 0) return HEADER_VIEW;
        if (position == getItemCount() - 1) return FOOTER_VIEW;
        return super.getItemViewType(position);
    }

    private void initTermList() {

        ArrayList<PeriodGradeItem> periodGradeItemList = item.getPeriodGradeItemArrayList();
        termsList = new ArrayList<>();
        termsList.add(context.getString(R.string.all_terms));
        for (int i = 0; i < periodGradeItemList.size(); i++)
            termsList.add(periodGradeItemList.get(i).getTermIndicator());
        setAllTerms(termsList);
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        private ViewHolder(View itemView) {
            super(itemView);
        }
    }

    public class HeaderViewHolder extends ViewHolder {

        @BindView(R2.id.detail_header_grades_bt) Button detail_header_grades_bt;
        @BindView(R2.id.detail_header_attendance_bt) Button detail_header_attendance_bt;
        @BindView(R2.id.detail_letter_grade_tv) TextView detail_letter_grade_tv;
        @BindView(R2.id.detail_percentage_grade_tv) TextView detail_percentage_grade_tv;
        @BindView(R2.id.detail_header_teacher_name_tv) TextView detail_header_teacher_name_tv;
        @BindView(R2.id.detail_header_block_tv) TextView detail_header_block_tv;
        @BindView(R2.id.detail_header_room_tv) TextView detail_header_room_tv;
        @BindView(R2.id.detail_header_term_tv) TextView detail_header_term_tv;
        @BindView(R2.id.detail_header_grade_background) RelativeLayout detail_header_grade_background;
        @BindView(R2.id.detail_term_select_spinner) Spinner detail_term_select_spinner;

        private HeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    private class FooterViewHolder extends ViewHolder {

        private FooterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public class NormalViewHolder extends ViewHolder {

        @BindView(R2.id.detail_assignment_name_tv) TextView detail_assignment_name_tv;
        @BindView(R2.id.detail_assignment_date_tv) TextView detail_assignment_date_tv;
        @BindView(R2.id.detail_assignment_percentage_tv) TextView detail_assignment_percentage_tv;
        @BindView(R2.id.detail_assignment_dividing_score_tv) TextView detail_assignment_dividing_score_tv;
        @BindView(R2.id.detail_assignment_grade_background) RelativeLayout detail_assignment_grade_background;

        private NormalViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
        }
    }

    private List<AssignmentItem> getList() {
        return this.assignmentItemList;
    }

    private void refreshAdapter() {
        this.notifyDataSetChanged();
    }

    private void setTerm(String term, boolean twoTerms) {

        assignmentItemList = new ArrayList<>();
        ArrayList<AssignmentItem> allAssignments = item.getAssignmentItemArrayList();

        if (twoTerms) {
            String[] terms = term.split("&");
            for (int i = 0; i < allAssignments.size(); i++) {
                if (allAssignments.get(i).getAssignmentTerm().equals(terms[0]) || allAssignments.get(i).getAssignmentTerm().equals(terms[1]))
                    assignmentItemList.add(allAssignments.get(i));
            }

        } else for (int i = 0; i < allAssignments.size(); i++)
            if (allAssignments.get(i).getAssignmentTerm().equals(term))
                assignmentItemList.add(allAssignments.get(i));

    }

    private void setAllTerms(ArrayList termsList) {
        if (termsList.contains("Y1")) setTerm("Y1", false);
        else if (termsList.contains("S1") && termsList.contains("S2")) setTerm("S1&S2", true);
        else if (termsList.contains("S1")) setTerm("S1", false);
        else if (termsList.contains("S2")) setTerm("S2", false);
        else setTerm(termsList.get(1).toString(), false);
    }
}