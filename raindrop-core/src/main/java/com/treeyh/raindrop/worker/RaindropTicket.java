package com.treeyh.raindrop.worker;

import com.treeyh.raindrop.Raindrop;

public class RaindropTicket {

    private RaindropTicket(){}

    private static class Instance {
        private static final RaindropTicket INSTANCE = new RaindropTicket();
    }

    /**
     * 单例
     * @return
     */
    public static RaindropTicket getInstance() {
        return RaindropTicket.Instance.INSTANCE;
    }



}
