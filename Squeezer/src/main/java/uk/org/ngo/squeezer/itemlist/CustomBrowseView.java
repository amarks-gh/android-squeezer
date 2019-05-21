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

import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import uk.org.ngo.squeezer.R;
import uk.org.ngo.squeezer.framework.ItemListActivity;
import uk.org.ngo.squeezer.framework.PlaylistItemView;
import uk.org.ngo.squeezer.model.CustomBrowseItem;

/**
 * View for one entry in a {@link CustomBrowseListActivity}.
 * <p>
 * Shows an entry with an icon indicating the type of the music folder item, and the name of the
 * item.
 *
 * @author nik
 */
public class CustomBrowseView extends PlaylistItemView<CustomBrowseItem> {

    @SuppressWarnings("unused")
    private final static String TAG = "CustomBrowseView";

    public CustomBrowseView(CustomBrowseListActivity activity) {
        super(activity);

        setViewParams(VIEW_PARAM_ICON | VIEW_PARAM_CONTEXT_BUTTON);
        setLoadingViewParams(VIEW_PARAM_ICON);
    }

    public CustomBrowseListActivity getActivity() {
        return (CustomBrowseListActivity)super.getActivity();
    }

    @Override
    public void bindView(View view, CustomBrowseItem item) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        viewHolder.text1.setText(item.getName());

        String type = item.getContext();
        if (0 == type.length())
            type = item.getType();
        if ("custom".equals(type))
            type = item.getId();
        int icon_resource = R.drawable.ic_music_folder;

        if (type.contains("artist")) {
            icon_resource = R.drawable.ic_artists;
        }
        else if (type.contains("album")){
            icon_resource = R.drawable.ic_albums;
        }
        else if (type.contains("genre")){
            icon_resource = R.drawable.ic_genres;
        }
        else if (type.contains("track")) {
            icon_resource = R.drawable.ic_songs;
        }
        else if (type.contains("playlist")) {
            icon_resource = R.drawable.ic_playlists;
        }
        else if (type.contains("years")) {
            icon_resource = R.drawable.ic_years;
        }

        viewHolder.icon.setImageResource(icon_resource);
    }

    @Override
    public boolean isSelectable(CustomBrowseItem item) {
        //if ("track".equals(item.getType())) {
        //    return super.isSelectable(item);
        //}
        return true;
    }

    @Override
    public void onItemSelected(int index, CustomBrowseItem item) {
        if ("track".equals(item.getType())) {
            super.onItemSelected(index, item);
        } else {
            CustomBrowseListActivity.show(getActivity(), item);
        }
    }

    // XXX: Make this a menu resource.
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        CustomBrowseItem item = (CustomBrowseItem) menuInfo.item;
        menu.add(Menu.NONE, R.id.play_now, Menu.NONE, R.string.PLAY_NOW);
        menu.add(Menu.NONE, R.id.add_to_playlist, Menu.NONE, R.string.ADD_TO_END);
    }

    @Override
    public boolean doItemContext(MenuItem menuItem, int index, CustomBrowseItem selectedItem) {
        switch (menuItem.getItemId()) {
            case R.id.play_now:
                this.getActivity().play(selectedItem);
                return true;
            case R.id.add_to_playlist:
                this.getActivity().add(selectedItem);
                return true;
        }
        return super.doItemContext(menuItem, index, selectedItem);
    }

    @Override
    public String getQuantityString(int quantity) {
        return getActivity().getResources().getQuantityString(R.plurals.musicfolder, quantity);
    }
}
