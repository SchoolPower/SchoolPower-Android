/**
 * Copyright (C) 2017 Gustav Wang
 */

package carbonylgroup.com.schoolpower.classes.ListItems;

import java.io.Serializable;

public class AssignmentItem implements Serializable {

    private String assignmentTitle;
    private String assignmentDate;
    private String assignmentPercentage;
    private String assignmentDividedScore;
    private String assignmentGrade;
    private String assignmentCategory;
    private String assignmentTerm;

    public AssignmentItem(String _assignmentTitle, String _assignmentDate, String _assignmentPercentage, String _assignmentDividedScore,
                          String _assignmentGrade, String _assignmentCategory, String _assignmentTerm) {

        this.assignmentTitle = _assignmentTitle;
        this.assignmentDate = _assignmentDate;
        this.assignmentPercentage = _assignmentPercentage;
        this.assignmentDividedScore = _assignmentDividedScore;
        this.assignmentGrade = _assignmentGrade;
        this.assignmentCategory = _assignmentCategory;
        this.assignmentTerm = _assignmentTerm;
    }

    public String getAssignmentTitle() {
        return assignmentTitle;
    }

//    public void setTermIndicator(String _termIndicator){
//        this.termIndicator = _termIndicator;
//    }

    public String getAssignmentDate() {
        return assignmentDate;
    }

//    public void setTermLetterGrade(String _termLetterGrade){
//        this.termLetterGrade = _termLetterGrade;
//    }

    public String getAssignmentPercentage() {
        return assignmentPercentage;
    }

    public String getAssignmentDividedScore() {
        return assignmentDividedScore;
    }

    public String getAssignmentGrade() {
        return assignmentGrade;
    }

    public String getAssignmentCategory() {
        return assignmentCategory;
    }

    public String getAssignmentTerm() {
        return assignmentTerm;
    }

//    public void setTermPercentageGrade(String _termPercentageGrade){
//        this.termPercentageGrade = _termPercentageGrade;
//    }
}
