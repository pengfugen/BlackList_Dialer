package com.android.dialer.calllog;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.dialer.R;

/**
 * Created by pengfugen on 16-3-4.
 */
public class WindRecentCallLogListItem {
    public ImageView mPhoto;
    public TextView mNumberOrName;
    public TextView mDisplayName;
    public TextView mDate;
    public TextView mTime;
    public ImageView mImageCallType;
    public CheckBox mCheckBox;

    long mRowId;
    long [] mCallIds;
    String mNumber;
    int mNumberPresentation;
    int mCallType;
    String mVoiceMailUri;
    String [] mConfCallNumbers;

    WindRecentCallLogListItem(View view)
    {
        mPhoto = (ImageView)view.findViewById(R.id.photo);
        mNumberOrName = (TextView)view.findViewById(R.id.name_or_number);
        mDisplayName = (TextView)view.findViewById(R.id.display_name);
        mDate = (TextView)view.findViewById(R.id.date);
        mTime = (TextView)view.findViewById(R.id.time);
        mImageCallType = (ImageView)view.findViewById(R.id.image_calltype);
        mCheckBox = (CheckBox)view.findViewById(R.id.checked);
    }

    public static WindRecentCallLogListItem fromView(View view) {
        return new WindRecentCallLogListItem(view);
    }
}
