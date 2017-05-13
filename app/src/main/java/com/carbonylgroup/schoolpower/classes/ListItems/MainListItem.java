/**
 * Copyright (C) 2017 Gustav Wang
 */

package com.carbonylgroup.schoolpower.classes.ListItems;

import java.io.Serializable;
import java.util.ArrayList;


public class MainListItem implements Serializable {

    private final String subjectTitle;
    private final String teacherName;
    private final String blockLetter;
    private final String roomNumber;
    private ArrayList<PeriodGradeItem> periodGradeItemArrayList;

    public MainListItem(String _subjectTitle, String _teacherName, String _blockLetter,
                        String _roomNumber, ArrayList<PeriodGradeItem> _periodGradeItemArrayList) {

        this.subjectTitle = _subjectTitle;
        this.teacherName = _teacherName;
        this.blockLetter = _blockLetter;
        this.roomNumber = _roomNumber;
        this.periodGradeItemArrayList = _periodGradeItemArrayList;

    }

    public String getLetterGrade() {
        // Use the grade of the last available term.
        return periodGradeItemArrayList.get(periodGradeItemArrayList.size()-1).getTermLetterGrade();
    }

    public String getPercentageGrade() {
        // Use the grade of the last available term.
        return periodGradeItemArrayList.get(periodGradeItemArrayList.size()-1).getTermPercentageGrade();
    }

    public String getSubjectTitle() {
        return subjectTitle;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public String getBlockLetter() {
        return blockLetter;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public ArrayList<PeriodGradeItem> getPeriodGradeItemArrayList() {
        return periodGradeItemArrayList;
    }

    public ArrayList<AssignmentItem> getAssignmentItemArrayList(String term) {
        for (PeriodGradeItem periodGradeItem : periodGradeItemArrayList) {
            if(term.equals(periodGradeItem.getTermIndicator())){
                return periodGradeItem.getAssignmentItemArrayList();
            }
        }
        return null;
    }

    public void addPeriodGradeItem(PeriodGradeItem pgi) {
        periodGradeItemArrayList.add(pgi);
    }
}
