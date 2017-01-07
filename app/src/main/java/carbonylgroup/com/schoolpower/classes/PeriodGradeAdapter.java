package carbonylgroup.com.schoolpower.classes;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import carbonylgroup.com.schoolpower.R;
import carbonylgroup.com.schoolpower.R2;

public class PeriodGradeAdapter extends RecyclerView.Adapter<PeriodGradeAdapter.PeriodGradeViewHolder> {

    private LayoutInflater inflater;
    private List<PeriodGradeItem> periodGradeItemList;

    public PeriodGradeAdapter(Context context, ArrayList<PeriodGradeItem> objects) {
        inflater = LayoutInflater.from(context);
        this.periodGradeItemList = objects;
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

        PeriodGradeViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("NormalTextViewHolder", "onClick--> position = " + getPosition());
                }
            });
        }
    }
}