//zhangyanbin@wind-mobi.com add 20160302 for blackList start
package com.mediatek.phone.ext;

import android.os.Message;
import com.android.internal.telephony.PhoneProxy;

public interface IIncomingCallExt {
    /**
     * called when receive incoming call event, for OP01 call rejection
     * @param msg
     * @param phone
     * @internal
     */
    public boolean handlePhoneEvent(Message msg, PhoneProxy phone);
}
//zhangyanbin@wind-mobi.com add 20160302 for blackList end
