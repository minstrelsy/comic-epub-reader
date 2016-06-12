package net.nkbits.epubcomic.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import net.androidcomics.acv.R;
import net.nkbits.epubcomic.db.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nakayama on 6/12/16.
 */
public class ItemAdapter extends BaseAdapter {
    private Context context;
    private List<Item> items;

    // Constructor
    public ItemAdapter(Context c) {
        items = new ArrayList<>();
        context = c;
    }

    public int getCount() {
        return items.size();
    }

    public Object getItem(int position) {
        return items.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    public void addItem(Item item) {
        items.add(item);
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        if (convertView == null || !(convertView.getTag() instanceof ViewHolder)) {
            convertView = layoutInflater.inflate(R.layout.book_shelf_view, parent, false);
            holder = new ViewHolder();

            holder.cover = (ImageView) convertView.findViewById(R.id.book_cover);

            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        holder.cover.setImageResource(R.drawable.book_cover);

        return convertView;
    }

    static class ViewHolder {
        ImageView cover;
    }
}
