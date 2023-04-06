package com.treeyh.raindrop.worker;

import com.treeyh.raindrop.Raindrop;

public class RaindropWorker {


    private RaindropWorker(){}

    private static class Instance {
        private static final RaindropWorker INSTANCE = new RaindropWorker();
    }

    /**
     * 单例
     * @return
     */
    public static RaindropWorker getInstance() {
        return RaindropWorker.Instance.INSTANCE;
    }
}
