package vn.aiteam.hackathon.Api.Model;

import android.util.Log;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Query {
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Query(String value){
        Log.i("Query","value:" +value);
        String tmpValue = new String(value);
        final String regex = "(\\d+ *)+";
        final Pattern pattern = Pattern.compile(regex,Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
        final Matcher matcher = pattern.matcher(tmpValue);

        while (matcher.find()) {
            String fullMatch = matcher.group(0);
            System.out.println("Full match: " + matcher.group(0));
            String removeSpace = fullMatch.replaceAll("\\s+","");
            removeSpace += " ";
            System.out.println("removeSpace " + removeSpace);
            tmpValue = tmpValue.replace(fullMatch,removeSpace);
            System.out.println("tmpValue " + tmpValue);
        }
        Log.i("Query","newvalue:" +tmpValue);
        this.query = tmpValue.trim().toLowerCase();
    }
    @SerializedName("query")
    @Expose
    private String query;
}
