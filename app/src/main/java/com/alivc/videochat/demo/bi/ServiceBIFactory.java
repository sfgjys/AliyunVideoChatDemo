package com.alivc.videochat.demo.bi;

/**
 * Created by liujianghao on 16-8-10.
 */
public class ServiceBIFactory {
    private static LiveServiceBI sLiveServiceBI = new LiveServiceBI();
    private static InviteServiceBI sInviteServiceBI = new InviteServiceBI();
    private static AccountServiceBI sAccountServiceBI = new AccountServiceBI();
    private static InteractionServiceBI sInteractionServiceBI = new InteractionServiceBI();


    public static LiveServiceBI getLiveServiceBI() {
        return sLiveServiceBI;
    }

    public static InviteServiceBI getInviteServiceBI() {
        return sInviteServiceBI;
    }

    public static AccountServiceBI getAccountServiceBI() {
        return sAccountServiceBI;
    }

    public static InteractionServiceBI getInteractionServiceBI() {
        return sInteractionServiceBI;
    }

}
