/*
 * Copyright (c) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.org.ngo.squeezer.itemlist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.util.Log;
import java.util.List;
import java.util.Map;

import uk.org.ngo.squeezer.R;
import uk.org.ngo.squeezer.framework.BaseListActivity;
import uk.org.ngo.squeezer.framework.ItemAdapter;
import uk.org.ngo.squeezer.framework.ItemView;
import uk.org.ngo.squeezer.framework.PlaylistItem;
import uk.org.ngo.squeezer.model.CustomBrowseItem;
import uk.org.ngo.squeezer.service.ISqueezeService;

/**
 * Display a list of Squeezebox music folders.
 * <p>
 * If the <code>extras</code> bundle contains a key that matches <code>MusicFolder.class.getName()</code>
 * the value is assumed to be an instance of that class, and that folder will be displayed.
 * <p>
 * Otherwise the root music folder is shown.
 * <p>
 * The activity's content views scrolls in from the right, and disappear to the left, to provide a
 * spatial component to navigation.
 *
 * @author nik
 */
public class CustomBrowseListActivity extends BaseListActivity<CustomBrowseItem> {

    /**
     * The folder to view. The root folder if null.
     */
    private CustomBrowseItem mFolder;

    @Override
    public ItemView<CustomBrowseItem> createItemView() {
        return new CustomBrowseView(this);
    }

    @Override
    protected ItemAdapter<CustomBrowseItem> createItemListAdapter(
            ItemView<CustomBrowseItem> itemView) {
        return new ItemAdapter<CustomBrowseItem>(itemView);
    }

    /**
     * Extract the folder to view (if provided).
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mFolder = extras.getParcelable(CustomBrowseItem.class.getName());
            TextView header = (TextView) findViewById(R.id.header);
            header.setText(mFolder.getName());
            header.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mFolder != null) {
            getMenuInflater().inflate(R.menu.playmenu, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Sets the enabled state of the R.menu.playmenu items.
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mFolder != null) {
            final int[] ids = {R.id.play_now, R.id.add_to_playlist};
            final boolean boundToService = getService() != null;

            for (int id : ids) {
                MenuItem item = menu.findItem(id);
                item.setEnabled(boundToService);
            }
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.play_now:
                play(mFolder);
                return true;
            case R.id.add_to_playlist:
                add(mFolder);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Fetch the contents of a folder. Fetches the contents of <code>mFolder</code> if non-null, the
     * root folder otherwise.
     *
     * @param start Where in the list of folders to start fetching.
     */
    @Override
    protected void orderPage(@NonNull ISqueezeService service, int start) {
        service.customBrowse("browse", start, mFolder, this);
    }

    /**
     * Show this activity, showing the contents of the root folder.
     *
     * @param activity
     */
    public static void show(Activity activity) {
        final Intent intent = new Intent(activity, CustomBrowseListActivity.class);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    /**
     * Show this activity, showing the contents of the given folder.
     *
     * @param activity
     * @param folder The folder whose contents will be shown.
     */
    public static void show(Activity activity, CustomBrowseItem folder) {
        final Intent intent = new Intent(activity, CustomBrowseListActivity.class);
        intent.putExtra(folder.getClass().getName(), folder);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    public void play(CustomBrowseItem item){
        if ("track".equals(item.getType())) {
            //custombrowse play for track has a bug where it plays the track but also
            //adds the whole album to the playlist. So just use the base class play.
            this.play((PlaylistItem)item);
        } else {
            this.getService().customBrowse("play", -1, item, null);
        }
    }

    public void add(CustomBrowseItem item){
        this.getService().customBrowse("add", -1, item, null);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    @Override
    public void onItemsReceived(int count, int start, Map<String, String> parameters, List<CustomBrowseItem> items, Class<CustomBrowseItem> dataType) {

        int itemCount = this.getItemAdapter().getCount();
        int pageSize = this.getItemAdapter().getPageSize();

        for (CustomBrowseItem item : items) {
            item.setHierarchy(parameters);
        }

        //Custombrowse doesn't report back the actual number of items available.
        //Here we check to see if we have reached the end, which is where the number
        //items returned is less than those requested.
        Log.d("CustomBrowseListActivity", "count = " + count + ",start=" + start + ", items.size() = " + items.size() + ",itemAdapter.count = " + itemCount);
        if (0 == items.size()) {
            count = start;
        } else {
            if (items.size() < pageSize) {
                count = start + items.size();
            } else {
                if ((start + items.size()) >= itemCount)
                    count = itemCount + 100;
                else if (itemCount > count)
                    count = itemCount;
            }
        }

        super.onItemsReceived(count, start, parameters, items, dataType);
    }

}
