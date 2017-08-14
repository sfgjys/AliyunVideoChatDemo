package com.alivc.videochat.demo.bi;


/**
 * 类的描述: 可以获取各种ServiceBI实例对象
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
