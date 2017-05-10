package com.carbonylgroup.schoolpower.classes.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ramotion.foldingcell.FoldingCell;


import java.util.ArrayList;
import java.util.HashSet;

import com.carbonylgroup.schoolpower.R;
import com.carbonylgroup.schoolpower.classes.ListItems.MainListItem;
import com.carbonylgroup.schoolpower.classes.ListItems.PeriodGradeItem;
import com.carbonylgroup.schoolpower.classes.Utils.Utils;

/**
 * Simple example of ListAdapter for using with Folding Cell
 * Adapter holds indexes of unfolded elements for correct work with default reusable views behavior
 */
public class FoldingCellListAdapter extends ArrayAdapter<MainListItem> {

    private int transformedPosition;

    private Utils utils;
    private Animation fab_in;
    private HashSet<Integer> unfoldedIndexes;
    private ArrayList<MainListItem> mainListItems;
    private View.OnClickListener defaultRequestBtnClickListener;


    public FoldingCellListAdapter(Context context, ArrayList<MainListItem> _mainListItems, HashSet<Integer> indexes, int _transformedPosition) {

        super(context, 0, _mainListItems);
        mainListItems = _mainListItems;
        unfoldedIndexes = indexes;
        transformedPosition = _transformedPosition;
        utils = new Utils(getContext());
    }

    @Override
    @NonNull
    public View getView(final int position, View convertView, ViewGroup parent) {

        MainListItem item = mainListItems.get(position);
        FoldingCell cell = (FoldingCell) convertView;
        ViewHolder viewHolder;

        if (cell == null) {

            viewHolder = new ViewHolder();
            LayoutInflater vi = LayoutInflater.from(getContext());
            cell = (FoldingCell) vi.inflate(R.layout.main_list_item, parent, false);

            viewHolder.fold_letter_grade_tv = (TextView) cell.findViewById(R.id.fold_letter_grade_tv);
            viewHolder.fold_teacher_name_tv = (TextView) cell.findViewById(R.id.fold_teacher_name_tv);
            viewHolder.fold_block_letter_tv = (TextView) cell.findViewById(R.id.fold_block_letter_tv);
            viewHolder.fold_subject_title_tv = (TextView) cell.findViewById(R.id.fold_subject_title_tv);
            viewHolder.unfold_teacher_name_tv = (TextView) cell.findViewById(R.id.unfold_teacher_name_tv);
            viewHolder.unfold_subject_title_tv = (TextView) cell.findViewById(R.id.detail_subject_title_tv);
            viewHolder.fold_percentage_grade_tv = (TextView) cell.findViewById(R.id.fold_percentage_grade_tv);
            viewHolder.unfold_percentage_grade_tv = (TextView) cell.findViewById(R.id.unfold_percentage_grade_tv);
            viewHolder.unfolded_grade_recycler_view = (RecyclerView) cell.findViewById(R.id.unfolded_grade_recycler_view);
            viewHolder.floating_action_button = (FloatingActionButton) cell.findViewById(R.id.floating_action_button);
            viewHolder.fold_grade_background = (RelativeLayout) cell.findViewById(R.id.fold_grade_background);
            viewHolder.unfold_header_view = (RelativeLayout) cell.findViewById(R.id.unfold_header_view);

            if (transformedPosition != -1)
                if (position == transformedPosition) {

                    viewHolder.unfold_header_view.setTransitionName(getContext().getString(R.string.shared_element_course_header));
                    viewHolder.floating_action_button.setTransitionName(getContext().getString(R.string.shared_element_course_fab));
                }

            cell.setTag(viewHolder);

            if (unfoldedIndexes.contains(position)) {

                cell.unfold(true);
                popUpFAB(cell, 300);
            } else cell.fold(true);
        } else {

            if (unfoldedIndexes.contains(position)) {

                cell.unfold(true);
                popUpFAB(cell, 300);
            } else cell.fold(true);
            viewHolder = (ViewHolder) cell.getTag();
        }

        ArrayList<PeriodGradeItem> items = item.getPeriodGradeItemArrayList();
        PeriodGradeAdapter adapter = new PeriodGradeAdapter(getContext(), items);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

        viewHolder.fold_letter_grade_tv.setText(item.getLetterGrade());
        viewHolder.fold_teacher_name_tv.setText(item.getTeacherName());
        viewHolder.fold_block_letter_tv.setText(item.getBlockLetter());
        viewHolder.fold_subject_title_tv.setText(item.getSubjectTitle());
        viewHolder.unfold_teacher_name_tv.setText(item.getTeacherName());
        viewHolder.unfold_subject_title_tv.setText(item.getSubjectTitle());
        viewHolder.fold_percentage_grade_tv.setText(item.getPercentageGrade());
        viewHolder.unfold_percentage_grade_tv.setText(item.getPercentageGrade());
        viewHolder.unfolded_grade_recycler_view.setLayoutManager(layoutManager);
        viewHolder.unfolded_grade_recycler_view.setAdapter(adapter);
        viewHolder.floating_action_button.setOnClickListener(defaultRequestBtnClickListener);
        viewHolder.fold_grade_background.setBackgroundColor(utils.getColorByLetterGrade(getContext(), item.getLetterGrade()));
        viewHolder.unfold_header_view.setBackgroundColor(utils.getColorByLetterGrade(getContext(), item.getLetterGrade()));

        return cell;
    }

    private void initAnim(int _delay) {

        fab_in = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        fab_in.setDuration(200);
        fab_in.setStartOffset(_delay);
        fab_in.setInterpolator(new DecelerateInterpolator());
    }

    public void refreshPeriodRecycler(FoldingCell _cell, int transformedPosition) {

        final ArrayList<PeriodGradeItem> items = mainListItems.get(transformedPosition).getPeriodGradeItemArrayList();
        final PeriodGradeAdapter adapter = new PeriodGradeAdapter(getContext(), items);
        ((RecyclerView) _cell.findViewById(R.id.unfolded_grade_recycler_view)).setAdapter(adapter);
    }

    public void setMainListItems(ArrayList<MainListItem> _mainListItems){
        mainListItems = _mainListItems;
    }

    private void popUpFAB(FoldingCell _cell, int _delay) {

        initAnim(_delay);
        _cell.findViewById(R.id.floating_action_button).startAnimation(fab_in);
    }

    public void registerToggle(int position) {

        if (unfoldedIndexes.contains(position)) registerFold(position);
        else registerUnfold(position);
    }

    private void registerFold(int position) {
        unfoldedIndexes.remove(position);
    }

    private void registerUnfold(int position) {
        unfoldedIndexes.add(position);
    }

    public HashSet<Integer> getUnfoldedIndexes() {
        return unfoldedIndexes;
    }

    public void setDefaultRequestBtnClickListener(View.OnClickListener defaultRequestBtnClickListener) {

        this.defaultRequestBtnClickListener = defaultRequestBtnClickListener;
    }

    private static class ViewHolder {

        TextView fold_letter_grade_tv;
        TextView fold_teacher_name_tv;
        TextView fold_block_letter_tv;
        TextView fold_subject_title_tv;
        TextView unfold_teacher_name_tv;
        TextView unfold_subject_title_tv;
        TextView fold_percentage_grade_tv;
        TextView unfold_percentage_grade_tv;
        FloatingActionButton floating_action_button;
        RecyclerView unfolded_grade_recycler_view;
        RelativeLayout fold_grade_background;
        RelativeLayout unfold_header_view;
    }
}