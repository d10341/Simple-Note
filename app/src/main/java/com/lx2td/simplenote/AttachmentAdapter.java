package com.lx2td.simplenote;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.lx2td.simplenote.models.Attachment;

import java.util.Collections;
import java.util.List;

import static com.lx2td.simplenote.utils.Constants.MIME_TYPE_AUDIO;
import static com.lx2td.simplenote.utils.Constants.MIME_TYPE_FILES;

public class AttachmentAdapter extends BaseAdapter {
    private Activity mActivity;
    private List<Attachment> attachmentsList;
    private LayoutInflater inflater;


    public AttachmentAdapter(Activity mActivity, List<Attachment> attachmentsList,
                             GridView mGridView) {
        this.mActivity = mActivity;
        if (attachmentsList == null) {
            attachmentsList = Collections.emptyList();
        }
        this.attachmentsList = attachmentsList;
        inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    public int getCount() {
        return attachmentsList.size();
    }


    public Attachment getItem(int position) {
        return attachmentsList.get(position);
    }


    public long getItemId(int position) {
        return 0;
    }


    public View getView(int position, View convertView, ViewGroup parent) {

        Attachment mAttachment = attachmentsList.get(position);

        AttachmentHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.gridview_item, parent, false);

            holder = new AttachmentHolder();
            holder.image = convertView.findViewById(R.id.gridview_item_picture);
            holder.text = convertView.findViewById(R.id.gridview_item_text);
            convertView.setTag(holder);
        } else {
            holder = (AttachmentHolder) convertView.getTag();
        }

        // Draw name in case the type is an audio recording
        if (mAttachment.getMime_type() != null && mAttachment.getMime_type().equals(MIME_TYPE_AUDIO)) {
            String text = "null";
            if (text == null) {
                text = mActivity.getString(R.string.attachment);
            }
            holder.text.setText(text);
            holder.text.setVisibility(View.VISIBLE);
        } else {
            holder.text.setVisibility(View.GONE);
        }

        // Draw name in case the type is an audio recording (or file in the future)
        if (mAttachment.getMime_type() != null && mAttachment.getMime_type().equals(MIME_TYPE_FILES)) {
            holder.text.setText(mAttachment.getName());
            holder.text.setVisibility(View.VISIBLE);
        }


        return convertView;
    }


    public List<Attachment> getAttachmentsList() {
        return attachmentsList;
    }


    public class AttachmentHolder {
        TextView text;
        ImageView image;
    }
}
