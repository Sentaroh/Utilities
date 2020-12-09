/*
The MIT License (MIT)
Copyright (c) 2018 Sentaroh

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights to use,
copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
and to permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be included in all copies or
substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.

*/
package com.sentaroh.android.Utilities;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import static android.content.Context.NSD_SERVICE;

public class NsdSmbNameResolution {
    private static Logger log = LoggerFactory.getLogger(NsdSmbNameResolution.class);
    static final String SMB_SERVICE_TYPE = "_smb._tcp.";

    private NsdManager mNsdManager =null;
    private String mQueryName ="";
    private String mQueryResult ="";
    private ArrayList<String> mNsdServiceList=new ArrayList<String>();

    public NsdSmbNameResolution(Context c) {
        mNsdManager = (NsdManager) c.getSystemService(NSD_SERVICE);
    }

    public String query(String query_name, final int time_out) {
        mQueryName =query_name;
        log.debug("Query Start for Name="+query_name);
        startDiscovery();
        synchronized (mNsdManager) {
            try {
                mNsdManager.wait(time_out);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            stopDiscovery();
        }
        log.debug("Query result="+ mQueryResult +", Name="+query_name);
        return mQueryResult;
    }

    public ArrayList<String> list(final int time_out) {
        mQueryName ="";
        log.debug("List Start");
        startDiscovery();
        synchronized (mNsdManager) {
            try {
                mNsdManager.wait(time_out);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            stopDiscovery();
        }
        log.debug("List ended, count="+mNsdServiceList.size());
        return mNsdServiceList;
    }

    private void startDiscovery() {
        mNsdManager.discoverServices(SMB_SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
    }

    private void stopDiscovery() {
        mNsdManager.stopServiceDiscovery(discoveryListener);
    }

    NsdManager.DiscoveryListener discoveryListener = new NsdManager.DiscoveryListener() {
        public void setResolutionParameter(NsdManager manager, String name) {
            mNsdManager =manager;
            mQueryName =name;
        }
        @Override
        public void onStopDiscoveryFailed(String serviceType, int errorCode) {
            log.trace(String.format("Failed to stop discovery serviceType=%s, errorCode=%d", serviceType, errorCode));
        }
        @Override
        public void onStartDiscoveryFailed(String serviceType, int errorCode) {
            log.trace(String.format("Failed to start discovery serviceType=%s, errorCode=%d", serviceType, errorCode));
        }
        @Override
        public void onServiceLost(NsdServiceInfo serviceInfo) {
            log.trace(String.format("Service lost serviceInfo=%s", serviceInfo));
            synchronized (mNsdManager) {
                mNsdManager.notify();
            }
        }
        @Override
        public void onServiceFound(NsdServiceInfo serviceInfo) {
            mNsdServiceList.add(serviceInfo.getServiceName());
            log.trace(String.format("Service found serviceInfo=%s", serviceInfo));
            if (serviceInfo.getServiceName().equals(mQueryName)) {
                mNsdManager.resolveService(serviceInfo, new NsdManager.ResolveListener() {
                    @Override
                    public void onServiceResolved(NsdServiceInfo serviceInfo) {
                        log.trace(String.format("Service resolved serviceInfo=%s", serviceInfo));
                        mQueryResult =serviceInfo.getHost().getHostAddress();
                        synchronized (mNsdManager) {
                            mNsdManager.notify();
                        }
                    }
                    @Override
                    public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                        log.trace(String.format("Failed to resolve serviceInfo=%s, errorCode=%d", serviceInfo, errorCode));
                        synchronized (mNsdManager) {
                            mNsdManager.notify();
                        }
                    }
                });
            }
        }
        @Override
        public void onDiscoveryStopped(String serviceType) {
            log.trace(String.format("Discovery stopped serviceType=%s", serviceType));
            synchronized (mNsdManager) {
                mNsdManager.notify();
            }
        }
        @Override
        public void onDiscoveryStarted(String serviceType) {
            log.trace(String.format("Discovery started serviceType=%s", serviceType));
        }
    };

}
