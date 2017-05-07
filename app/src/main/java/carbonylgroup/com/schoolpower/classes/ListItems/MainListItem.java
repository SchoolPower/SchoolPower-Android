/**
 * Copyright (C) 2017 Gustav Wang
 */

package carbonylgroup.com.schoolpower.classes.ListItems;

import java.io.Serializable;
import java.util.ArrayList;


public class MainListItem implements Serializable {

    private String letterGrade;
    private String percentageGrade;
    private String subjectTitle;
    private String teacherName;
    private String blockLetter;
    private String roomNumber;
    private String termNumber;
    private ArrayList<PeriodGradeItem> periodGradeItemArrayList;
    private ArrayList<AssignmentItem> assignmentItemArrayList;


    public MainListItem(String _letterGrade, String _percentageGrade, String _subjectTitle, String _teacherName,
                        String _blockLetter, String _roomNumber, String _termNumber, ArrayList _periodGradeItemArrayList, ArrayList _assignmentItemArrayList) {

        this.letterGrade = _letterGrade;
        this.percentageGrade = _percentageGrade;
        this.subjectTitle = _subjectTitle;
        this.teacherName = _teacherName;
        this.blockLetter = _blockLetter;
        this.roomNumber = _roomNumber;
        this.termNumber = _termNumber;
        this.periodGradeItemArrayList = _periodGradeItemArrayList;
        this.assignmentItemArrayList = _assignmentItemArrayList;
    }

    public String getLetterGrade() {
        return letterGrade;
    }

    public void setLetterGrade(String _letterGrade) {
        this.letterGrade = _letterGrade;
    }

    public String getPercentageGrade() {
        return percentageGrade;
    }

    public void setPercentageGrade(String _percentageGrade) {
        this.percentageGrade = _percentageGrade;
    }

    public String getSubjectTitle() {
        return subjectTitle;
    }

    public void setSubjectTitle(String _subjectTitle) {
        this.subjectTitle = _subjectTitle;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String _teacherName) {
        this.teacherName = _teacherName;
    }

    public String getBlockLetter() {
        return blockLetter;
    }

    public void setBlockLetter(String _blockLetter) {
        this.blockLetter = _blockLetter;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public String getTermNumber() {
        return termNumber;
    }

    public ArrayList<PeriodGradeItem> getPeriodGradeItemArrayList() {
        return periodGradeItemArrayList;
    }

    public void setPeriodGradeItemArrayList(ArrayList<PeriodGradeItem> _periodGradeItemArrayList) {
        this.periodGradeItemArrayList = _periodGradeItemArrayList;
    }

    public ArrayList<AssignmentItem> getAssignmentItemArrayList() {
        return assignmentItemArrayList;
    }

    public void setAssignmentItemArrayList(ArrayList<AssignmentItem> _assignmentItemArrayList) {
        this.assignmentItemArrayList = _assignmentItemArrayList;
    }
}
