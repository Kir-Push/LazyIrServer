package com.push.lazyir.modules.notifications.sms;

import java.util.List;

/**
 * Created by buhalo on 18.01.18.
 */

public class SmsPack {
    private List<Sms> sms;

    public SmsPack(List<Sms> sms) {
        this.sms = sms;
    }

    public SmsPack() {
    }

    public List<Sms> getSms() {
        return sms;
    }

    public void setSms(List<Sms> sms) {
        this.sms = sms;
    }
}
