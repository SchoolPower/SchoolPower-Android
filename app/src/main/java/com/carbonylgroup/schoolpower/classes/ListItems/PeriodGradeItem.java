/**
 * Copyright (C) 2017 Gustav Wang
 */

package com.carbonylgroup.schoolpower.classes.ListItems;

import java.io.Serializable;
import java.util.ArrayList;


public class PeriodGradeItem implements Serializable {

    private final String termIndicator;
    private final String termLetterGrade;
    private final String termPercentageGrade;
    private final ArrayList<AssignmentItem> assignmentItemArrayList;

    public PeriodGradeItem(String _termIndicator, String _termLetterGrade, String _termPercentageGrade,
                           ArrayList<AssignmentItem> _assignmentItemArrayList){

        this.termIndicator = _termIndicator;
        this.termLetterGrade = _termLetterGrade;
        this.termPercentageGrade = _termPercentageGrade;
        this.assignmentItemArrayList = _assignmentItemArrayList;
    }

    public String getTermIndicator(){
        return termIndicator;
    }

    public String getTermLetterGrade(){
        return termLetterGrade;
    }

    public String getTermPercentageGrade(){
        return termPercentageGrade;
    }

    public ArrayList<AssignmentItem> getAssignmentItemArrayList() {
        return assignmentItemArrayList;
    }
}
