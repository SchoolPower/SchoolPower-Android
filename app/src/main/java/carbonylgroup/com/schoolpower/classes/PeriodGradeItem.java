package carbonylgroup.com.schoolpower.classes;

import java.util.ArrayList;

public class PeriodGradeItem {

    private String termIndicator;
    private String termLetterGrade;
    private String termPercentageGrade;

    public PeriodGradeItem(String _termIndicator, String _termLetterGrade, String _termPercentageGrade){

        this.termIndicator = _termIndicator;
        this.termLetterGrade = _termLetterGrade;
        this.termPercentageGrade = _termPercentageGrade;
    }

    public String getTermIndicator(){
        return termIndicator;
    }

    public void setTermIndicator(String _termIndicator){
        this.termIndicator = _termIndicator;
    }

    public String getTermLetterGrade(){
        return termLetterGrade;
    }

    public void setTermLetterGrade(String _termLetterGrade){
        this.termLetterGrade = _termLetterGrade;
    }

    public String getTermPercentageGrade(){
        return termPercentageGrade;
    }

    public void setTermPercentageGrade(String _termPercentageGrade){
        this.termPercentageGrade = _termPercentageGrade;
    }

    /**
     * @return List of elements prepared for tests
     */
    public static ArrayList<PeriodGradeItem> getTestingList() {
        ArrayList<PeriodGradeItem> items = new ArrayList<>();
        items.add(new PeriodGradeItem("T1", "A", "95"));
        items.add(new PeriodGradeItem("T2", "A", "94"));
        items.add(new PeriodGradeItem("S1", "A", "95"));
        items.add(new PeriodGradeItem("T3", "A", "100"));
        items.add(new PeriodGradeItem("T4", "A", "100"));
        items.add(new PeriodGradeItem("S2", "A", "233"));
        items.add(new PeriodGradeItem("Y1", "A", "999"));
        return items;
    }
}
