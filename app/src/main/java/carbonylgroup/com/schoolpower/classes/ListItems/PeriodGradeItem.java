/**
 * Copyright (C) 2017 Gustav Wang
 */

package carbonylgroup.com.schoolpower.classes.ListItems;

import java.io.Serializable;
import java.util.ArrayList;


public class PeriodGradeItem implements Serializable {

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
}
