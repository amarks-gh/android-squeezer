package uk.org.ngo.squeezer.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import android.util.Log;
import java.util.HashMap;
import android.os.Parcel;

import com.google.common.base.Strings;

import java.util.Map;
import uk.org.ngo.squeezer.framework.PlaylistItem;

public class CustomBrowseItem extends PlaylistItem {


    @Override
    public String getPlaylistTag() {

        if ("track".equals(this.getType()))
            return "track_id";

        return "Unknown_type_in_getTag()";
    }

    @Override
    public String getFilterTag() {
        return "folder_id";
    }

    private String name;

    @Override
    public String getName() {
        return name;
    }

    public CustomBrowseItem setName(String name) {
        this.name = name;
        return this;
    }

    public String getContext(){ return context; }

    /**
     * The folder item's type, "track", "folder", "playlist", "unknown".
     */
    // XXX: Should be an enum.
    private String type;

    public String getType() {
        return type;
    }

    public CustomBrowseItem setType(String type) {
        this.type = type;
        return this;
    }

    private String context = "";
    private String level = "";
    private String hierarchy = null;
    private ArrayList<String> hierarchyParams = null;

    public CustomBrowseItem(Map<String, String> record) {
        setId(record.get("itemid"));
        name = record.get("itemname");
        type = record.get("itemtype");
        context = Strings.nullToEmpty(record.get("itemcontext"));
        level = record.get("level");
    }

    public void setHierarchy(Map<String,String> parameters) {
        if  (parameters.containsKey("hierarchy")) {
            hierarchy = parameters.get("hierarchy");
            hierarchyParams = new ArrayList<String>();
            for (String key : hierarchy.split(",")) {
                hierarchyParams.add(key + ":" + parameters.get(key));
            }
        }

    }
    public void getRequestParameters(List<String> parameters){
        if (null == hierarchy || 0 == hierarchy.length()) {
            parameters.add("hierarchy:" + level);
        } else {
            parameters.add("hierarchy:" + hierarchy + "," + level);
            parameters.addAll(hierarchyParams);
        }
        parameters.add(level + ":" + getId());
    }

    public static final Creator<CustomBrowseItem> CREATOR = new Creator<CustomBrowseItem>() {
        @Override
        public CustomBrowseItem[] newArray(int size) {
            return new CustomBrowseItem[size];
        }

        @Override
        public CustomBrowseItem createFromParcel(Parcel source) {
            return new CustomBrowseItem(source);
        }
    };

    private CustomBrowseItem(Parcel source) {
        Log.d("CustomBrowseItem","constructor called");
        setId(source.readString());
        name = source.readString();
        type = source.readString();
        context = source.readString();
        level = source.readString();
        hierarchy = source.readString();
        if (0 == hierarchy.length()) {
          hierarchy = null;
          hierarchyParams = null;
        } else {
            if (null == hierarchyParams)
                hierarchyParams = new ArrayList<String>();
            else
                hierarchyParams.clear();
            int i = source.readInt();
            while (i > 0) {
                hierarchyParams.add(source.readString());
                i--;
            }
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Log.d("CustomBrowseItem", "writeToParcel() called");
        dest.writeString(getId());
        dest.writeString(name);
        dest.writeString(type);
        dest.writeString(context);
        dest.writeString(level);
        if (null == hierarchy || 0 == hierarchy.length())
            dest.writeString("");
        else
        {
            dest.writeString(hierarchy);
            dest.writeInt(hierarchyParams.size());
            for (String s : hierarchyParams)
                dest.writeString(s);
        }
    }
}
