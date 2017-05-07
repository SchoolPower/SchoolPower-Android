/**
 * Copyright (C) 2017 Gustav Wang
 */

package carbonylgroup.com.schoolpower.classes.Utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import carbonylgroup.com.schoolpower.R;
import carbonylgroup.com.schoolpower.classes.ListItems.AssignmentItem;
import carbonylgroup.com.schoolpower.classes.ListItems.MainListItem;
import carbonylgroup.com.schoolpower.classes.ListItems.PeriodGradeItem;


public class Utils {

    private Context context;

    private int[] gradeColorIds = {R.color.A_score_green, R.color.B_score_green, R.color.Cp_score_yellow, R.color.C_score_orange,
            R.color.Cm_score_red, R.color.primary_dark, R.color.primary, R.color.primary};
    private int[] gradeColorIdsPlain = {R.color.A_score_green, R.color.B_score_green, R.color.Cp_score_yellow, R.color.C_score_orange,
            R.color.Cm_score_red, R.color.primary_dark, R.color.primary};
    private int[] gradeDarkColorIdsPlain = {R.color.A_score_green_dark, R.color.B_score_green_dark, R.color.Cp_score_yellow_dark, R.color.C_score_orange_dark,
            R.color.Cm_score_red_dark, R.color.primary_darker, R.color.primary_dark};

    public Utils(Context _context) {

        this.context = _context;
    }

    private int indexOfString(String searchString, String[] domain) {

        for (int i = 0; i < domain.length; i++)
            if (searchString.equals(domain[i])) return i;

        return -1;
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    /* Color Handler */
    public int getColorByLetterGrade(Context context, String letterGrade) {

        String[] letterGrades = {"A", "B", "C+", "C", "C-", "F", "I", "--"};
        return context.getResources().getColor(gradeColorIds[indexOfString(letterGrade, letterGrades)]);
    }

    public int getColorByPeriodItem(Context context, PeriodGradeItem item) {

        String[] letterGrades = {"A", "B", "C+", "C", "C-", "F", "I", "--"};
        return context.getResources().getColor(gradeColorIds[indexOfString(item.getTermLetterGrade(), letterGrades)]);
    }

    public int getDarkColorByPrimary(int _originalPrimary) {

        int flag = 0;
        for (int i : gradeColorIdsPlain) {
            if (_originalPrimary == context.getResources().getColor(i)) break;
            flag++;
        }

        return context.getResources().getColor(gradeDarkColorIdsPlain[flag]);
    }

    public ArrayList<MainListItem> inputDataArrayList() throws Exception {

        FileInputStream inputStream = context.openFileInput(context.getString(R.string.dataFileName));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;

        while ((len = inputStream.read(buffer)) != -1) outputStream.write(buffer, 0, len);
        byte[] data = outputStream.toByteArray();

        if (data == null) return null;
        inputStream.close();
        return parseJsonResult(new String(data));
    }

    public void outputDataJson(String _jsonStr) throws Exception {

        FileOutputStream outputStream = context.openFileOutput(context.getString(R.string.dataFileName), Context.MODE_PRIVATE);
        outputStream.write(_jsonStr.getBytes());
        outputStream.close();
    }

    /**
     * @param url    url
     * @param params name1=value1&name2=value2
     * @return result
     */
    static String sendPost(String url, String params) {

        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {

            URL realUrl = new URL(url);
            // Open Connection
            URLConnection conn = realUrl.openConnection();
            // Setting
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // Get URLConnection Output
            out = new PrintWriter(conn.getOutputStream());
            // Send Param
            out.print(params);
            out.flush();
            // Define BufferedReader Input
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) result += "\n" + line;

        } catch (Exception e) {
            System.out.println("发送POST请求出现异常！" + e);
            e.printStackTrace();
        }
        // Close I/O
        finally {
            try {
                if (out != null) out.close();
                if (in != null) in.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }

    public ArrayList<MainListItem>  parseJsonResult(String jsonStr) {

        if (!jsonStr.equals("")) {

            try {

                JSONArray jsonData = new JSONArray(jsonStr);

                HashMap<String, MainListItem> dataMap = new HashMap<>();

                for (int i = 0; i < jsonData.length(); i++) {

                    JSONObject termObj = jsonData.getJSONObject(i);

                    PeriodGradeItem periodGradeItem = new PeriodGradeItem(termObj.getString("term"),
                            termObj.getString("grade").equals("")?"--":termObj.getString("grade"), termObj.getString("mark"));

                    if (dataMap.get(termObj.getString("name")) == null) {

                        ArrayList<PeriodGradeItem> periodGradeList = new ArrayList<>();
                        periodGradeList.add(periodGradeItem);

                        ArrayList<AssignmentItem> assignmentList = new ArrayList<>();
                        JSONArray asmArray = termObj.getJSONArray("assignments");
                        for (int j = 0; j < asmArray.length(); j++) {
                            JSONObject asmObj = asmArray.getJSONObject(j);
                            String[] dates = asmObj.getString("date").split("/");
                            String date = dates[2] + "/" + dates[0] + "/" + dates[1];
                            AssignmentItem assignmentItem = new AssignmentItem(asmObj.getString("assignment"),
                                    date, asmObj.getString("grade").equals("")?"--":asmObj.getString("percent"),
                                    asmObj.getString("score").endsWith("d")?context.getString(R.string.unpublished):asmObj.getString("score"),
                                    asmObj.getString("grade").equals("")?"--":asmObj.getString("grade"), asmObj.getString("category"), termObj.getString("term"));
                            assignmentList.add(assignmentItem);
                        }
                        MainListItem mainListItem = new MainListItem(termObj.getString("grade").equals("")?"--":termObj.getString("grade"),
                                termObj.getString("mark"), termObj.getString("name"), termObj.getString("teacher"),
                                termObj.getString("block"), termObj.getString("room"), termObj.getString("term"), periodGradeList, assignmentList);
                        dataMap.put(termObj.getString("name"), mainListItem);

                    } else {

                        MainListItem mainListItem = dataMap.get(termObj.getString("name"));
                        ArrayList<PeriodGradeItem> periodGradeList = mainListItem.getPeriodGradeItemArrayList();

                        periodGradeList.add(periodGradeItem);
                        ArrayList<AssignmentItem> assignmentList = mainListItem.getAssignmentItemArrayList();
                        JSONArray asmArray = termObj.getJSONArray("assignments");
                        for (int j = 0; j < asmArray.length(); j++) {
                            JSONObject asmObj = asmArray.getJSONObject(j);
                            String[] dates = asmObj.getString("date").split("/");
                            String date = dates[2] + "/" + dates[0] + "/" + dates[1];
                            AssignmentItem assignmentItem = new AssignmentItem(asmObj.getString("assignment"),
                                    date, asmObj.getString("grade").equals("")?"--":asmObj.getString("percent"),
                                    asmObj.getString("score").endsWith("d")?context.getString(R.string.unpublished):asmObj.getString("score"),
                                    asmObj.getString("grade").equals("")?"--":asmObj.getString("grade"), asmObj.getString("category"), termObj.getString("term"));
                            assignmentList.add(assignmentItem);
                        }

                        mainListItem.setPeriodGradeItemArrayList(periodGradeList);
                        mainListItem.setAssignmentItemArrayList(assignmentList);
                        dataMap.put(termObj.getString("name"), mainListItem);
                    }
                }
                ArrayList<MainListItem> dataList = new ArrayList<>();
                dataList.addAll(dataMap.values());
                Collections.sort(dataList, new Comparator<MainListItem>() {
                    public int compare(MainListItem o1, MainListItem o2) {
                        return o1.getBlockLetter().compareTo(o2.getBlockLetter());
                    }
                });
                return dataList;

            } catch (final JSONException e) {
                e.printStackTrace();
            }

        } else Log.e("ParseJsonData", "Empty Json");

        return null;
    }
}