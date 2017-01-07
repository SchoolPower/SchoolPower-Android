package carbonylgroup.com.schoolpower.classes;

import java.util.ArrayList;

/**
 * Simple POJO model for example
 */
public class MainListItem {

    private String letterGrade;
    private String percentageGrade;
    private String subjectTitle;
    private String teacherName;
    private String blockLetter;

    public MainListItem(String _letterGrade, String _percentageGrade, String _subjectTitle, String _teacherName, String _blockLetter) {

        this.letterGrade = _letterGrade;
        this.percentageGrade = _percentageGrade;
        this.subjectTitle = _subjectTitle;
        this.teacherName = _teacherName;
        this.blockLetter = _blockLetter;
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

    public String getBlockLetter () {
        return blockLetter;
    }

    public void setBlockLetter(String _blockLetter) {
        this.blockLetter = _blockLetter;
    }

    /**
     * @return List of elements prepared for tests
     */
    public static ArrayList<MainListItem> getTestingList() {

        ArrayList<MainListItem> items = new ArrayList<>();
        items.add(new MainListItem("--", "--", "Homeroom 10", "Jack Siu", "Block HR"));
        items.add(new MainListItem("A", "100", "Science 10", "Grainne Smith", "Block A"));
        items.add(new MainListItem("B", "79", "Planning 10", "Grainne Smith", "Block B"));
        items.add(new MainListItem("C+", "70", "Communication 11", "Rachel Chang", "Block C"));
        items.add(new MainListItem("C", "63", "Foundation of Maths and Pre-Calculus 10", "Jack Siu", "Block D"));
        items.add(new MainListItem("C-", "52", "Chinese Social Study 10", "Jun Liu", "Block E"));
        items.add(new MainListItem("F", "23", "Mandarin Chinese 10", "Haixia Shi", "Block E"));
        items.add(new MainListItem("I", "35", "Moral Education", "Han Wang", "Block ME"));
        return items;
    }
}
