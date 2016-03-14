package com.android.dialer.calllog;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.CallLog;
import android.telecom.PhoneAccountHandle;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.contacts.common.util.UriUtils;
import com.android.dialer.R;
import com.mediatek.dialer.ext.ExtensionManager;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by pengfugen on 16-3-3.
 */
public class WindRecentCallLogAdapter extends CallLogAdapter{

    private final static String LOG_TAG = "WindRecentCallLogAdapter";

    private Cursor mCursor;

    PhoneNumberDisplayHelper mPhoneNumberHelper;

    /** M: Fix CR ALPS01569024. Save the selected call log id list. @{ */
    private final ArrayList<Integer> mSelectedCallLogIdList = new ArrayList<Integer>();
    private final HashMap<String,String> mSelectedCallLogInfoList = new HashMap<String,String>();
    private final ArrayList<Integer> mSelectedGroupCallIdList = new ArrayList<Integer>();

    /*public static class CallLogInfo {
        int callId;
        String number;
        String name;

        CallLogInfo(int callId, String number, String name) {
            this.callId = callId;
            this.number = number;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof CallLogInfo)) {
                return false;
            }
            CallLogInfo info = (CallLogInfo) o;
            return (info.number == this.number);
        }
    }*/

    private int mSelectedItemCount = 0;
    // A listener to listen the selected item change
    public interface SelectedItemChangeListener {
        public void onSelectedItemCountChange(int count);
    }
    private SelectedItemChangeListener mSelectedItemChangeListener;
    /** @} */

    /**
     * Construct function
     *
     * @param context context
     * @param callFetcher Callfetcher
     * @param contactInfoHelper contactinfohelper
     * @param voicemailNumber voicemailNumber
     */
    public WindRecentCallLogAdapter(Context context, CallFetcher callFetcher,
                                        ContactInfoHelper contactInfoHelper, String voicemailNumber) {
        super(context, callFetcher, contactInfoHelper, null, null, false);
        mPhoneNumberHelper = new PhoneNumberDisplayHelper(mContext, mContext.getResources());
    }

    /**
     * @param cursor cursor
     */
    public void changeCursor(Cursor cursor) {
        log("changeCursor(), cursor = " + cursor);
        if (null != cursor) {
            log("cursor count = " + cursor.getCount());
        }
        if (mCursor != cursor) {
            mCursor = cursor;
        }
        super.changeCursor(cursor);

        /** M: Fix CR ALPS01569024. Reconcile the selected call log ids
         *  with the ids in the new cursor. @{ */
        reconcileSeletetedItems(cursor);
        if (mSelectedItemChangeListener != null) {
            mSelectedItemChangeListener.onSelectedItemCountChange(
                    mSelectedItemCount);
        }
        /** @} */
    }

    @Override
    protected View newChildView(Context context, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.wind_recent_calllog_item, parent, false);

        // Get the views to bind to and cache them.
        WindRecentCallLogListItem views = WindRecentCallLogListItem.fromView(view);
        view.setTag(views);

        return view;
    }

    /**
     * Binds the views in the entry to the data in the call log.
     *
     * @param view the view corresponding to this entry
     * @param c the cursor pointing to the entry in the call log
     * @param count the number of entries in the current item, greater than 1 if it is a group
     */
    protected void bindView(View callLogItemView, Cursor c, int count) {
        log("bindView(), cursor = " + c + " count = " + count);
        /** M: Fix CR ALPS01569024. Use the call log id to identify the select item. @{ */
        boolean checkState = false;
        if (mSelectedCallLogIdList.size() > 0) {
            int position = c.getPosition();
            log("bindView(), position = " + position);
            for (int i = 0; i < count; i++) {
                if (!c.moveToPosition(position + i)) {
                    continue;
                }
                if (mSelectedCallLogIdList.contains(c.getInt(CallLogQuery.ID))) {
                    checkState = true;
                    break;
                }
            }
            c.moveToPosition(position);
        }
        /** @} */

        final WindRecentCallLogListItem views = (WindRecentCallLogListItem) callLogItemView.getTag();

        final String number = c.getString(CallLogQuery.NUMBER);
        final int numberPresentation = c.getInt(CallLogQuery.NUMBER_PRESENTATION);
        long date = c.getLong(CallLogQuery.DATE);
        final int callType = c.getInt(CallLogQuery.CALL_TYPE);
        final String countryIso = c.getString(CallLogQuery.COUNTRY_ISO);
        final long rowId = c.getLong(CallLogQuery.ID);

        // Store some values used when the actions ViewStub is inflated on expansion of the actions
        // section.
        views.mRowId = rowId;
        views.mNumber = number;
        /// M: [VoLTE ConfCall] need reset confCallNumbers for new call log item
        views.mConfCallNumbers = null;
        views.mNumberPresentation = numberPresentation;
        views.mCallType = callType;
        views.mVoiceMailUri = c.getString(CallLogQuery.VOICEMAIL_URI);
        // Stash away the Ids of the calls so that we can support deleting a row in the call log.
        views.mCallIds = getCallIds(c, count);
        final PhoneAccountHandle accountHandle = PhoneAccountUtils.getAccount(
                c.getString(CallLogQuery.ACCOUNT_COMPONENT_NAME),
                c.getString(CallLogQuery.ACCOUNT_ID));
        log("bindView(), callIds:"+views.mCallIds);

        ContactInfo info = ContactInfo.getContactInfofromCursor(c);
        /// M: for plug-in @{
        ExtensionManager.getInstance().getCallLogExtension().bindViewPreForCallLogAdapter(mContext, info);
        /// @}

        String name = info.name;
        CharSequence formattedNumber = info.formattedNumber == null
                ? null : PhoneNumberUtils.ttsSpanAsPhoneNumber(info.formattedNumber);
        int[] callTypes = getCallTypes(c, count);

        log("bindView(), rowId:"+rowId+" number:" + number+ " numberPresentation:"+numberPresentation +" name:"+name);
        log("bindView(), callType:"+callType);

        views.mPhoto.setBackgroundResource(R.drawable.asus_contacts_old_ic_history_head);

        final CharSequence displayNumber =
                mPhoneNumberHelper.getDisplayNumber(accountHandle, number,
                        numberPresentation, formattedNumber);
        if (TextUtils.isEmpty(name)) {
            // We have a real phone number as "nameView" so make it always LTR
            views.mNumberOrName.setTextDirection(View.TEXT_DIRECTION_LTR);
            views.mNumberOrName.setText(displayNumber);
            views.mDisplayName.setText(mContext.getString(R.string.name_unknow));
        } else {
            views.mNumberOrName.setText(name);
            views.mDisplayName.setText(displayNumber);
            views.mDisplayName.setTextDirection(View.TEXT_DIRECTION_LTR);
        }

        /*CharSequence dateValue = DateUtils.formatDateRange(mContext, date, date,
                DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE |
                        DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_12HOUR);

        android.util.Log.d(LOG_TAG,"The date is:"+dateValue);*/
        SimpleDateFormat format = new SimpleDateFormat("EE-yyyy-MM-dd HH:mm");
        String strValue = format.format(new Date(date));
        Log.d(LOG_TAG,"The date is:"+strValue);
        if (strValue != null && strValue.contains(" ")) {
            String[] strArr = strValue.split(" ");
            String temp = strArr[0];
            String[] arrWeekAndDate = temp.split("-");
            String strWeek = "";
            String strDate = "";
            if (arrWeekAndDate.length == 4) {
                strWeek = arrWeekAndDate[0];
                strDate = arrWeekAndDate[2] + "/" + arrWeekAndDate[3];
            }
            views.mDate.setText(strDate + "," + strWeek);
            String time = strArr[1];
            String[] arrHourAndMin = time.split(":");
            String label = "AM";

            if (arrHourAndMin.length == 2) {
                int hour = Integer.parseInt(arrHourAndMin[0]);
                if (hour >= 12) {
                    label = "PM";
                }
            }
            views.mTime.setText(time + " " + label);
        }

        if (callType == CallLog.Calls.INCOMING_TYPE) {
            views.mImageCallType.setBackgroundResource(R.drawable.asus_contacts_ic_history_incomung);
            views.mNumberOrName.setTextColor(Color.BLACK);
        } else if (callType == CallLog.Calls.OUTGOING_TYPE) {
            views.mImageCallType.setBackgroundResource(R.drawable.asus_contacts_ic_history_outgoing);
            views.mNumberOrName.setTextColor(Color.BLACK);
        } else if(callType == CallLog.Calls.MISSED_TYPE){
            views.mImageCallType.setBackgroundResource(R.drawable.asus_contacts_ic_history_miss);
            views.mNumberOrName.setTextColor(Color.RED);
        }

        if (views.mCheckBox != null) {
            views.mCheckBox.setFocusable(false);
            views.mCheckBox.setClickable(false);
            views.mCheckBox.setVisibility(View.VISIBLE);
            views.mCheckBox.setChecked(checkState);
            Log.d(LOG_TAG,"checkbox state: clickable:"+views.mCheckBox.isClickable() +" focusable:"+views.mCheckBox.isFocusable());
        }
    }

    private int[] getCallTypes(Cursor cursor, int count) {
        int position = cursor.getPosition();
        int[] callTypes = new int[count];
        for (int index = 0; index < count; ++index) {
            callTypes[index] = cursor.getInt(CallLogQuery.CALL_TYPE);
            cursor.moveToNext();
        }
        cursor.moveToPosition(position);
        return callTypes;
    }

    private ContactInfo getContactInfoFromCallLog(Cursor c) {
        ContactInfo info = new ContactInfo();
        info.lookupUri = UriUtils.parseUriOrNull(c.getString(CallLogQuery.CACHED_LOOKUP_URI));
        info.name = c.getString(CallLogQuery.CACHED_NAME);
        info.type = c.getInt(CallLogQuery.CACHED_NUMBER_TYPE);
        info.label = c.getString(CallLogQuery.CACHED_NUMBER_LABEL);
        String matchedNumber = c.getString(CallLogQuery.CACHED_MATCHED_NUMBER);
        info.number = matchedNumber == null ? c.getString(CallLogQuery.NUMBER) : matchedNumber;
        info.normalizedNumber = c.getString(CallLogQuery.CACHED_NORMALIZED_NUMBER);
        info.photoId = c.getLong(CallLogQuery.CACHED_PHOTO_ID);
        info.photoUri = null;  // We do not cache the photo URI.
        info.formattedNumber = c.getString(CallLogQuery.CACHED_FORMATTED_NUMBER);
        return info;
    }

    private long[] getCallIds(final Cursor cursor, final int groupSize) {
        // We want to restore the position in the cursor at the end.
        int startingPosition = cursor.getPosition();
        long[] ids = new long[groupSize];
        // Copy the ids of the rows in the group.
        for (int index = 0; index < groupSize; ++index) {
            ids[index] = cursor.getLong(CallLogQuery.ID);
            cursor.moveToNext();
        }
        cursor.moveToPosition(startingPosition);
        return ids;
    }

    /**
     * get delete filter
     *
     * @return the delete selection
     */
    public String getDeleteFilter() {
        log("getDeleteFilter()");
        StringBuilder where = new StringBuilder("_id in ");
        where.append("(");
        /** M: Fix CR ALPS01569024. Use the call log id to identify the select item. @{ */
        if (mSelectedCallLogIdList.size() > 0) {
            boolean isFirst = true;
            for (int id : mSelectedCallLogIdList) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    where.append(",");
                }
                where.append("\'");
                where.append(id);
                where.append("\'");
            }
        } else {
            where.append(-1);
        }
        /** @} */

        where.append(")");
        log("getDeleteFilter() where ==  " + where.toString());
        return where.toString();
    }

    /**
     * change selected status to map
     *
     * @param listPosition position to change
     * @return int
     */
    public int changeSelectedStatusToMap(final int listPosition) {
        log("changeSelectedStatusToMap()");
        int count = 0;
        if (isGroupHeader(listPosition)) {
            count = getGroupSize(listPosition);
        } else {
            count = 1;
        }

        /** M: Fix CR ALPS01569024. Use the call log id to identify the select item. @{ */
        Cursor cursor = (Cursor) getItem(listPosition);
        if (cursor == null) {
            return mSelectedItemCount;
        }
        int position = cursor.getPosition();
        int firstId = cursor.getInt(CallLogQuery.ID);
        String firstNumber = cursor.getString(CallLogQuery.NUMBER);
        String firstName = cursor.getString(CallLogQuery.CALLS_JOIN_DATA_VIEW_DISPLAY_NAME);

        boolean shouldSelected = false;
        if (mSelectedCallLogIdList.contains(firstId)) {
            shouldSelected = false;
            mSelectedItemCount--;
            mSelectedGroupCallIdList.remove((Integer)firstId);
            mSelectedCallLogInfoList.remove(firstNumber);
        } else {
            shouldSelected = true;
            mSelectedItemCount++;
            mSelectedGroupCallIdList.add((Integer)firstId);
            mSelectedCallLogInfoList.put(firstNumber, firstName);
        }

        for (int i = 0; i < count; i++) {
            if (!cursor.moveToPosition(position + i)) {
                continue;
            }
            int id = cursor.getInt(CallLogQuery.ID);
            if (shouldSelected) {
                mSelectedCallLogIdList.add(id);
            } else {
                mSelectedCallLogIdList.remove((Integer) id);
            }
        }
        cursor.moveToPosition(position);
        return mSelectedItemCount;
        /** @} */
    }

    /**
     * get selected items count
     *
     * @return the count of selected
     */
    public int getSelectedItemCount() {
        log("getSelectedItemCount()");
        /// M: Fix CR ALPS01569024. Use the call log id to identify the select item.
        return mSelectedItemCount;
    }

    private void log(final String log) {
        Log.i(LOG_TAG, log);
    }

    /** M: Fix CR ALPS01569024. @{ */
    /**
     * Get the id list of selected call log.
     * @return the id list of selected call log.
     */
    public ArrayList<Integer> getSelectedCallLogIds() {
        return new ArrayList<Integer>(mSelectedCallLogIdList);
    }

    public ArrayList<Integer> getSelectedGroupCallIdList() {
        return mSelectedGroupCallIdList;
    }

    public HashMap<String, String> getSelectedCallLogInfoList(){
        return mSelectedCallLogInfoList;
    }

    /**
     * Set the id list of selected call log.
     */
    public void setSelectedCallLogIds(ArrayList<Integer> idList) {
        mSelectedCallLogIdList.clear();
        mSelectedCallLogIdList.addAll(idList);
    }

    /**
     * Reconcile the selected call log ids with the ids in the new cursor.
     */
    private boolean reconcileSeletetedItems(Cursor newCursor) {
        if (mSelectedCallLogIdList.isEmpty()) {
            return false;
        }
        if (newCursor == null || newCursor.getCount() <= 0) {
            mSelectedCallLogIdList.clear();
            mSelectedItemCount = 0;
            return true;
        }
        ArrayList<Integer> idList = new ArrayList<Integer>();
        ArrayList<Integer> groupIdList = new ArrayList<Integer>();
        int newSelectedItemCount = 0;
        for (int i = 0; i < getCount(); ++i) {
            int count = 0;
            Cursor cursor = (Cursor) getItem(i);
            if (cursor == null) {
                continue;
            }
            int position = cursor.getPosition();
            if (isGroupHeader(i)) {
                count = getGroupSize(i);
                log("getSelectedItemCount() count:"+count);
            } else {
                count = 1;
            }
            boolean haveSelectedCallLog = false;
            groupIdList.clear();
            for (int j = 0; j < count; j++) {
                if (!mCursor.moveToPosition(position + j)) {
                    continue;
                }
                int id = mCursor.getInt(CallLogQuery.ID);
                groupIdList.add(id);
                if (!haveSelectedCallLog && mSelectedCallLogIdList.contains(id)) {
                    haveSelectedCallLog = true;
                }
            }
            if (haveSelectedCallLog) {
                newSelectedItemCount++;
                idList.addAll(groupIdList);
            }
        }
        mSelectedCallLogIdList.clear();
        mSelectedCallLogIdList.addAll(idList);
        log("getSelectedItemCount() groupIdList:"+groupIdList);
        log("getSelectedItemCount() mSelectedCallLogIdList:"+mSelectedCallLogIdList);
        log("getSelectedItemCount() mSelectedItemCount:"+mSelectedItemCount);
        mSelectedItemCount = newSelectedItemCount;
        return true;
    }

    public void setSelectedItemChangeListener(SelectedItemChangeListener listener) {
        mSelectedItemChangeListener = listener;
    }

    public void removeSelectedItemChangeListener() {
        mSelectedItemChangeListener = null;
    }
}
