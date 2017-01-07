package carbonylgroup.com.schoolpower.classes;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ramotion.foldingcell.FoldingCell;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import carbonylgroup.com.schoolpower.R;

/**
 * Simple example of ListAdapter for using with Folding Cell
 * Adapter holds indexes of unfolded elements for correct work with default reusable views behavior
 */
public class FoldingCellListAdapter extends ArrayAdapter<MainListItem> {

    private HashSet<Integer> unfoldedIndexes = new HashSet<>();
    private View.OnClickListener defaultRequestBtnClickListener;

    public FoldingCellListAdapter(Context context, List<MainListItem> objects) {

        super(context, 0, objects);
    }

    @Override
    @NonNull
    public View getView(final int position, View convertView, ViewGroup parent) {

        MainListItem item = getItem(position);
        FoldingCell cell = (FoldingCell) convertView;
        ViewHolder viewHolder;

        if (cell == null) {

            viewHolder = new ViewHolder();
            LayoutInflater vi = LayoutInflater.from(getContext());
            cell = (FoldingCell) vi.inflate(R.layout.main_list_item, parent, false);

            viewHolder.fold_letter_grade_tv = (TextView) cell.findViewById(R.id.fold_letter_grade_tv);
            viewHolder.fold_percentage_grade_tv = (TextView) cell.findViewById(R.id.fold_percentage_grade_tv);
            viewHolder.fold_subject_title_tv = (TextView) cell.findViewById(R.id.fold_subject_title_tv);
            viewHolder.fold_teacher_name_tv = (TextView) cell.findViewById(R.id.fold_teacher_name_tv);
            viewHolder.fold_block_letter_tv = (TextView) cell.findViewById(R.id.fold_block_letter_tv);
            viewHolder.unfold_subject_title_tv = (TextView) cell.findViewById(R.id.unfold_subject_title_tv);
            viewHolder.unfold_teacher_name_tv = (TextView) cell.findViewById(R.id.unfold_teacher_name_tv);
            viewHolder.unfold_percentage_grade_tv = (TextView) cell.findViewById(R.id.unfold_percentage_grade_tv);
            viewHolder.unfolded_grade_recycler_view = (RecyclerView) cell.findViewById(R.id.unfolded_grade_recycler_view);
            viewHolder.floating_action_button = (FloatingActionButton) cell.findViewById(R.id.floating_action_button);
            viewHolder.fold_grade_background = (RelativeLayout) cell.findViewById(R.id.fold_grade_background);
            viewHolder.unfold_header_view = (RelativeLayout) cell.findViewById(R.id.unfold_header_view);

            cell.setTag(viewHolder);
        } else {

            if (unfoldedIndexes.contains(position)) cell.unfold(true);
            else cell.fold(true);
            viewHolder = (ViewHolder) cell.getTag();
        }

        int[] gradeColorIds = {R.color.A_score_green, R.color.B_score_yellow, R.color.Cp_score_yellow, R.color.C_score_orange,
                R.color.Cm_score_red, R.color.primary_dark, R.color.primary, R.color.primary};
        String[] letterGrades = {"A", "B", "C+", "C", "C-", "F", "I", "--"};
        int gradeColor = getContext().getResources().getColor(gradeColorIds[indexOfString(item.getLetterGrade(), letterGrades)]);
        ArrayList<PeriodGradeItem> items = PeriodGradeItem.getTestingList();
        PeriodGradeAdapter adapter = new PeriodGradeAdapter(getContext(), items);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

        viewHolder.fold_letter_grade_tv.setText(item.getLetterGrade());
        viewHolder.fold_percentage_grade_tv.setText(item.getPercentageGrade());
        viewHolder.fold_subject_title_tv.setText(item.getSubjectTitle());
        viewHolder.fold_teacher_name_tv.setText(item.getTeacherName());
        viewHolder.fold_block_letter_tv.setText(item.getBlockLetter());
        viewHolder.unfold_subject_title_tv.setText(item.getSubjectTitle());
        viewHolder.unfold_teacher_name_tv.setText(item.getTeacherName());
        viewHolder.unfold_percentage_grade_tv.setText(item.getPercentageGrade());
        viewHolder.unfolded_grade_recycler_view.setLayoutManager(layoutManager);
        viewHolder.unfolded_grade_recycler_view.setAdapter(adapter);
        viewHolder.floating_action_button.setOnClickListener(defaultRequestBtnClickListener);
        viewHolder.fold_grade_background.setBackgroundColor(gradeColor);
        viewHolder.unfold_header_view.setBackgroundColor(gradeColor);

        return cell;
    }

    public void refreshPeriodRecycler(FoldingCell _cell) {

        final ArrayList<PeriodGradeItem> items = PeriodGradeItem.getTestingList();
        final PeriodGradeAdapter adapter = new PeriodGradeAdapter(getContext(), items);
        ((RecyclerView) _cell.findViewById(R.id.unfolded_grade_recycler_view)).setAdapter(adapter);
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

    public void setDefaultRequestBtnClickListener(View.OnClickListener defaultRequestBtnClickListener) {

        this.defaultRequestBtnClickListener = defaultRequestBtnClickListener;
    }

    private int indexOfString(String searchString, String[] domain) {

        for (int i = 0; i < domain.length; i++)
            if (searchString.equals(domain[i])) return i;

        return -1;
    }

    private static class ViewHolder {

        TextView fold_letter_grade_tv;
        TextView fold_percentage_grade_tv;
        TextView fold_subject_title_tv;
        TextView fold_teacher_name_tv;
        TextView fold_block_letter_tv;
        TextView unfold_subject_title_tv;
        TextView unfold_teacher_name_tv;
        TextView unfold_percentage_grade_tv;
        RecyclerView unfolded_grade_recycler_view;
        FloatingActionButton floating_action_button;
        RelativeLayout fold_grade_background;
        RelativeLayout unfold_header_view;
    }
}